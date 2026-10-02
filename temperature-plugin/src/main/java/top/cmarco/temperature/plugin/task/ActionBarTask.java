package top.cmarco.temperature.plugin.task;

import java.util.OptionalDouble;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import top.cmarco.temperature.core.climate.Season;
import top.cmarco.temperature.plugin.permission.Permissions;
import top.cmarco.temperature.plugin.service.SeasonTracker;
import top.cmarco.temperature.plugin.service.TemperatureService;
import top.cmarco.temperature.plugin.service.WorldRegistry;
import top.cmarco.temperature.plugin.ui.ActionBarRenderer;

/**
 * Draws each player's action bar on its own, faster cadence.
 *
 * <p>Rendering is separated from simulation so the display can be smooth — every few ticks — while
 * the physics stays cheap. A player with the display switched off is skipped entirely, and a player
 * who has not been simulated yet has no reading to draw and is left alone until the tick task has
 * produced one.
 */
public final class ActionBarTask extends BukkitRunnable {

    private final TemperatureService service;
    private final WorldRegistry worlds;
    private final SeasonTracker seasons;
    private final ActionBarRenderer renderer;

    /**
     * @param service  the source of readings
     * @param worlds   the simulated worlds
     * @param seasons  the season source
     * @param renderer the renderer
     */
    public ActionBarTask(@NotNull TemperatureService service, @NotNull WorldRegistry worlds,
                         @NotNull SeasonTracker seasons, @NotNull ActionBarRenderer renderer) {
        this.service = service;
        this.worlds = worlds;
        this.seasons = seasons;
        this.renderer = renderer;
    }

    @Override
    public void run() {
        if (!service.display().enabled()) {
            return;
        }
        for (World world : worlds.worlds()) {
            Season season = seasons.season(world);
            for (Player player : world.getPlayers()) {
                if (!player.hasPermission(Permissions.USE)
                        || !service.preferences().displayEnabled(player)) {
                    continue;
                }
                OptionalDouble body = service.bodyTemperature(player);
                if (body.isEmpty()) {
                    continue;
                }
                player.sendActionBar(renderer.render(
                        service.unitOf(player), body.getAsDouble(), season, service.display()));
            }
        }
    }
}
