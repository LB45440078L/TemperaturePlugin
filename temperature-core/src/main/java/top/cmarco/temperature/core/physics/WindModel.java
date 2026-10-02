package top.cmarco.temperature.core.physics;

import top.cmarco.temperature.core.config.TemperatureSettings;
import top.cmarco.temperature.core.environment.ThermalEnvironment;

/**
 * A simple wind model, sufficient to make wind chill meaningful.
 *
 * <p>The plugin has no notion of real weather simulation, so wind is assembled from the two things
 * it can actually observe: whether a storm is running, and whether the player is under open sky. A
 * player in a cave is sheltered; a player on a hill in the rain is not.
 *
 * <pre>{@code
 *   wind = base + (storming ? stormBonus : 0) + (exposed ? exposureBonus : 0)
 * }</pre>
 *
 * <p>Speeds are in kilometres per hour, the unit the wind-chill formula expects.
 */
public final class WindModel {

    private final double baseKmh;
    private final double stormBonusKmh;
    private final double exposureBonusKmh;

    /**
     * @param settings the tunables supplying the three wind terms
     */
    public WindModel(TemperatureSettings settings) {
        this.baseKmh = settings.windBaseKmh();
        this.stormBonusKmh = settings.windStormBonusKmh();
        this.exposureBonusKmh = settings.windExposureBonusKmh();
    }

    /**
     * @param environment the measured surroundings
     * @return the estimated wind speed at the player, in kilometres per hour
     */
    public double windSpeedKmh(ThermalEnvironment environment) {
        double wind = baseKmh;
        if (environment.precipitating()) {
            wind += stormBonusKmh;
        }
        if (environment.exposedToSky()) {
            wind += exposureBonusKmh;
        }
        return wind;
    }
}
