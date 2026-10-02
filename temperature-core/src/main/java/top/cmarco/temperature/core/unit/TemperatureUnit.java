package top.cmarco.temperature.core.unit;

import java.util.Locale;
import java.util.Optional;

/**
 * The temperature scales the plugin can render a reading in.
 *
 * <p>Degrees Celsius is the canonical working scale: the entire simulation runs in Celsius and
 * only converts at the very edge, when a value is written to a player. Each constant therefore
 * carries two affine transforms — {@link #fromCelsius} and {@link #toCelsius} — that must be
 * exact inverses of one another.
 *
 * <p>All of the historic scales are defined here as well, because the original plugin advertised
 * them and removing them would be a regression for existing players. The Newton and Rømer
 * inverses in the original code were wrong; see {@code docs/physics_model.md} for the corrected
 * definitions and the reference the values come from.
 */
public enum TemperatureUnit {

    /** The SI-adjacent scale the simulation works in. Water freezes at 0 and boils at 100. */
    CELSIUS("\u00b0C", "Celsius", v -> v, v -> v),

    /** The customary US scale. */
    FAHRENHEIT("\u00b0F", "Fahrenheit", c -> c * 9.0 / 5.0 + 32.0, f -> (f - 32.0) * 5.0 / 9.0),

    /** The thermodynamic (absolute) scale. */
    KELVIN("K", "Kelvin", c -> c + 273.15, k -> k - 273.15),

    /** The absolute scale with Fahrenheit-sized degrees. */
    RANKINE("\u00b0R", "Rankine", c -> (c + 273.15) * 9.0 / 5.0, r -> r * 5.0 / 9.0 - 273.15),

    /** The obsolete French scale. Water freezes at 0 and boils at 80. */
    REAUMUR("\u00b0Re", "R\u00e9aumur", c -> c * 0.8, r -> r / 0.8),

    /** The obsolete Newton scale. Water freezes at 0 and boils at 33. */
    NEWTON("\u00b0N", "Newton", c -> c * 33.0 / 100.0, n -> n * 100.0 / 33.0),

    /** The obsolete Delisle scale, which runs backwards: hotter is a smaller number. */
    DELISLE("\u00b0De", "Delisle", c -> (100.0 - c) * 1.5, d -> 100.0 - d / 1.5),

    /** The obsolete R\u00f8mer scale. */
    ROMER("\u00b0Ro", "R\u00f8mer", c -> c * 21.0 / 40.0 + 7.5, r -> (r - 7.5) * 40.0 / 21.0);

    /** The Celsius value of absolute zero, the physical floor of the scale. */
    public static final double ABSOLUTE_ZERO_CELSIUS = -273.15;

    private final String symbol;
    private final String displayName;
    private final UnitConverter fromCelsius;
    private final UnitConverter toCelsius;

    TemperatureUnit(String symbol, String displayName, UnitConverter fromCelsius, UnitConverter toCelsius) {
        this.symbol = symbol;
        this.displayName = displayName;
        this.fromCelsius = fromCelsius;
        this.toCelsius = toCelsius;
    }

    /**
     * Converts a magnitude expressed in this unit into degrees Celsius.
     *
     * @param value a magnitude in this unit
     * @return the equivalent Celsius magnitude
     */
    public double toCelsius(double value) {
        return toCelsius.convert(value);
    }

    /**
     * Converts a Celsius magnitude into this unit.
     *
     * @param celsius a magnitude in degrees Celsius
     * @return the equivalent magnitude in this unit
     */
    public double fromCelsius(double celsius) {
        return fromCelsius.convert(celsius);
    }

    /**
     * Converts a magnitude from this unit into another.
     *
     * @param value a magnitude in this unit
     * @param target the unit to express the result in
     * @return the equivalent magnitude in {@code target}
     */
    public double convert(double value, TemperatureUnit target) {
        return target == this ? value : target.fromCelsius(toCelsius(value));
    }

    /** @return the short glyph appended to a rendered reading, e.g. {@code °C}. */
    public String symbol() {
        return symbol;
    }

    /** @return the human-readable name, used in menus and documentation. */
    public String displayName() {
        return displayName;
    }

    /**
     * Parses a unit by its enum name or its display name, case-insensitively.
     *
     * @param raw the text to parse; may be {@code null}
     * @return the matching unit, or empty if nothing matched
     */
    public static Optional<TemperatureUnit> parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        String needle = raw.trim().toUpperCase(Locale.ROOT);
        for (TemperatureUnit unit : values()) {
            if (unit.name().equals(needle) || unit.displayName.toUpperCase(Locale.ROOT).equals(needle)) {
                return Optional.of(unit);
            }
        }
        return Optional.empty();
    }
}
