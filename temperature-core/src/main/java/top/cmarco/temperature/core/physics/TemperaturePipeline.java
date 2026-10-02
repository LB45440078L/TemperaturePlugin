package top.cmarco.temperature.core.physics;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * The ordered set of {@link TemperatureModifier}s that together produce an air temperature.
 *
 * <p>Additive terms are summed first, then every factor is applied, so the result is independent of
 * registration order. The class keeps its members visible both for evaluation and — through
 * {@link #breakdown} — for the debug command, which needs to show a moderator <em>why</em> a reading
 * came out the way it did rather than just the number.
 */
public final class TemperaturePipeline {

    private final List<TemperatureModifier> modifiers;

    /**
     * @param modifiers the modifiers to compose
     */
    public TemperaturePipeline(List<TemperatureModifier> modifiers) {
        this.modifiers = List.copyOf(Objects.requireNonNull(modifiers, "modifiers"));
    }

    /**
     * Evaluates the dry-bulb air temperature.
     *
     * @param context the reading being computed
     * @return the air temperature in degrees Celsius, additive terms summed and factors applied
     */
    public double airTemperatureCelsius(ThermalContext context) {
        double additive = 0.0;
        for (TemperatureModifier modifier : modifiers) {
            additive += modifier.additiveContribution(context);
        }
        double factor = 1.0;
        for (TemperatureModifier modifier : modifiers) {
            factor *= modifier.multiplicativeFactor(context, additive);
        }
        return additive * factor;
    }

    /**
     * @param context the reading being computed
     * @return each modifier's additive contribution, keyed by the modifier's name, in order
     */
    public Map<String, Double> breakdown(ThermalContext context) {
        Map<String, Double> contributions = new LinkedHashMap<>();
        for (TemperatureModifier modifier : modifiers) {
            contributions.put(modifier.name(), modifier.additiveContribution(context));
        }
        return contributions;
    }

    /** @return the modifiers, in registration order. */
    public List<TemperatureModifier> modifiers() {
        return modifiers;
    }
}
