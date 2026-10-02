package top.cmarco.temperature.core.physics;

import top.cmarco.temperature.core.climate.Season;

/**
 * The instantaneous reading of one point in the world, before any body has been considered.
 *
 * <p>"Ambient" is the air itself: what a thermometer would read ({@code airCelsius}), what the air
 * would feel like on bare skin ({@code apparentCelsius}), and the two quantities used to get from
 * one to the other. The player's own drifting body temperature is a separate concern, carried by the
 * engine.
 *
 * @param airCelsius      the dry-bulb air temperature, in degrees Celsius
 * @param apparentCelsius the apparent temperature (heat index, wind chill or dry-bulb), in Celsius
 * @param humidityPercent the relative humidity, in percent
 * @param windSpeedKmh    the estimated wind speed, in kilometres per hour
 * @param season          the season at the sample
 */
public record AmbientState(
        double airCelsius,
        double apparentCelsius,
        double humidityPercent,
        double windSpeedKmh,
        Season season) {
}
