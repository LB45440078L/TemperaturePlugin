package top.cmarco.temperature.core.climate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.Test;
import top.cmarco.temperature.core.config.TemperatureSettings;

class AltitudeModelTest {

    private static final BiomeClimate DESERT =
            new BiomeClimate("minecraft:desert", ClimateArchetype.WARM, new ClimateRange(25.0, 41.5, 15.0, 45.0));

    @Test
    void linearLapseIsColderWithHeight() {
        TemperatureSettings settings = TemperatureSettings.builder()
                .altitude(AltitudeModel.Mode.LINEAR_LAPSE, 0.85)
                .build();
        AltitudeModel model = AltitudeModel.from(settings);

        double ground = model.temperatureAt(DESERT, -64.0, -64.0, 320.0);
        double sky = model.temperatureAt(DESERT, 320.0, -64.0, 320.0);

        assertThat(ground).isCloseTo(41.5, within(1e-9));
        assertThat(sky).isCloseTo(25.0 + 0.15 * 16.5, within(1e-9));
        assertThat(sky).isLessThan(ground);
    }

    @Test
    void linearLapseIsMonotonic() {
        TemperatureSettings settings = TemperatureSettings.builder()
                .altitude(AltitudeModel.Mode.LINEAR_LAPSE, 0.85)
                .build();
        AltitudeModel model = AltitudeModel.from(settings);
        double previous = Double.POSITIVE_INFINITY;
        for (double y = -64.0; y <= 320.0; y += 8.0) {
            double temp = model.temperatureAt(DESERT, y, -64.0, 320.0);
            assertThat(temp).isLessThanOrEqualTo(previous);
            previous = temp;
        }
    }

    @Test
    void legacyPolynomialReproducesTheOriginalCurve() {
        TemperatureSettings settings = TemperatureSettings.builder()
                .altitude(AltitudeModel.Mode.LEGACY_POLYNOMIAL, 0.85)
                .build();
        AltitudeModel model = AltitudeModel.from(settings);

        // The original array was the polynomial evaluated at these heights.
        assertThat(model.retentionFactor(-64.0, -64.0, 320.0)).isCloseTo(0.918813, within(1e-5));
        assertThat(model.retentionFactor(0.0, -64.0, 320.0)).isCloseTo(0.695913, within(1e-6));
        assertThat(model.retentionFactor(320.0, -64.0, 320.0)).isCloseTo(0.0706, within(5e-3));
    }

    @Test
    void retentionIsClampedToUnitRange() {
        TemperatureSettings settings = TemperatureSettings.builder()
                .altitude(AltitudeModel.Mode.LEGACY_POLYNOMIAL, 0.85)
                .build();
        AltitudeModel model = AltitudeModel.from(settings);
        // Far outside the fitted domain the raw polynomial leaves [0, 1]; the model must clamp.
        assertThat(model.retentionFactor(2_000.0, -64.0, 320.0)).isBetween(0.0, 1.0);
        assertThat(model.retentionFactor(-2_000.0, -64.0, 320.0)).isBetween(0.0, 1.0);
    }

    @Test
    void altitudeIsClampedToWorldBounds() {
        TemperatureSettings settings = TemperatureSettings.builder()
                .altitude(AltitudeModel.Mode.LINEAR_LAPSE, 0.85)
                .build();
        AltitudeModel model = AltitudeModel.from(settings);
        assertThat(model.temperatureAt(DESERT, -10_000.0, -64.0, 320.0))
                .isEqualTo(model.temperatureAt(DESERT, -64.0, -64.0, 320.0));
        assertThat(model.temperatureAt(DESERT, 10_000.0, -64.0, 320.0))
                .isEqualTo(model.temperatureAt(DESERT, 320.0, -64.0, 320.0));
    }
}
