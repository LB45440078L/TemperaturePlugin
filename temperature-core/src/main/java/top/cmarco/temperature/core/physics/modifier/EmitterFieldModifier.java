package top.cmarco.temperature.core.physics.modifier;

import top.cmarco.temperature.core.physics.EmitterField;
import top.cmarco.temperature.core.physics.TemperatureModifier;
import top.cmarco.temperature.core.physics.ThermalContext;

/**
 * Adds the thermal field cast by nearby blocks — fires, lava, ice and snow.
 *
 * <p>The arithmetic lives in {@link EmitterField} so it can be tested in isolation; this modifier
 * is only the adapter that pulls the environment out of the context.
 */
public final class EmitterFieldModifier implements TemperatureModifier {

    private final EmitterField emitterField;

    /**
     * @param emitterField the pre-computed field, holding the configured influence radius
     */
    public EmitterFieldModifier(EmitterField emitterField) {
        this.emitterField = emitterField;
    }

    @Override
    public double additiveContribution(ThermalContext context) {
        return emitterField.contribution(context.environment());
    }

    @Override
    public String name() {
        return "emitters";
    }
}
