package top.cmarco.temperature.core.physics;

import top.cmarco.temperature.core.config.TemperatureSettings;

/**
 * Brings a body towards the ambient apparent temperature over time.
 *
 * <p>A player does not snap to the air temperature the instant they step outside; they drift
 * towards it. The body is modelled as a first-order system relaxing exponentially towards the
 * target with time constant {@code tau}:
 *
 * <pre>{@code
 *   dT/dt = (T_target - T) / tau
 *   T(t + dt) = T_target + (T - T_target) * exp(-dt / tau)
 * }</pre>
 *
 * <p>The closed-form step is used rather than an Euler increment because it is unconditionally
 * stable and its result is independent of how finely time is chopped. The original plugin used an
 * explicit Euler step, {@code T + (target - T) * 0.011} per update, which is that same relaxation
 * only when the update interval is fixed and {@code tau} is large; the equivalence, and the value
 * of {@code tau} that reproduces it, are derived in {@code docs/physics_model.md}.
 *
 * <p>{@code tau} is the time to close about 63% of the gap; after {@code 3*tau} the body is within
 * 5% of the target.
 */
public final class Thermoregulator {

    private final double timeConstantSeconds;
    private final double minCelsius;
    private final double maxCelsius;

    /**
     * @param settings the tunables supplying the time constant and the clamp bounds
     */
    public Thermoregulator(TemperatureSettings settings) {
        this.timeConstantSeconds = settings.thermoregulationTimeConstantSeconds();
        this.minCelsius = settings.minBodyTemperatureCelsius();
        this.maxCelsius = settings.maxBodyTemperatureCelsius();
    }

    /**
     * Advances a body temperature by one step.
     *
     * @param currentCelsius the body temperature at the start of the step
     * @param targetCelsius  the ambient apparent temperature being approached
     * @param deltaSeconds   the length of the step; non-positive values return the input unchanged
     * @return the body temperature after the step, clamped to the configured bounds
     */
    public double relax(double currentCelsius, double targetCelsius, double deltaSeconds) {
        if (deltaSeconds <= 0.0) {
            return clamp(currentCelsius);
        }
        double decay = Math.exp(-deltaSeconds / timeConstantSeconds);
        double next = targetCelsius + (currentCelsius - targetCelsius) * decay;
        return clamp(next);
    }

    /** @return the relaxation time constant, in seconds. */
    public double timeConstantSeconds() {
        return timeConstantSeconds;
    }

    private double clamp(double value) {
        return value < minCelsius ? minCelsius : Math.min(value, maxCelsius);
    }
}
