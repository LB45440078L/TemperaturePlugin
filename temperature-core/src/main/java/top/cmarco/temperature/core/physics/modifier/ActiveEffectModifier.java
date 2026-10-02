package top.cmarco.temperature.core.physics.modifier;

import top.cmarco.temperature.core.environment.ActiveEffect;
import top.cmarco.temperature.core.physics.TemperatureModifier;
import top.cmarco.temperature.core.physics.ThermalContext;

/**
 * Sums the timed effects currently in force — leftover cooling from a drink, or a heat/cold a plugin
 * attached to a consumable.
 *
 * <p>Effects whose instant has passed are ignored; the adapter prunes them. Keeping the expiry test
 * here as well means a stale effect can never influence a reading even if the adapter is late to
 * clean up.
 */
public final class ActiveEffectModifier implements TemperatureModifier {

    @Override
    public double additiveContribution(ThermalContext context) {
        long now = context.nowEpochMillis();
        double total = 0.0;
        for (ActiveEffect effect : context.environment().activeEffects()) {
            if (effect.isActiveAt(now)) {
                total += effect.deltaCelsius();
            }
        }
        return total;
    }

    @Override
    public String name() {
        return "active-effects";
    }
}
