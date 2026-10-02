package top.cmarco.temperature.plugin.task;

import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import top.cmarco.temperature.plugin.service.SeasonTracker;
import top.cmarco.temperature.plugin.service.WorldRegistry;

/**
 * Polls each simulated world's clock and fires a season-change event when the season turns.
 *
 * <p>A slow poll is enough — seasons change in real time measured in in-game months — and polling
 * rather than scheduling keeps the transition correct even if the server was asleep across the
 * boundary.
 */
public final class SeasonWatchTask extends BukkitRunnable {

    private final SeasonTracker tracker;
    private final WorldRegistry worlds;

    /**
     * @param tracker the tracker to refresh
     * @param worlds  the simulated worlds
     */
    public SeasonWatchTask(@NotNull SeasonTracker tracker, @NotNull WorldRegistry worlds) {
        this.tracker = tracker;
        this.worlds = worlds;
    }

    @Override
    public void run() {
        for (World world : worlds.worlds()) {
            tracker.refresh(world);
        }
    }
}
