package top.cmarco.temperature.core.physics.modifier;

import top.cmarco.temperature.core.config.TemperatureSettings;
import top.cmarco.temperature.core.physics.TemperatureModifier;
import top.cmarco.temperature.core.physics.ThermalContext;

/**
 * Adds the insulating value of worn armour.
 *
 * <p>Leather, and only leather, insulates — one configured number of degrees per piece. Whether the
 * player has enough leather to be <em>immune</em> to a whole effect is a threshold decision and
 * lives in the adapter, not here; this modifier only nudges the reading.
 */
public final class ArmorModifier implements TemperatureModifier {

    private final double leatherGainPerPieceCelsius;

    /**
     * @param settings the tunables supplying the per-piece insulation
     */
    public ArmorModifier(TemperatureSettings settings) {
        this.leatherGainPerPieceCelsius = settings.leatherGainPerPieceCelsius();
    }

    @Override
    public double additiveContribution(ThermalContext context) {
        return context.environment().armor().leatherPieces() * leatherGainPerPieceCelsius;
    }

    @Override
    public String name() {
        return "armor";
    }
}
