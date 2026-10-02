package top.cmarco.temperature.core.physics;

/**
 * One term in the air-temperature budget.
 *
 * <p>The pipeline composes a set of these. Each modifier may contribute in two ways:
 *
 * <ul>
 *   <li>an <strong>additive</strong> amount, in degrees Celsius — a heat source, a season, a
 *       piece of clothing. These are summed into the dry-bulb air temperature.</li>
 *   <li>a <strong>multiplicative</strong> factor, dimensionless, with 1.0 meaning "no change" —
 *       rain scrubbing the heat off the air, for example. Factors are applied to the summed
 *       temperature, and a factor may inspect that running temperature (rain only cools air that is
 *       already warm).</li>
 * </ul>
 *
 * <p>Splitting the two is deliberate. Adding before multiplying keeps the result independent of the
 * order in which modifiers were registered, which a single "apply, return new temperature" signature
 * would not.
 */
public interface TemperatureModifier {

    /**
     * @param context the reading being computed
     * @return the amount to add to the dry-bulb air temperature, in degrees Celsius
     */
    default double additiveContribution(ThermalContext context) {
        return 0.0;
    }

    /**
     * @param context     the reading being computed
     * @param airCelsius  the dry-bulb temperature after every additive term has been summed
     * @return a factor to multiply that temperature by; 1.0 leaves it unchanged
     */
    default double multiplicativeFactor(ThermalContext context, double airCelsius) {
        return 1.0;
    }

    /** @return a stable short label, used in diagnostics and debug output. */
    default String name() {
        return getClass().getSimpleName();
    }
}
