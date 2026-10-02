package top.cmarco.temperature.core.physics.modifier;

import top.cmarco.temperature.core.config.TemperatureSettings;
import top.cmarco.temperature.core.physics.TemperatureModifier;
import top.cmarco.temperature.core.physics.ThermalContext;

/**
 * Cools a player whose body is in water.
 *
 * <p>A new addition: the original plugin let a player stand in an ocean at no thermal cost. Water
 * has a large heat capacity, so immersion is modelled as a flat reduction while submerged, with a
 * switch to disable it. A more elaborate model would meter it against the water's own temperature;
 * the flat term is the honest first approximation and is documented as such.
 */
public final class ImmersionModifier implements TemperatureModifier {

    private final boolean enabled;
    private final double coolingCelsius;

    /**
     * @param settings the tunables supplying the switch and the magnitude
     */
    public ImmersionModifier(TemperatureSettings settings) {
        this.enabled = settings.immersionEnabled();
        this.coolingCelsius = settings.immersionCoolingCelsius();
    }

    @Override
    public double additiveContribution(ThermalContext context) {
        return enabled && context.environment().submerged() ? -coolingCelsius : 0.0;
    }

    @Override
    public String name() {
        return "immersion";
    }
}
