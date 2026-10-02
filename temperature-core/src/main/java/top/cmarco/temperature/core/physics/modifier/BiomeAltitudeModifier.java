package top.cmarco.temperature.core.physics.modifier;

import top.cmarco.temperature.core.climate.AltitudeModel;
import top.cmarco.temperature.core.physics.TemperatureModifier;
import top.cmarco.temperature.core.physics.ThermalContext;

/**
 * The baseline term: the air temperature of the biome, adjusted for height.
 *
 * <p>Every other modifier is a perturbation on top of this one. It is the only additive term that
 * is present for every reading, which is why it comes first in the pipeline even though the pipeline
 * does not depend on order.
 */
public final class BiomeAltitudeModifier implements TemperatureModifier {

    private final AltitudeModel altitudeModel;

    /**
     * @param altitudeModel the vertical profile to evaluate
     */
    public BiomeAltitudeModifier(AltitudeModel altitudeModel) {
        this.altitudeModel = altitudeModel;
    }

    @Override
    public double additiveContribution(ThermalContext context) {
        var environment = context.environment();
        return altitudeModel.temperatureAt(
                environment.climate(),
                environment.altitude(),
                environment.worldMinY(),
                environment.worldMaxY());
    }

    @Override
    public String name() {
        return "biome-altitude";
    }
}
