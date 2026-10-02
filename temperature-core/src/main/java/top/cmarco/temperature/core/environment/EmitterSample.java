package top.cmarco.temperature.core.environment;

/**
 * One emitter observed near a player, reduced to the only quantity the field needs.
 *
 * <p>The squared distance is used rather than the distance itself because the field is a function
 * of {@code d^2}, and the sampler already has the squared value from
 * {@code Location#distanceSquared}; taking a square root only to square it again would be a wasted
 * call per block per player per tick.
 *
 * @param emitter           the kind of block
 * @param distanceSquared   the squared distance from the player to the block, in blocks squared
 * @param active            whether the emitter is actually emitting (a cold furnace is not a heat
 *                          source); the sampler decides this, the field does not care why
 */
public record EmitterSample(ThermalEmitter emitter, double distanceSquared, boolean active) {

    public EmitterSample {
        if (distanceSquared < 0.0) {
            throw new IllegalArgumentException("distanceSquared must not be negative");
        }
    }

    /** @return a convenience sample that is always emitting. */
    public static EmitterSample of(ThermalEmitter emitter, double distanceSquared) {
        return new EmitterSample(emitter, distanceSquared, true);
    }
}
