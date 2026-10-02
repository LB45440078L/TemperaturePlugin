package top.cmarco.temperature.plugin.service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import top.cmarco.temperature.core.climate.Season;
import top.cmarco.temperature.core.physics.TemperatureModel;
import top.cmarco.temperature.plugin.api.WorldSeasonChangeEvent;

/**
 * Watches each world's season and fires {@link WorldSeasonChangeEvent} when it turns.
 *
 * <p>The season is derived from game time, so this tracker does not advance it — it only remembers
 * the last season seen and notices a difference. That means a server that was offline for a week
 * picks up the correct season the moment it starts, and a world whose clock was moved by an admin is
 * followed rather than argued with.
 *
 * <p>A listener may cancel or redirect a transition; the tracker honours both and remembers the
 * resulting season, so a redirected season is not immediately overwritten on the next poll.
 */
public final class SeasonTracker {

    private final TemperatureModel model;
    private final Plugin plugin;
    private final Map<UUID, Season> lastSeen = new HashMap<>();

    /**
     * @param model  the model, supplying the calendar
     * @param plugin the owning plugin, used to fire events
     */
    public SeasonTracker(@NotNull TemperatureModel model, @NotNull Plugin plugin) {
        this.model = model;
        this.plugin = plugin;
    }

    /**
     * Recomputes a world's season and fires an event if it has changed.
     *
     * @param world the world to check
     */
    public void refresh(@NotNull World world) {
        Season current = model.seasonCycle().seasonAt(world.getGameTime());
        Season previous = lastSeen.putIfAbsent(world.getUID(), current);
        if (previous == null || previous == current) {
            return;
        }

        WorldSeasonChangeEvent event = new WorldSeasonChangeEvent(world, previous, current);
        Bukkit.getPluginManager().callEvent(event);
        lastSeen.put(world.getUID(), event.isCancelled() ? previous : event.upcomingSeason());
    }

    /**
     * @param world the world
     * @return the season last recorded for the world, or the season its clock currently implies
     */
    @NotNull
    public Season season(@NotNull World world) {
        Season recorded = lastSeen.get(world.getUID());
        return recorded != null ? recorded : model.seasonCycle().seasonAt(world.getGameTime());
    }

    /** Forgets every world, so the next refresh re-seeds without firing a spurious event. */
    public void clear() {
        lastSeen.clear();
    }
}
