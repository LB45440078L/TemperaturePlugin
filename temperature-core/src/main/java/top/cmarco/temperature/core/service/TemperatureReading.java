package top.cmarco.temperature.core.service;

import java.util.UUID;
import top.cmarco.temperature.core.physics.AmbientState;

/**
 * The full result of one simulation step for one player.
 *
 * @param playerId            the player this reading belongs to
 * @param bodyCelsius         the player's drifting felt temperature, in degrees Celsius
 * @param ambient             the ambient state the body is drifting towards
 * @param timestampEpochMillis the instant of the reading, in epoch milliseconds
 */
public record TemperatureReading(
        UUID playerId,
        double bodyCelsius,
        AmbientState ambient,
        long timestampEpochMillis) {
}
