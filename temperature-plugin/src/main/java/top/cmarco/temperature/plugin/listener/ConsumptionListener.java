package top.cmarco.temperature.plugin.listener;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import top.cmarco.temperature.core.environment.ActiveEffect;
import top.cmarco.temperature.plugin.effect.EffectRegistry;

/**
 * Watches what players eat and drink.
 *
 * <p>Two things are picked up:
 *
 * <ul>
 *   <li>A drink of plain water starts the cooling curve.</li>
 *   <li>Any consumable tagged in its persistent data container with a temperature modifier applies
 *       that modifier while it lasts.</li>
 * </ul>
 *
 * <p>The tagged-consumable keys replace the raw NBT tags the legacy plugin read through a bundled
 * NBT library. Bukkit's own persistent data container is the supported way to attach custom data to
 * an item, needs no third-party dependency to be shaded and relocated, and survives across versions.
 * The cost is a migration: an item tagged for the old plugin must be re-tagged with the new keys.
 * The keys are:
 *
 * <ul>
 *   <li>{@code temperature_modifier_type} — {@code increase} or {@code decrease}</li>
 *   <li>{@code temperature_modifier_amount} — a number of degrees</li>
 *   <li>{@code temperature_modifier_time} — how long it lasts, in seconds</li>
 * </ul>
 */
public final class ConsumptionListener implements Listener {

    private final EffectRegistry effects;
    private final NamespacedKey typeKey;
    private final NamespacedKey amountKey;
    private final NamespacedKey timeKey;

    /**
     * @param plugin  the owning plugin, providing the namespace
     * @param effects the registry to add effects to
     */
    public ConsumptionListener(@NotNull Plugin plugin, @NotNull EffectRegistry effects) {
        this.effects = effects;
        this.typeKey = new NamespacedKey(plugin, "temperature_modifier_type");
        this.amountKey = new NamespacedKey(plugin, "temperature_modifier_amount");
        this.timeKey = new NamespacedKey(plugin, "temperature_modifier_time");
    }

    /**
     * @param event the consume event
     */
    @EventHandler
    public void onConsume(@NotNull PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (isPlainWater(item)) {
            effects.recordWater(player.getUniqueId(), System.currentTimeMillis());
        }
        applyTaggedModifier(player, item);
    }

    private boolean isPlainWater(ItemStack item) {
        if (item == null || item.getType() != Material.POTION) {
            return false;
        }
        return item.getItemMeta() instanceof PotionMeta potion
                && potion.getBasePotionType() == org.bukkit.potion.PotionType.WATER;
    }

    private void applyTaggedModifier(Player player, ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }
        PersistentDataContainer container = meta.getPersistentDataContainer();
        String type = container.get(typeKey, PersistentDataType.STRING);
        Double amount = container.get(amountKey, PersistentDataType.DOUBLE);
        Long seconds = container.get(timeKey, PersistentDataType.LONG);
        if (type == null || amount == null || seconds == null || seconds <= 0L) {
            return;
        }

        boolean increases = !type.equalsIgnoreCase("decrease");
        double delta = Math.abs(amount) * (increases ? 1.0 : -1.0);
        long expiresAt = System.currentTimeMillis() + seconds * 1000L;
        effects.addTimed(player.getUniqueId(),
                new ActiveEffect(delta, expiresAt, "consumable:" + type.toLowerCase(java.util.Locale.ROOT)));
    }
}
