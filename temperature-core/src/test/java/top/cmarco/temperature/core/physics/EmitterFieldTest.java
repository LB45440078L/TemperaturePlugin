package top.cmarco.temperature.core.physics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import top.cmarco.temperature.core.config.TemperatureSettings;
import top.cmarco.temperature.core.environment.EmitterSample;
import top.cmarco.temperature.core.environment.ThermalEmitter;
import top.cmarco.temperature.core.environment.ThermalEnvironment;

class EmitterFieldTest {

    private final EmitterField field = new EmitterField(TemperatureSettings.defaults());

    @Test
    void lavaIsTheStrongestCommonSource() {
        double lava = field.contributionOf(ThermalEmitter.LAVA, 1.0);
        double campfire = field.contributionOf(ThermalEmitter.CAMPFIRE, 1.0);
        double snow = field.contributionOf(ThermalEmitter.SNOW, 1.0);
        assertThat(lava).isGreaterThan(campfire);
        assertThat(snow).isNegative();
        assertThat(lava).isCloseTo(3.2189, within(1e-3));
    }

    @Test
    void contributionReachesZeroAtTheInfluenceRadius() {
        assertThat(field.contributionOf(ThermalEmitter.LAVA, 25.0)).isZero();
        assertThat(field.contributionOf(ThermalEmitter.LAVA, 30.0)).isZero();
        assertThat(field.contributionOf(ThermalEmitter.LAVA, 24.0)).isPositive();
    }

    @Test
    void contributionDecreasesWithDistance() {
        double near = field.contributionOf(ThermalEmitter.LAVA, 1.0);
        double far = field.contributionOf(ThermalEmitter.LAVA, 16.0);
        assertThat(far).isLessThan(near).isPositive();
    }

    @Test
    void coldEmittersSubtract() {
        assertThat(field.contributionOf(ThermalEmitter.ICE, 1.0)).isNegative();
        assertThat(field.contributionOf(ThermalEmitter.SNOW, 4.0)).isNegative();
    }

    @Test
    void onlyTheNearestEmitersAreSummed() {
        TemperatureSettings settings = TemperatureSettings.builder().emitterField(5.0, 2).build();
        EmitterField capped = new EmitterField(settings);
        List<EmitterSample> samples = List.of(
                EmitterSample.of(ThermalEmitter.LAVA, 9.0),
                EmitterSample.of(ThermalEmitter.LAVA, 1.0),
                EmitterSample.of(ThermalEmitter.LAVA, 4.0));

        double expected = field.contributionOf(ThermalEmitter.LAVA, 1.0)
                + field.contributionOf(ThermalEmitter.LAVA, 4.0);
        assertThat(capped.contribution(environmentWith(samples))).isCloseTo(expected, within(1e-9));
    }

    @Test
    void inactiveEmitersAreIgnored() {
        List<EmitterSample> samples = List.of(
                new EmitterSample(ThermalEmitter.FURNACE, 1.0, false),
                EmitterSample.of(ThermalEmitter.LAVA, 1.0));
        assertThat(field.contribution(environmentWith(samples)))
                .isCloseTo(field.contributionOf(ThermalEmitter.LAVA, 1.0), within(1e-9));
    }

    @Test
    void orderOfSamplesDoesNotChangeTheResult() {
        List<EmitterSample> ordered = List.of(
                EmitterSample.of(ThermalEmitter.LAVA, 1.0),
                EmitterSample.of(ThermalEmitter.SNOW, 4.0),
                EmitterSample.of(ThermalEmitter.CAMPFIRE, 9.0));
        List<EmitterSample> shuffled = new ArrayList<>(ordered);
        java.util.Collections.reverse(shuffled);
        assertThat(field.contribution(environmentWith(shuffled)))
                .isCloseTo(field.contribution(environmentWith(ordered)), within(1e-12));
    }

    private static ThermalEnvironment environmentWith(List<EmitterSample> samples) {
        return ThermalEnvironment.builder()
                .climate(new top.cmarco.temperature.core.climate.BiomeClimate(
                        "minecraft:plains",
                        top.cmarco.temperature.core.climate.ClimateArchetype.TEMPERATE,
                        new top.cmarco.temperature.core.climate.ClimateRange(10.0, 32.0, 45.0, 75.0)))
                .emitters(samples)
                .build();
    }
}
