package top.cmarco.temperature.core.physics;

import java.util.Comparator;
import java.util.List;
import top.cmarco.temperature.core.config.TemperatureSettings;
import top.cmarco.temperature.core.environment.EmitterSample;
import top.cmarco.temperature.core.environment.ThermalEnvironment;
import top.cmarco.temperature.core.environment.ThermalEmitter;

/**
 * The thermal field cast by nearby blocks.
 *
 * <p>Each emitter contributes a logarithmic potential that decays with distance and reaches exactly
 * zero at the influence radius {@code R}:
 *
 * <pre>{@code
 *   contribution(d) = ln( R^2 / (d^2 + quench) )
 * }</pre>
 *
 * <p>This is the original plugin's expression — it used {@code 3.218905 - ln(d^2 + quench)}, and
 * {@code e^3.218905 = 25 = 5^2} — rewritten so the constant that governs the range is visible as
 * {@code R} instead of hidden inside an exponent. The {@code quench} term keeps the potential finite
 * as the player stands on the block; see {@code docs/physics_model.md} for the derivation and the
 * per-material constants.
 *
 * <p>Two deliberate differences from the original scan:
 *
 * <ul>
 *   <li>Contributions are clamped at zero, so a source just outside {@code R} can never cool the air
 *       — the original could, because the scan box occasionally reached past the zero crossing.</li>
 *   <li>When more than {@code maxApplied} emitters are in range, the <em>nearest</em> are kept. The
 *       original stopped at an arbitrary count in scan order, so the set of blocks that counted
 *       depended on where the loops happened to break. Sorting makes the answer independent of
 *       iteration order.</li>
 * </ul>
 */
public final class EmitterField {

    private final double influenceRadius;
    private final double influenceRadiusSquared;
    private final int maxApplied;

    /**
     * @param settings the tunables supplying the influence radius and the emitter cap
     */
    public EmitterField(TemperatureSettings settings) {
        this.influenceRadius = settings.emitterInfluenceRadius();
        this.influenceRadiusSquared = influenceRadius * influenceRadius;
        this.maxApplied = settings.maxEmittersApplied();
    }

    /**
     * Sums the field around a player.
     *
     * @param environment the measured surroundings
     * @return the total contribution to the air temperature, in degrees Celsius
     */
    public double contribution(ThermalEnvironment environment) {
        List<EmitterSample> active = environment.emitters().stream()
                .filter(EmitterSample::active)
                .sorted(Comparator.comparingDouble(EmitterSample::distanceSquared))
                .limit(maxApplied)
                .toList();

        double total = 0.0;
        for (EmitterSample sample : active) {
            total += contributionOf(sample.emitter(), sample.distanceSquared());
        }
        return total;
    }

    /**
     * The contribution of a single emitter at a distance.
     *
     * @param emitter         the kind of block
     * @param distanceSquared the squared distance, in blocks squared
     * @return a signed contribution, positive for heat, negative for cold
     */
    public double contributionOf(ThermalEmitter emitter, double distanceSquared) {
        double closeness = influenceRadiusSquared / (distanceSquared + emitter.quench());
        double potential = Math.log(closeness);
        if (potential <= 0.0) {
            return 0.0;
        }
        return emitter.sign() * potential;
    }

    /** @return the distance, in blocks, at which every emitter's contribution reaches zero. */
    public double influenceRadius() {
        return influenceRadius;
    }
}
