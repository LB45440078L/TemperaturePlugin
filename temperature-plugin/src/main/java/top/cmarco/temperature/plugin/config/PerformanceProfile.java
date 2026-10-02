package top.cmarco.temperature.plugin.config;

import java.util.Locale;
import java.util.Optional;

/**
 * How often the plugin recomputes a player's temperature.
 *
 * <p>The legacy plugin exposed the same four names; the tick counts are kept so an operator's
 * existing choice keeps its meaning. {@code EXTREME} runs every tick and exists for debugging or
 * very small servers, not for production.
 */
public enum PerformanceProfile {

    /** Every twenty ticks — for large servers where the simulation is a background nicety. */
    LOW(20L),
    /** Every ten ticks. */
    MEDIUM(10L),
    /** Every eight ticks — the default. */
    HIGH(8L),
    /** Every tick — maximum fidelity, maximum cost. */
    EXTREME(1L);

    private final long updateIntervalTicks;

    PerformanceProfile(long updateIntervalTicks) {
        this.updateIntervalTicks = updateIntervalTicks;
    }

    /** @return the number of ticks between temperature updates. */
    public long updateIntervalTicks() {
        return updateIntervalTicks;
    }

    /**
     * Parses a profile name case-insensitively.
     *
     * @param raw the configured value; may be {@code null}
     * @return the matching profile, or empty if unknown
     */
    public static Optional<PerformanceProfile> parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(valueOf(raw.trim().toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
