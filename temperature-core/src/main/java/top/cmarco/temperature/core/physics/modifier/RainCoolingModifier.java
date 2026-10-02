package top.cmarco.temperature.core.physics.modifier;

import top.cmarco.temperature.core.config.TemperatureSettings;
import top.cmarco.temperature.core.physics.TemperatureModifier;
import top.cmarco.temperature.core.physics.ThermalContext;

/**
 * Scrubs heat out of warm air when it is raining.
 *
 * <p>The only multiplicative term, and the reason the modifier interface exposes factors: rain does
 * not remove a fixed number of degrees, it removes a fraction of the heat that is present. The
 * original code did the same thing ({@code temperature *= 0.725} in a storm) but only above a
 * threshold; this keeps the threshold and expresses the rest as a factor.
 */
public final class RainCoolingModifier implements TemperatureModifier {

    private final double factor;
    private final double thresholdCelsius;

    /**
     * @param settings the tunables supplying the factor and the threshold it applies above
     */
    public RainCoolingModifier(TemperatureSettings settings) {
        this.factor = settings.rainCoolingFactor();
        this.thresholdCelsius = settings.rainCoolingThresholdCelsius();
    }

    @Override
    public double multiplicativeFactor(ThermalContext context, double airCelsius) {
        if (context.environment().precipitating() && airCelsius > thresholdCelsius) {
            return factor;
        }
        return 1.0;
    }

    @Override
    public String name() {
        return "rain";
    }
}
