package top.cmarco.temperature.core.environment;

/**
 * The kinds of block that measurably move the air temperature around a player.
 *
 * <p>Each kind carries two numbers that define its contribution to the thermal field:
 *
 * <ul>
 *   <li>{@link #sign()} — whether the block heats ({@code +1}) or cools ({@code -1}) the air.</li>
 *   <li>{@link #quench()} — a softening length, in blocks squared. A point source would diverge as
 *       the player stands on it; the quench term caps that divergence and doubles as the material's
 *       "how close do you have to be" knob. Small quench means the effect is concentrated at short
 *       range (lava), large quench means it is gentle but reaches further (a snowfield).</li>
 * </ul>
 *
 * <p>The quench constants preserve the values tuned in the original plugin so existing servers feel
 * the same, but they now live in one named table instead of five loosely related material sets.
 */
public enum ThermalEmitter {

    /** A lit furnace or blast furnace. */
    FURNACE(1, 0.001),

    /** Lava and magma. The strongest common heat source. */
    LAVA(1, 0.00001),

    /** A campfire or soul campfire. */
    CAMPFIRE(1, 0.17),

    /** Actual fire, which the original plugin ignored. */
    FIRE(1, 0.02),

    /** Ice of any kind. */
    ICE(-1, 0.00015),

    /** Snow of any kind, including powder snow. */
    SNOW(-1, 0.15);

    private final int sign;
    private final double quench;

    ThermalEmitter(int sign, double quench) {
        this.sign = sign;
        this.quench = quench;
    }

    /** @return {@code +1} for a heat source, {@code -1} for a cold source. */
    public int sign() {
        return sign;
    }

    /** @return the divergence-softening constant, in blocks squared. */
    public double quench() {
        return quench;
    }

    /** @return {@code true} if this block heats its surroundings. */
    public boolean isHeating() {
        return sign > 0;
    }
}
