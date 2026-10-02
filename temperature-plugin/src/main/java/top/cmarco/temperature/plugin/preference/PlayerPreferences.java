package top.cmarco.temperature.plugin.preference;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import top.cmarco.temperature.core.unit.TemperatureUnit;

/**
 * Stores each player's display choices in their persistent data container.
 *
 * <p>Per-player state is kept on the player, not in a plugin map, so it survives restarts without a
 * database and travels with the player across worlds. The legacy plugin used the same container but
 * inverted the meaning of the byte ({@code 0} meant "off" while being written by a method named
 * {@code enableTemperature}), which made every later reader a puzzle; here the flag is a plain
 * boolean and the methods say what they do.
 */
public final class PlayerPreferences {

    private static final String UNIT_KEY = "temperature_unit";
    private static final String DISPLAY_KEY = "temperature_display_enabled";

    private final NamespacedKey unitKey;
    private final NamespacedKey displayKey;

    /**
     * @param plugin the owning plugin, providing the namespace
     */
    public PlayerPreferences(@NotNull Plugin plugin) {
        this.unitKey = new NamespacedKey(plugin, UNIT_KEY);
        this.displayKey = new NamespacedKey(plugin, DISPLAY_KEY);
    }

    /**
     * @param player    the player
     * @param fallback  the unit to use when the player has never chosen one
     * @return the player's display unit
     */
    @NotNull
    public TemperatureUnit unit(@NotNull Player player, @NotNull TemperatureUnit fallback) {
        String stored = player.getPersistentDataContainer().get(unitKey, PersistentDataType.STRING);
        return TemperatureUnit.parse(stored).orElse(fallback);
    }

    /**
     * @param player the player
     * @param unit   the unit to remember
     */
    public void setUnit(@NotNull Player player, @NotNull TemperatureUnit unit) {
        player.getPersistentDataContainer().set(unitKey, PersistentDataType.STRING, unit.name());
    }

    /**
     * @param player the player
     * @return whether the player wants the action bar drawn; defaults to {@code true}
     */
    public boolean displayEnabled(@NotNull Player player) {
        Byte stored = player.getPersistentDataContainer().get(displayKey, PersistentDataType.BYTE);
        return stored == null || stored != 0;
    }

    /**
     * @param player  the player
     * @param enabled whether the action bar should be drawn
     */
    public void setDisplayEnabled(@NotNull Player player, boolean enabled) {
        player.getPersistentDataContainer().set(displayKey, PersistentDataType.BYTE, (byte) (enabled ? 1 : 0));
    }

    /**
     * Removes every preference this plugin stores.
     *
     * @param player the player
     */
    public void clear(@NotNull Player player) {
        PersistentDataContainer container = player.getPersistentDataContainer();
        container.remove(unitKey);
        container.remove(displayKey);
    }
}
