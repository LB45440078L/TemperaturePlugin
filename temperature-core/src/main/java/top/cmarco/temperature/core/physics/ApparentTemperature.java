package top.cmarco.temperature.core.physics;

import top.cmarco.temperature.core.config.TemperatureSettings;

/**
 * Converts a dry-bulb air temperature into what the player actually feels.
 *
 * <p>Two well-established empirical regressions are used, each inside its own validity domain, with
 * the dry-bulb temperature as the fallback:
 *
 * <ul>
 *   <li>The <strong>heat index</strong> (Rothfusz regression, metric form) when the air is warm and
 *       humid — warmth feels hotter when sweat cannot evaporate.</li>
 *   <li>The <strong>wind chill</strong> (Environment Canada metric form) when the air is cold and
 *       windy — cold feels colder when moving air strips the boundary layer.</li>
 * </ul>
 *
 * <p>The original plugin evaluated only the heat-index polynomial, everywhere, with no domain guard
 * — including for freezing temperatures, where the polynomial is meaningless and produced negative
 * "perceived" values. Gating each formula to the range it was fitted for is the single largest
 * correctness improvement in this file. Both regressions and their domains are worked through in
 * {@code docs/physics_model.md}.
 */
public final class ApparentTemperature {

    private final boolean heatIndexEnabled;
    private final double heatIndexMinCelsius;
    private final double heatIndexMaxCelsius;
    private final boolean windChillEnabled;
    private final double windChillThresholdCelsius;
    private final double windChillMinWindKmh;

    /**
     * @param settings the tunables gating each regression
     */
    public ApparentTemperature(TemperatureSettings settings) {
        this.heatIndexEnabled = settings.heatIndexEnabled();
        this.heatIndexMinCelsius = settings.heatIndexMinTemperatureCelsius();
        this.heatIndexMaxCelsius = settings.heatIndexMaxTemperatureCelsius();
        this.windChillEnabled = settings.windChillEnabled();
        this.windChillThresholdCelsius = settings.windChillThresholdCelsius();
        this.windChillMinWindKmh = settings.windChillMinWindKmh();
    }

    /**
     * @param dryBulbCelsius   the air temperature, in degrees Celsius
     * @param humidityPercent  the relative humidity, in percent
     * @param windSpeedKmh     the wind speed, in kilometres per hour
     * @return the apparent temperature in degrees Celsius
     */
    public double estimate(double dryBulbCelsius, double humidityPercent, double windSpeedKmh) {
        if (heatIndexEnabled
                && dryBulbCelsius >= heatIndexMinCelsius
                && dryBulbCelsius <= heatIndexMaxCelsius
                && humidityPercent >= 40.0) {
            return heatIndex(dryBulbCelsius, humidityPercent);
        }
        if (windChillEnabled && dryBulbCelsius <= windChillThresholdCelsius
                && windSpeedKmh >= windChillMinWindKmh) {
            return Math.min(dryBulbCelsius, windChill(dryBulbCelsius, windSpeedKmh));
        }
        return dryBulbCelsius;
    }

    /**
     * The metric Rothfusz heat-index regression.
     *
     * <p>Valid for a dry-bulb temperature above roughly 27&nbsp;&deg;C and relative humidity above
     * 40%. Callers should not invoke it outside those bounds; {@link #estimate} gates it.</p>
     *
     * @param t the dry-bulb temperature, in degrees Celsius
     * @param r the relative humidity, in percent
     * @return the heat index, in degrees Celsius
     */
    public static double heatIndex(double t, double r) {
        double t2 = t * t;
        double r2 = r * r;
        return -8.78469475556
                + 1.61139411 * t
                + 2.33854883889 * r
                + -0.14611605 * t * r
                + -0.012308094 * t2
                + -0.0164248277778 * r2
                + 0.002211732 * t2 * r
                + 0.00072546 * t * r2
                + -0.000003582 * t2 * r2;
    }

    /**
     * The Environment Canada wind-chill formula.
     *
     * <p>Valid for an air temperature at or below 10&nbsp;&deg;C and a wind speed of at least
     * 4.8&nbsp;km/h; outside that it has no meaning.</p>
     *
     * @param t the air temperature, in degrees Celsius
     * @param v the wind speed, in kilometres per hour
     * @return the wind-chill temperature, in degrees Celsius
     */
    public static double windChill(double t, double v) {
        double vPow = Math.pow(v, 0.16);
        return 13.12 + 0.6215 * t - 11.37 * vPow + 0.3965 * t * vPow;
    }
}
