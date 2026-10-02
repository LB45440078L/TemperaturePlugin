package top.cmarco.temperature.core.climate;

/**
 * The discrete seasons a world cycles through.
 *
 * <p>Seasons exist as an enum rather than a number because they are surfaced to operators and to
 * other plugins through {@code WorldSeasonChangeEvent}: a stable, named identity is far easier to
 * write a listener against than "month index 2". The cycle order is
 * {@link #SUMMER} → {@link #AUTUMN} → {@link #WINTER} → {@link #SPRING}, matching the legacy
 * plugin so existing worlds keep their current season.
 *
 * <p>Per-season temperature offsets are <em>not</em> stored on the constant; they are tunables and
 * live in {@code TemperatureSettings}.
 */
public enum Season {

    SPRING("Spring"),
    SUMMER("Summer"),
    AUTUMN("Autumn"),
    WINTER("Winter");

    private static final Season[] CYCLE = {SUMMER, AUTUMN, WINTER, SPRING};

    private final String displayName;

    Season(String displayName) {
        this.displayName = displayName;
    }

    /** @return the season's position in the cycle, 0 being the first season after world creation. */
    public int cycleIndex() {
        for (int i = 0; i < CYCLE.length; i++) {
            if (CYCLE[i] == this) {
                return i;
            }
        }
        throw new IllegalStateException("Cycle does not contain " + this);
    }

    /** @return the season that follows this one, wrapping around the year. */
    public Season next() {
        return CYCLE[(cycleIndex() + 1) % CYCLE.length];
    }

    /** @return the season that precedes this one, wrapping around the year. */
    public Season previous() {
        return CYCLE[(cycleIndex() + CYCLE.length - 1) % CYCLE.length];
    }

    /** @return a human-readable name suitable for an action bar or a message. */
    public String displayName() {
        return displayName;
    }

    /** @return the season at a given index in the cycle, wrapping for any integer. */
    public static Season atCycleIndex(int index) {
        return CYCLE[Math.floorMod(index, CYCLE.length)];
    }

    /** @return every season in cycle order, for iteration that must not depend on declaration order. */
    public static Season[] cycle() {
        return CYCLE.clone();
    }
}
