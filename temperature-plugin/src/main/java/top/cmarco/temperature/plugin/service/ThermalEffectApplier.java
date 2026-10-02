package top.cmarco.temperature.plugin.service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import top.cmarco.temperature.core.config.TemperatureSettings;
import top.cmarco.temperature.core.service.TemperatureReading;

/**
 * Turns a crossing of the heat or cold threshold into a visible consequence.
 *
 * <p>Above the heat threshold a player catches fire unless protected; below the cold threshold they
 * accumulate freeze ticks unless wearing enough leather. Below and above, in between, both are wound
 * back — the legacy plugin left a player's freeze ticks in place once they warmed up, so the screen
 * frost never cleared.
 *
 * <p>The thresholds, the protection counts and the effect magnitudes are constants rather than
 * configuration: they mirror vanilla's own values and the legacy plugin's behaviour, and exposing
 * them invites a server to make the effects unreadable. What <em>is</em> configurable is the
 * temperature at which they fire, which lives in the core settings.
 *
 * <p>Not thread-safe; called from the server thread.
 */
public final class ThermalEffectApplier {

    /** Vanilla's first-stage freeze level; the effect builds from here. */
    private static final int FREEZE_BASELINE_TICKS = 25;
    /** How many freeze ticks each step adds. */
    private static final int FREEZE_TICKS_PER_STEP = 1;
    /** Leather pieces that fully insulate a player against the cold penalty. */
    private static final int LEATHER_PIECES_FOR_IMMUNITY = 3;
    /** Fire ticks applied on each heat step, matching thirty-two vanilla damage ticks of burn. */
    private static final int BURN_FIRE_TICKS = 20;
    /** Minimum milliseconds between burn applications, so the screen is not strobed. */
    private static final long BURN_INTERVAL_MILLIS = 1_000L;
    /** Fire-protection pieces that fully insulate a player against the heat penalty. */
    private static final int FIRE_PROTECTION_PIECES_FOR_IMMUNITY = 3;

    private final double heatThreshold;
    private final double coldThreshold;

    private final Map<UUID, Long> lastBurn = new HashMap<>();
    private final Map<UUID, Integer> freezeTicks = new HashMap<>();

    /**
     * @param settings the tunables supplying the two thresholds
     */
    public ThermalEffectApplier(@NotNull TemperatureSettings settings) {
        this.heatThreshold = settings.heatEffectThresholdCelsius();
        this.coldThreshold = settings.coldEffectThresholdCelsius();
    }

    /**
     * Applies or clears the temperature consequences for a player.
     *
     * @param player  the player
     * @param reading their latest reading
     * @param nowMillis the current instant
     */
    public void apply(@NotNull Player player, @NotNull TemperatureReading reading, long nowMillis) {
        double celsius = reading.bodyCelsius();
        if (celsius >= heatThreshold) {
            applyHeat(player, nowMillis);
        } else if (celsius <= coldThreshold) {
            applyCold(player);
        } else {
            clear(player);
        }
    }

    private void applyHeat(Player player, long nowMillis) {
        UUID id = player.getUniqueId();
        if (isHeatImmune(player)) {
            return;
        }
        long last = lastBurn.getOrDefault(id, nowMillis - BURN_INTERVAL_MILLIS);
        if (nowMillis - last >= BURN_INTERVAL_MILLIS || last == nowMillis) {
            player.setFireTicks(BURN_FIRE_TICKS);
            lastBurn.put(id, nowMillis);
        }
    }

    private void applyCold(Player player) {
        UUID id = player.getUniqueId();
        if (countLeather(player) >= LEATHER_PIECES_FOR_IMMUNITY) {
            return;
        }
        int current = freezeTicks.getOrDefault(id, FREEZE_BASELINE_TICKS);
        int next = Math.min(current + FREEZE_TICKS_PER_STEP, player.getMaxFreezeTicks());
        player.setFreezeTicks(next);
        freezeTicks.put(id, next);
    }

    private void clear(Player player) {
        UUID id = player.getUniqueId();
        lastBurn.remove(id);
        freezeTicks.remove(id);
        if (player.getFreezeTicks() > 0) {
            player.setFreezeTicks(0);
        }
    }

    private boolean isHeatImmune(Player player) {
        if (player.hasPotionEffect(PotionEffectType.FIRE_RESISTANCE)) {
            return true;
        }
        int protectedPieces = 0;
        for (ItemStack item : player.getInventory().getArmorContents()) {
            if (item != null && item.getItemMeta() != null
                    && item.getItemMeta().hasEnchant(Enchantment.FIRE_PROTECTION)) {
                protectedPieces++;
            }
        }
        return protectedPieces >= FIRE_PROTECTION_PIECES_FOR_IMMUNITY;
    }

    private int countLeather(Player player) {
        int leather = 0;
        for (ItemStack item : player.getInventory().getArmorContents()) {
            if (item != null && item.getItemMeta() instanceof org.bukkit.inventory.meta.LeatherArmorMeta) {
                leather++;
            }
        }
        return leather;
    }

    /**
     * Forgets a player's thresholds state.
     *
     * @param playerId the player
     */
    public void forget(UUID playerId) {
        lastBurn.remove(playerId);
        freezeTicks.remove(playerId);
    }

    /** Forgets every player. */
    public void clear() {
        lastBurn.clear();
        freezeTicks.clear();
    }
}
