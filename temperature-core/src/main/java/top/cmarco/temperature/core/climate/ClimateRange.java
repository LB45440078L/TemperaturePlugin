package top.cmarco.temperature.core.climate;

/**
 * An inclusive band of temperature and relative humidity for a climate family.
 *
 * <p>Ranges rather than single values are used because a biome is not one temperature: the
 * {@link AltitudeModel} and the {@link HumidityModel} move a reading within the band. The
 * temperature band is expressed in degrees Celsius and the humidity band in percent (0–100).
 *
 * @param minTemperatureCelsius the coolest the family's air gets before other effects
 * @param maxTemperatureCelsius the warmest the family's air gets before other effects
 * @param minHumidityPercent   the relative humidity the family never drops below
 * @param maxHumidityPercent   the relative humidity the family never rises above
 */
public record ClimateRange(
        double minTemperatureCelsius,
        double maxTemperatureCelsius,
        double minHumidityPercent,
        double maxHumidityPercent) {

    public ClimateRange {
        if (minTemperatureCelsius > maxTemperatureCelsius) {
            throw new IllegalArgumentException(
                    "minTemperatureCelsius (" + minTemperatureCelsius + ") exceeds max ("
                            + maxTemperatureCelsius + ')');
        }
        if (minHumidityPercent > maxHumidityPercent) {
            throw new IllegalArgumentException(
                    "minHumidityPercent (" + minHumidityPercent + ") exceeds max ("
                            + maxHumidityPercent + ')');
        }
        if (minHumidityPercent < 0.0 || maxHumidityPercent > 100.0) {
            throw new IllegalArgumentException("humidity must lie within [0, 100]");
        }
        if (minTemperatureCelsius < -273.15) {
            throw new IllegalArgumentException("temperature below absolute zero");
        }
    }

    /** @return the width of the temperature band, in degrees Celsius. */
    public double temperatureSpan() {
        return maxTemperatureCelsius - minTemperatureCelsius;
    }

    /** @return the width of the humidity band, in percentage points. */
    public double humiditySpan() {
        return maxHumidityPercent - minHumidityPercent;
    }
}
