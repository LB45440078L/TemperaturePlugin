package top.cmarco.temperature.core.climate;

import top.cmarco.temperature.core.config.TemperatureSettings;

/**
 * The diurnal relative-humidity curve.
 *
 * <p>Relative humidity is modelled as a single bell that peaks in the early afternoon and falls to
 * a floor overnight, scaled into the biome's humidity band. The shape is a Gaussian in the time of
 * day:
 *
 * <pre>{@code
 *   scale(t) = amplitude * exp( -(t - peak)^2 / (2 * sigma^2) ) + floor
 *   humidity = min + scale * (max - min)
 * }</pre>
 *
 * <p>where {@code t} and {@code peak} are fractions of a day. This reproduces the original plugin's
 * shape — which used a 6500-tick peak and a 4700-tick standard deviation — while expressing the
 * two parameters in a form an operator can reason about. Unlike the original, the result is a
 * genuine percentage: the legacy code divided the band by ten before feeding it to a heat-index
 * formula that expects percent, which silently muted the humidity term. See
 * {@code docs/physics_model.md}.
 *
 * <p>Rain raises humidity toward saturation; a caller-supplied jitter term reproduces the noisy
 * character of the original without making the model untestable.
 */
public final class HumidityModel {

    private final double peakFraction;
    private final double standardDeviationFraction;
    private final double amplitude;
    private final double floor;
    private final double precipitationBonus;

    private HumidityModel(double peakFraction, double standardDeviationFraction, double amplitude,
                          double floor, double precipitationBonus) {
        this.peakFraction = peakFraction;
        this.standardDeviationFraction = standardDeviationFraction;
        this.amplitude = amplitude;
        this.floor = floor;
        this.precipitationBonus = precipitationBonus;
    }

    /** @return the model configured by {@code settings}. */
    public static HumidityModel from(TemperatureSettings settings) {
        return new HumidityModel(
                settings.humidityPeakFraction(),
                settings.humidityStandardDeviation(),
                settings.humidityAmplitude(),
                settings.humidityFloor(),
                settings.precipitationHumidityBonus());
    }

    /**
     * Resolves the relative humidity at a moment.
     *
     * @param climate      the biome's climate band
     * @param dayFraction  time of day in {@code [0, 1)}, noon at 0.25
     * @param precipitating whether it is raining or snowing at the sample
     * @param jitter        a caller-supplied noise term, roughly in {@code [0, 1]}; 0 for a clean
     *                      deterministic reading
     * @return the relative humidity in percent, clamped to {@code [0, 100]}
     */
    public double resolve(BiomeClimate climate, double dayFraction, boolean precipitating, double jitter) {
        double scale = diurnalScale(dayFraction);
        ClimateRange range = climate.range();
        double humidity = range.minHumidityPercent() + scale * range.humiditySpan();
        if (precipitating) {
            humidity += precipitationBonus;
        }
        humidity += jitter;
        return clamp(humidity, 0.0, 100.0);
    }

    /**
     * @param dayFraction time of day in {@code [0, 1)}
     * @return the normalised diurnal scale, roughly {@code [0, 1]}
     */
    public double diurnalScale(double dayFraction) {
        double delta = dayFraction - peakFraction;
        return amplitude * Math.exp(-(delta * delta) / (2.0 * standardDeviationFraction * standardDeviationFraction))
                + floor;
    }

    /** @return the fractional time of day at which humidity peaks. */
    public double peakFraction() {
        return peakFraction;
    }

    private static double clamp(double value, double low, double high) {
        return value < low ? low : Math.min(value, high);
    }
}
