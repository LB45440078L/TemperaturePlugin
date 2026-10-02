package top.cmarco.temperature.plugin.task;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import top.cmarco.temperature.plugin.permission.Permissions;
import top.cmarco.temperature.plugin.service.TemperatureService;
import top.cmarco.temperature.plugin.service.WorldRegistry;

/**
 * Advances every eligible player's temperature on the configured cadence.
 *
 * <p>Only players who hold {@link Permissions#USE} are simulated, and only in configured worlds, so
 * the cost of the plugin is bounded by the players who opted in rather than the player count.
 */
public final class TemperatureTickTask extends BukkitRunnable {

    private final TemperatureService service;
    private final WorldRegistry worlds;

    /**
     * @param service the service to tick
     * @param worlds  the simulated worlds
     */
    public TemperatureTickTask(@NotNull TemperatureService service, @NotNull WorldRegistry worlds) {
        this.service = service;
        this.worlds = worlds;
    }

    @Override
    public void run() {
        long now = System.currentTimeMillis();
        for (World world : worlds.worlds()) {
            for (Player player : world.getPlayers()) {
                if (player.hasPermission(Permissions.USE)) {
                    service.tick(player, now);
                }
            }
        }
    }
}
