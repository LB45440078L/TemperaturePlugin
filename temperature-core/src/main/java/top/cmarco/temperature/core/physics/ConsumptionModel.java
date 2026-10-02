package top.cmarco.temperature.core.physics;

import top.cmarco.temperature.core.config.TemperatureSettings;

/**
 * The cooling curve of a drink of water.
 *
 * <p>Drinking water does not cool instantly and then stop; the effect ramps up as it takes hold and
 * then fades. The original plugin modelled this with an inverted parabola in the minutes since the
 * drink:
 *
 * <pre>{@code
 *   cooling(t) = peak - (factor * t - offset)^2
 * }</pre>
 *
 * <p>with {@code factor = 0.6}, {@code offset = 4.5} and {@code peak = 20}, so cooling is greatest
 * at {@code t = 7.5} minutes and reaches zero at about {@code t = 15} minutes. That is kept, with
 * the coefficients exposed as tunables and the result floored at zero (the raw parabola dips a
 * fraction of a degree below zero at {@code t = 0}, which would silently <em>warm</em> a player who
 * had just drunk).
 */
public final class ConsumptionModel {

    private final double factor;
    private final double offset;
    private final double peakCelsius;
    private final int durationMinutes;

    /**
     * @param settings the tunables supplying the curve shape and its duration
     */
    public ConsumptionModel(TemperatureSettings settings) {
        this.factor = settings.waterCoolingFactor();
        this.offset = settings.waterCoolingOffset();
        this.peakCelsius = settings.waterCoolingPeakCelsius();
        this.durationMinutes = settings.waterEffectDurationMinutes();
    }

    /**
     * @param minutesSinceConsumption minutes elapsed since the drink; negative clamps to zero
     * @return the cooling still in effect, in degrees Celsius, never negative
     */
    public double waterCoolingCelsius(double minutesSinceConsumption) {
        double t = Math.max(0.0, minutesSinceConsumption);
        double shaped = factor * t - offset;
        return Math.max(0.0, peakCelsius - shaped * shaped);
    }

    /** @return how long, in minutes, a drink of water keeps cooling the player. */
    public int durationMinutes() {
        return durationMinutes;
    }
}
