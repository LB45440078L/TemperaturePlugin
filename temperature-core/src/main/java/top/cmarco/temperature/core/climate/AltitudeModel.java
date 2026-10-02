package top.cmarco.temperature.core.climate;

import top.cmarco.temperature.core.config.TemperatureSettings;

/**
 * The vertical temperature profile: how the air temperature inside a biome's band varies with
 * height.
 *
 * <p>A Minecraft block is one metre, so the real atmosphere gives us a well-known anchor — the
 * environmental lapse rate, about 6.5&nbsp;&deg;C per kilometre, i.e. per 1000 blocks. Two curves
 * are offered:
 *
 * <ul>
 *   <li>{@link Mode#LINEAR_LAPSE} — the physical model: the reading moves linearly from the warm
 *       end of the band near the world floor to the cool end near the build limit.</li>
 *   <li>{@link Mode#LEGACY_POLYNOMIAL} — the curve the original plugin shipped: a quadratic
 *       fitted so a height of {@code y} yields a "retention" factor. It is retained as the default
 *       only so existing servers keep their familiar feel, and is documented in full in
 *       {@code docs/physics_model.md}.</li>
 * </ul>
 *
 * <p>Both curves return a retention factor in {@code [0, 1]}: 1 keeps the biome at its warm
 * extreme, 0 drops it to its cool extreme. The reading is then
 * {@code min + retention * (max - min)}.
 */
public final class AltitudeModel {

    /** Which vertical curve to evaluate. */
    public enum Mode {
        /** Physically motivated, linear with height. */
        LINEAR_LAPSE,
        /** The original quadratic, kept for continuity with existing servers. */
        LEGACY_POLYNOMIAL
    }

    private final Mode mode;
    private final double lapseFraction;
    private final double polynomialA;
    private final double polynomialB;
    private final double polynomialC;

    private AltitudeModel(Mode mode, double lapseFraction, double a, double b, double c) {
        this.mode = mode;
        this.lapseFraction = lapseFraction;
        this.polynomialA = a;
        this.polynomialB = b;
        this.polynomialC = c;
    }

    /** @return the model configured by {@code settings}. */
    public static AltitudeModel from(TemperatureSettings settings) {
        return new AltitudeModel(
                settings.altitudeMode(),
                settings.lapseFraction(),
                settings.legacyLapseQuadraticA(),
                settings.legacyLapseQuadraticB(),
                settings.legacyLapseQuadraticC());
    }

    /**
     * Evaluates the air temperature for a position.
     *
     * @param climate the biome's climate band
     * @param y       the block's height, in blocks
     * @param minY    the world's lowest buildable height
     * @param maxY    the world's highest buildable height
     * @return the air temperature at that position, in degrees Celsius
     */
    public double temperatureAt(BiomeClimate climate, double y, double minY, double maxY) {
        double retention = retentionFactor(y, minY, maxY);
        ClimateRange range = climate.range();
        return range.minTemperatureCelsius()
                + retention * range.temperatureSpan();
    }

    /**
     * @param y    the block's height
     * @param minY the world's lowest buildable height
     * @param maxY the world's highest buildable height
     * @return the retention factor in {@code [0, 1]}, 1 being the warm extreme
     */
    public double retentionFactor(double y, double minY, double maxY) {
        double height = clamp(y, minY, maxY);
        double retention = switch (mode) {
            case LINEAR_LAPSE -> linearRetention(height, minY, maxY);
            case LEGACY_POLYNOMIAL -> legacyRetention(height);
        };
        return clamp(retention, 0.0, 1.0);
    }

    private double linearRetention(double y, double minY, double maxY) {
        double span = maxY - minY;
        if (span <= 0.0) {
            return 1.0;
        }
        double normalisedHeight = (y - minY) / span;
        return 1.0 - lapseFraction * normalisedHeight;
    }

    private double legacyRetention(double y) {
        return polynomialA * y * y + polynomialB * y + polynomialC;
    }

    private static double clamp(double value, double low, double high) {
        return value < low ? low : Math.min(value, high);
    }
}
