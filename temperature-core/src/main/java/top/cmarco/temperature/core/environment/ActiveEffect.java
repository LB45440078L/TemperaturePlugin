package top.cmarco.temperature.core.environment;

/**
 * A temporary additive temperature effect that expires on wall-clock time.
 *
 * <p>Covers both the cooling left behind by a drink of water and the arbitrary heat or cold a
 * plugin can attach to a consumable through the item-NBT tags. Modelling both as one record keeps
 * the modifier that consumes them trivial and means a new timed effect is a new producer, not a new
 * branch in the physics.
 *
 * @param deltaCelsius          the contribution while active; negative cools
 * @param expiresAtEpochMillis  the instant, in epoch milliseconds, after which the effect is gone
 * @param source                a short label used in diagnostics and the debug command
 */
public record ActiveEffect(double deltaCelsius, long expiresAtEpochMillis, String source) {

    /** @return {@code true} if this effect should still be applied at {@code nowMillis}. */
    public boolean isActiveAt(long nowMillis) {
        return nowMillis < expiresAtEpochMillis;
    }
}
