package top.cmarco.temperature.plugin.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import top.cmarco.temperature.plugin.TemperaturePlugin;

/**
 * Releases a player's transient state when they leave.
 *
 * <p>Body temperature, the water-cooling clock and the threshold counters are all per-session — a
 * player who logs off and back on gets a fresh, ambient reading rather than the temperature of the
 * spot they logged off in. Leaving the maps populated would also be a slow leak on a busy server.
 */
public final class PlayerSessionListener implements Listener {

    private final TemperaturePlugin plugin;

    /**
     * @param plugin the owning plugin
     */
    public PlayerSessionListener(@NotNull TemperaturePlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * @param event the quit event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(@NotNull PlayerQuitEvent event) {
        var id = event.getPlayer().getUniqueId();
        plugin.service().engine().forget(id);
        plugin.effectRegistry().forget(id);
        plugin.service().effects().forget(id);
    }
}
