package top.cmarco.temperature.plugin.sampler;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.NotNull;
import top.cmarco.temperature.core.environment.ActiveEffect;
import top.cmarco.temperature.core.environment.ArmorProfile;
import top.cmarco.temperature.core.environment.EmitterSample;
import top.cmarco.temperature.core.environment.ThermalEnvironment;
import top.cmarco.temperature.core.physics.TemperatureModel;
import top.cmarco.temperature.plugin.effect.EffectRegistry;

/**
 * Translates a live player and world into the immutable {@link ThermalEnvironment} the core reads.
 *
 * <p>This is the single place the plugin asks the server questions about the world. Everything it
 * learns — the biome, the height, whether the sky is open, whether it is raining, the blocks nearby,
 * the armour, the timed effects — is reduced to plain values, so the physics never holds a live
 * {@code Player} reference and can never accidentally read the world from off the server thread.
 *
 * <p>Not thread-safe; called from the server thread.
 */
public final class EnvironmentSampler {

    private static final double DEFAULT_HUMIDITY_JITTER_MAX = 1.0;

    private final TemperatureModel model;
    private final EmitterScanner scanner;
    private final EffectRegistry effectRegistry;

    /**
     * @param model          the model, used to resolve the biome's climate
     * @param scanner        the nearby-block scanner
     * @param effectRegistry the source of timed effects
     */
    public EnvironmentSampler(@NotNull TemperatureModel model, @NotNull EmitterScanner scanner,
                              @NotNull EffectRegistry effectRegistry) {
        this.model = model;
        this.scanner = scanner;
        this.effectRegistry = effectRegistry;
    }

    /**
     * Measures a player's surroundings.
     *
     * @param player          the player
     * @param nowEpochMillis  the current instant, for timed effects
     * @return the frozen environment
     */
    @NotNull
    public ThermalEnvironment sample(@NotNull Player player, long nowEpochMillis) {
        Location location = player.getLocation();
        World world = location.getWorld();
        if (world == null) {
            throw new IllegalStateException("player has no world");
        }

        String biomeKey = BiomeKeys.keyOf(location.getBlock().getBiome());
        List<EmitterSample> emitters = scanner.scan(player);
        List<ActiveEffect> effects = effectRegistry.activeEffects(
                player.getUniqueId(), nowEpochMillis, model.consumptionModel());

        return ThermalEnvironment.builder()
                .climate(model.resolveClimate(biomeKey))
                .altitude(location.getY())
                .worldBounds(world.getMinHeight(), world.getMaxHeight())
                .exposedToSky(isExposedToSky(location))
                .precipitating(isPrecipitating(world, location))
                .submerged(player.isInWater())
                .gameTime(world.getGameTime())
                .emitters(emitters)
                .armor(armorProfile(player))
                .activeEffects(effects)
                .humidityJitter(ThreadLocalRandom.current().nextDouble(DEFAULT_HUMIDITY_JITTER_MAX))
                .build();
    }

    /**
     * A sample is "exposed to sky" when nothing solid stands above it.
     *
     * <p>The legacy plugin compared the highest block's {@code x}/{@code z} to the sampled block's
     * — which are the same coordinates by construction — so the test was always true and every
     * player, however deep underground, received the solar bonus.
     */
    private boolean isExposedToSky(Location location) {
        World world = location.getWorld();
        return world != null
                && world.getHighestBlockYAt(location.getBlockX(), location.getBlockZ()) <= location.getBlockY();
    }

    private boolean isPrecipitating(World world, Location location) {
        if (!world.hasStorm()) {
            return false;
        }
        // Rain does not reach a player who is sheltered, and the snow line matters: precipitation is
        // only "wet" below the storm ceiling, which the sky check already covers indoors.
        return isExposedToSky(location);
    }

    private ArmorProfile armorProfile(Player player) {
        int leather = 0;
        int fireProtection = 0;
        for (ItemStack item : player.getInventory().getArmorContents()) {
            if (item == null) {
                continue;
            }
            ItemMeta meta = item.getItemMeta();
            if (meta instanceof LeatherArmorMeta) {
                leather++;
            }
            if (meta != null && meta.hasEnchant(Enchantment.FIRE_PROTECTION)) {
                fireProtection++;
            }
        }
        return new ArmorProfile(leather, fireProtection);
    }
}
