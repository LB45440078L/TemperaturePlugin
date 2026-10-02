package top.cmarco.temperature.plugin.config;

import java.util.List;
import top.cmarco.temperature.core.config.TemperatureSettings;

/**
 * The product of one successful configuration load: the simulation tunables, the display tunables,
 * the worlds to simulate and the two scheduler periods.
 *
 * <p>Bundling them means a reload performs exactly one atomic swap of a single object, so no code
 * can ever observe half of the old and half of the new configuration.
 *
 * @param simulation                 the physics tunables handed to the core
 * @param display                    the action-bar tunables
 * @param worlds                     the names of the worlds to simulate
 * @param updateIntervalTicks        how often to recompute a player's temperature
 * @param seasonCheckIntervalTicks   how often to re-evaluate each world's season
 * @param actionBarIntervalTicks     how often to redraw the action bar
 */
public record LoadedConfiguration(
        TemperatureSettings simulation,
        DisplaySettings display,
        List<String> worlds,
        long updateIntervalTicks,
        long seasonCheckIntervalTicks,
        long actionBarIntervalTicks) {

    public LoadedConfiguration {
        worlds = List.copyOf(worlds);
    }
}
