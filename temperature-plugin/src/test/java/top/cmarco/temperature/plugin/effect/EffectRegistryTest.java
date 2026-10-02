package top.cmarco.temperature.plugin.effect;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import top.cmarco.temperature.core.config.TemperatureSettings;
import top.cmarco.temperature.core.environment.ActiveEffect;
import top.cmarco.temperature.core.physics.ConsumptionModel;

class EffectRegistryTest {

    private final ConsumptionModel consumption = new ConsumptionModel(TemperatureSettings.defaults());
    private final EffectRegistry registry = new EffectRegistry();
    private final UUID player = UUID.randomUUID();

    @Test
    void waterCoolsImmediatelyThenFades() {
        long drunkAt = 1_000_000L;
        registry.recordWater(player, drunkAt);

        double early = coolingAt(drunkAt, drunkAt + 60_000L);
        double peak = coolingAt(drunkAt, drunkAt + 7 * 60_000L + 30_000L);
        double late = coolingAt(drunkAt, drunkAt + 14 * 60_000L);

        assertThat(peak).isGreaterThan(early);
        assertThat(peak).isGreaterThan(late);
        assertThat(peak).isGreaterThan(0.0);
    }

    @Test
    void waterEffectExpires() {
        long drunkAt = 1_000_000L;
        registry.recordWater(player, drunkAt);
        assertThat(effectsAt(drunkAt + 16 * 60_000L)).isEmpty();
    }

    @Test
    void timedEffectsApplyThenExpire() {
        registry.addTimed(player, new ActiveEffect(6.0, 5_000L, "test"));
        assertThat(effectsAt(4_000L)).hasSize(1);
        assertThat(effectsAt(6_000L)).isEmpty();
    }

    @Test
    void multipleTimedEffectsStack() {
        registry.addTimed(player, new ActiveEffect(3.0, 10_000L, "a"));
        registry.addTimed(player, new ActiveEffect(-2.0, 10_000L, "b"));
        List<ActiveEffect> effects = effectsAt(5_000L);
        assertThat(effects).hasSize(2);
        assertThat(effects.stream().mapToDouble(ActiveEffect::deltaCelsius).sum()).isEqualTo(1.0);
    }

    @Test
    void forgetClearsEverything() {
        registry.recordWater(player, 1_000L);
        registry.addTimed(player, new ActiveEffect(1.0, 10_000L, "a"));
        registry.forget(player);
        assertThat(registry.activeEffects(player, 2_000L, consumption)).isEmpty();
    }

    private double coolingAt(long drunkAt, long now) {
        UUID fresh = UUID.randomUUID();
        registry.recordWater(fresh, drunkAt);
        List<ActiveEffect> effects = registry.activeEffects(fresh, now, consumption);
        return effects.isEmpty() ? 0.0 : -effects.get(0).deltaCelsius();
    }

    private List<ActiveEffect> effectsAt(long now) {
        return registry.activeEffects(player, now, consumption);
    }
}
