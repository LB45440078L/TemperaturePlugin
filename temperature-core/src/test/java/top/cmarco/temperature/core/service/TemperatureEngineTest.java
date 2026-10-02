package top.cmarco.temperature.core.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import top.cmarco.temperature.core.config.TemperatureSettings;
import top.cmarco.temperature.core.environment.ThermalEnvironment;
import top.cmarco.temperature.core.physics.TemperatureModel;

class TemperatureEngineTest {

    private final TemperatureModel model = new TemperatureModel(TemperatureSettings.defaults());
    private final TemperatureEngine engine = new TemperatureEngine(model);
    private final UUID player = UUID.randomUUID();

    private ThermalEnvironment desert() {
        return ThermalEnvironment.builder()
                .climate(model.resolveClimate("minecraft:desert"))
                .worldBounds(-64.0, 320.0)
                .altitude(64.0)
                .gameTime(6_000L)
                .exposedToSky(true)
                .build();
    }

    @Test
    void firstReadingStartsAtTheInitialBodyTemperature() {
        TemperatureReading reading = engine.update(player, desert(), 1_000L);
        assertThat(reading.bodyCelsius())
                .isCloseTo(TemperatureSettings.defaults().initialBodyTemperatureCelsius(), within(1e-9));
    }

    @Test
    void bodyDriftsTowardsTheAmbientTarget() {
        double target = engine.update(player, desert(), 1_000L).ambient().apparentCelsius();
        long clock = 1_000L;
        double value = engine.update(player, desert(), clock).bodyCelsius();
        for (int i = 0; i < 400; i++) {
            clock += 1_000L;
            value = engine.update(player, desert(), clock).bodyCelsius();
        }
        assertThat(value).isCloseTo(target, within(0.5));
    }

    @Test
    @DisplayName("a long real gap advances the body more than a short one")
    void elapsedTimeMatters() {
        engine.update(player, desert(), 0L);
        double quick = engine.update(player, desert(), 5_000L).bodyCelsius();

        UUID other = UUID.randomUUID();
        engine.update(other, desert(), 0L);
        double slow = engine.update(other, desert(), 60_000L).bodyCelsius();

        double target = model.ambient(desert(), 0L).apparentCelsius();
        assertThat(Math.abs(slow - target)).isLessThan(Math.abs(quick - target));
    }

    @Test
    void forgetResetsTheBody() {
        engine.update(player, desert(), 0L);
        engine.update(player, desert(), 120_000L);
        engine.forget(player);
        assertThat(engine.bodyTemperature(player)).isEmpty();
        assertThat(engine.tracked()).isZero();
    }

    @Test
    void clearForgetsEveryone() {
        engine.update(player, desert(), 0L);
        engine.update(UUID.randomUUID(), desert(), 0L);
        engine.clear();
        assertThat(engine.tracked()).isZero();
    }
}
