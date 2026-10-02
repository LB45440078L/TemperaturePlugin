package top.cmarco.temperature.core.service;

import java.util.HashMap;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.UUID;
import top.cmarco.temperature.core.environment.ThermalEnvironment;
import top.cmarco.temperature.core.physics.AmbientState;
import top.cmarco.temperature.core.physics.TemperatureModel;

/**
 * Holds every player's drifting body temperature and advances it each step.
 *
 * <p>The model is pure; this is where the only mutable state lives. Each player keeps the
 * temperature they currently feel and the instant it was last advanced, so a step relaxes it by the
 * <em>real</em> elapsed time rather than assuming a fixed tick cadence — a server that stalls for a
 * second, or a player who joins mid-tick, both get the right answer because the integrator takes a
 * time delta.
 *
 * <p>Correctness note carried over from the original: the value stored here is the body temperature
 * in Celsius. The legacy plugin interpolated in whatever unit the player had selected, so two
 * players side by side converged at different physical rates. Interpolating in the canonical scale
 * and converting only at display time removes that.
 *
 * <p>Not thread-safe; the adapter must call it from the server thread.
 */
public final class TemperatureEngine {

    private final TemperatureModel model;
    private final Map<UUID, Body> bodies = new HashMap<>();

    /**
     * @param model the stateless thermodynamic model to drive
     */
    public TemperatureEngine(TemperatureModel model) {
        this.model = model;
    }

    /**
     * Advances a player's body temperature and returns the full reading.
     *
     * @param playerId       the player's id
     * @param environment    the player's measured surroundings
     * @param nowEpochMillis the current instant, in epoch milliseconds
     * @return the reading after this step
     */
    public TemperatureReading update(UUID playerId, ThermalEnvironment environment, long nowEpochMillis) {
        AmbientState ambient = model.ambient(environment, nowEpochMillis);
        Body previous = bodies.get(playerId);

        double deltaSeconds = previous == null
                ? 0.0
                : (nowEpochMillis - previous.updatedAtMillis()) / 1000.0;
        double current = previous == null
                ? model.settings().initialBodyTemperatureCelsius()
                : previous.celsius();

        double next = model.relax(current, ambient.apparentCelsius(), deltaSeconds);
        bodies.put(playerId, new Body(next, nowEpochMillis));

        return new TemperatureReading(playerId, next, ambient, nowEpochMillis);
    }

    /**
     * Overrides a player's stored body temperature.
     *
     * <p>Used after an event listener has edited or cancelled a reading: the step has already been
     * computed and stored optimistically, and this writes back the value that should actually stand.
     *
     * @param playerId       the player's id
     * @param celsius        the body temperature to store, in degrees Celsius
     * @param nowEpochMillis the instant the value is being set
     */
    public void setBody(UUID playerId, double celsius, long nowEpochMillis) {
        bodies.put(playerId, new Body(celsius, nowEpochMillis));
    }

    /**
     * @return a copy of every tracked body temperature, keyed by player, in degrees Celsius
     */
    public java.util.Map<UUID, Double> snapshotBodies() {
        java.util.Map<UUID, Double> snapshot = new java.util.HashMap<>(bodies.size());
        bodies.forEach((id, body) -> snapshot.put(id, body.celsius()));
        return snapshot;
    }

    /**
     * Restores body temperatures captured by {@link #snapshotBodies()}, so a configuration reload
     * does not reset every player to the initial temperature.
     *
     * @param snapshot        body temperatures keyed by player
     * @param nowEpochMillis  the instant to stamp the restored values with
     */
    public void restoreBodies(java.util.Map<UUID, Double> snapshot, long nowEpochMillis) {
        snapshot.forEach((id, celsius) -> bodies.put(id, new Body(celsius, nowEpochMillis)));
    }

    /**
     * @param playerId the player's id
     * @return the player's last body temperature, or empty if they have never been simulated
     */
    public OptionalDouble bodyTemperature(UUID playerId) {
        Body body = bodies.get(playerId);
        return body == null ? OptionalDouble.empty() : OptionalDouble.of(body.celsius());
    }

    /**
     * Forgets a player, so that a future reading starts from the initial body temperature again.
     *
     * @param playerId the player's id
     */
    public void forget(UUID playerId) {
        bodies.remove(playerId);
    }

    /** Forgets every player, for a clean reload. */
    public void clear() {
        bodies.clear();
    }

    /** @return how many players are currently tracked. */
    public int tracked() {
        return bodies.size();
    }

    /** @return the model this engine drives. */
    public TemperatureModel model() {
        return model;
    }

    private record Body(double celsius, long updatedAtMillis) {
    }
}
