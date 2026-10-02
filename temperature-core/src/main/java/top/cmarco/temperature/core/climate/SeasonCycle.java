package top.cmarco.temperature.core.climate;

import top.cmarco.temperature.core.config.TemperatureSettings;

/**
 * Turns a world's game time into a season and into a smooth seasonal temperature offset.
 *
 * <p>The calendar is deliberately simple and fast-forward-free: one in-game day is
 * {@code ticksPerDay} ticks (24000 by default), one season is {@code daysPerSeason} days (31), and
 * the four seasons repeat forever. Season <em>index</em> is therefore an integer division, which
 * is exactly reproducible from a save file and needs no per-world bookkeeping.
 *
 * <p>Two things are exposed:
 *
 * <ul>
 *   <li>{@link #seasonAt(long)} — the discrete season, used for the season-change event and the
 *       display. It changes in steps.</li>
 *   <li>{@link #seasonalOffsetCelsius(long)} — a continuous offset that weaves between the
 *       configured per-season offsets as the season progresses, so temperature drifts rather than
 *       jumping on the first day of a new season. This is an addition over the legacy plugin,
 *       which snapped by a fixed number of degrees at each boundary.</li>
 * </ul>
 */
public final class SeasonCycle {

    private final long ticksPerDay;
    private final long daysPerSeason;
    private final long ticksPerSeason;

    /**
     * @param ticksPerDay   in-game ticks in a full day/night cycle; must be positive
     * @param daysPerSeason in-game days in each season; must be positive
     */
    public SeasonCycle(long ticksPerDay, long daysPerSeason) {
        if (ticksPerDay <= 0 || daysPerSeason <= 0) {
            throw new IllegalArgumentException("ticksPerDay and daysPerSeason must be positive");
        }
        this.ticksPerDay = ticksPerDay;
        this.daysPerSeason = daysPerSeason;
        this.ticksPerSeason = ticksPerDay * daysPerSeason;
    }

    /** @return the season the supplied game time falls in. */
    public Season seasonAt(long gameTime) {
        long elapsedSeasons = Math.floorDiv(gameTime, ticksPerSeason);
        return Season.atCycleIndex((int) Math.floorMod(elapsedSeasons, Season.cycle().length));
    }

    /**
     * @param gameTime the world's game time, in ticks
     * @return how far through the current season the world is, in {@code [0, 1)}
     */
    public double progressWithinSeason(long gameTime) {
        long into = Math.floorMod(gameTime, ticksPerSeason);
        return (double) into / (double) ticksPerSeason;
    }

    /**
     * @param gameTime the world's game time, in ticks
     * @return the time of day as a fraction of a full day, in {@code [0, 1)}; noon is 0.25
     */
    public double dayFraction(long gameTime) {
        return (double) Math.floorMod(gameTime, ticksPerDay) / (double) ticksPerDay;
    }

    /**
     * Computes the seasonally adjusted offset, blended smoothly between adjacent seasons.
     *
     * @param gameTime the world's game time, in ticks
     * @param settings the tunables, supplying the per-season offsets
     * @return the offset to add to the air temperature, in degrees Celsius
     */
    public double seasonalOffsetCelsius(long gameTime, TemperatureSettings settings) {
        Season current = seasonAt(gameTime);
        Season following = current.next();
        double from = settings.seasonOffsetCelsius(current);
        double to = settings.seasonOffsetCelsius(following);
        double progress = progressWithinSeason(gameTime);
        return from + (to - from) * progress;
    }

    /** @return in-game ticks in a season, for callers that need to reason about the calendar. */
    public long ticksPerSeason() {
        return ticksPerSeason;
    }
}
