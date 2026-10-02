package top.cmarco.temperature.core.physics.modifier;

import top.cmarco.temperature.core.physics.TemperatureModifier;
import top.cmarco.temperature.core.physics.ThermalContext;

/**
 * Applies the seasonal offset, blended continuously between adjacent seasons.
 *
 * <p>Reads the calendar from the context rather than holding one, so the same modifier instance
 * serves every world — each world's game time is carried in its own environment.
 */
public final class SeasonalModifier implements TemperatureModifier {

    @Override
    public double additiveContribution(ThermalContext context) {
        return context.seasonCycle().seasonalOffsetCelsius(
                context.environment().gameTime(), context.settings());
    }

    @Override
    public String name() {
        return "season";
    }
}
