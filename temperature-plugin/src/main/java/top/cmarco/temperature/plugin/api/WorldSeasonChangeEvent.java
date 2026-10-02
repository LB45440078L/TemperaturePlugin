package top.cmarco.temperature.plugin.api;

import org.bukkit.World;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.world.WorldEvent;
import org.jetbrains.annotations.NotNull;
import top.cmarco.temperature.core.climate.Season;

/**
 * Fired when a world's season changes.
 *
 * <p>A listener may cancel the transition (the world keeps its previous season) or substitute a
 * different upcoming season. The plugin re-checks every configured world on a timer, so a cancelled
 * transition is simply retried on the next check.
 */
public class WorldSeasonChangeEvent extends WorldEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Season previousSeason;
    private Season upcomingSeason;
    private boolean cancelled;

    /**
     * @param world    the world whose season changed
     * @param previous the season the world is leaving
     * @param upcoming the season the world is entering
     */
    public WorldSeasonChangeEvent(@NotNull World world, @NotNull Season previous, @NotNull Season upcoming) {
        super(world);
        this.previousSeason = previous;
        this.upcomingSeason = upcoming;
    }

    /** @return the season the world is leaving. */
    @NotNull
    public Season previousSeason() {
        return previousSeason;
    }

    /** @return the season the world is entering. */
    @NotNull
    public Season upcomingSeason() {
        return upcomingSeason;
    }

    /** @param season the season the world should enter instead. */
    public void setUpcomingSeason(@NotNull Season season) {
        this.upcomingSeason = season;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
