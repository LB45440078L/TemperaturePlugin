package top.cmarco.temperature.core.physics;

import top.cmarco.temperature.core.climate.Season;
import top.cmarco.temperature.core.climate.SeasonCycle;
import top.cmarco.temperature.core.config.TemperatureSettings;
import top.cmarco.temperature.core.environment.ThermalEnvironment;

/**
 * Everything a {@link TemperatureModifier} may read while contributing to a reading.
 *
 * <p>Derived values that must be consistent across modifiers — the resolved humidity, the season —
 * are computed once and carried here rather than recomputed per modifier. The wall-clock timestamp
 * is part of the context, not the environment, because it describes "when we are evaluating" rather
 * than "what the world looks like"; that keeps the environment a pure function of world state and
 * makes timed effects testable by moving a fake clock.
 *
 * @param environment      the measured surroundings
 * @param settings         the active tunables
 * @param seasonCycle      the calendar in force
 * @param humidityPercent  the resolved relative humidity, in percent
 * @param season           the season at the sample
 * @param nowEpochMillis   the instant the reading is taken, in epoch milliseconds
 */
public record ThermalContext(
        ThermalEnvironment environment,
        TemperatureSettings settings,
        SeasonCycle seasonCycle,
        double humidityPercent,
        Season season,
        long nowEpochMillis) {
}
