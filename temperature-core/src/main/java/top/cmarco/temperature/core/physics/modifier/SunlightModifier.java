package top.cmarco.temperature.core.physics.modifier;

import top.cmarco.temperature.core.config.TemperatureSettings;
import top.cmarco.temperature.core.physics.TemperatureModifier;
import top.cmarco.temperature.core.physics.ThermalContext;

/**
 * Adds solar heating to a sample that stands under open sky.
 *
 * <p>The original plugin intended this — it had an {@code hitBySun} bonus — but the test was
 * written as {@code highestBlock.getX() <= block.getX() && highestBlock.getZ() <= block.getZ()},
 * and the "highest block" is always at the very {@code x}/{@code z} being sampled, so the
 * comparison was vacuously true and the bonus applied to everyone, indoors or buried. Here the
 * adapter answers the real question — is there solid ground above the sample? — and this modifier
 * simply honours it.
 */
public final class SunlightModifier implements TemperatureModifier {

    private final double solarGainCelsius;

    /**
     * @param settings the tunables supplying the solar gain
     */
    public SunlightModifier(TemperatureSettings settings) {
        this.solarGainCelsius = settings.solarGainCelsius();
    }

    @Override
    public double additiveContribution(ThermalContext context) {
        return context.environment().exposedToSky() ? solarGainCelsius : 0.0;
    }

    @Override
    public String name() {
        return "sunlight";
    }
}
