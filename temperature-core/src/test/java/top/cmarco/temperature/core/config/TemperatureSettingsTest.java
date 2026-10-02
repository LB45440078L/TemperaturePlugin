package top.cmarco.temperature.core.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import top.cmarco.temperature.core.climate.ClimateArchetype;
import top.cmarco.temperature.core.climate.ClimateRange;
import top.cmarco.temperature.core.climate.Season;
import top.cmarco.temperature.core.environment.ThermalEmitter;

class TemperatureSettingsTest {

    @Test
    void defaultsAreValidAndPopulated() {
        TemperatureSettings settings = TemperatureSettings.defaults();
        for (ClimateArchetype archetype : ClimateArchetype.values()) {
            assertThat(settings.climateRange(archetype)).isNotNull();
        }
        for (Season season : Season.values()) {
            assertThat(settings.seasonOffsetCelsius(season)).isNotNaN();
        }
    }

    @Test
    void climateRangeIsImmutableAndCopySafe() {
        TemperatureSettings settings = TemperatureSettings.defaults();
        assertThat(settings.climateRange(ClimateArchetype.WARM))
                .isEqualTo(new ClimateRange(20.0, 38.0, 15.0, 45.0));
    }

    @Test
    void overridesAreRecorded() {
        TemperatureSettings settings = TemperatureSettings.builder()
                .biomeOverride("minecraft:desert", ClimateArchetype.COLD)
                .build();
        assertThat(settings.biomeOverride("minecraft:desert")).isEqualTo(ClimateArchetype.COLD);
    }

    @Test
    void rejectsAnInvertedLapseFraction() {
        assertThatThrownBy(() -> TemperatureSettings.builder().altitude(null, 0.5).build())
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> TemperatureSettings.builder()
                .altitude(top.cmarco.temperature.core.climate.AltitudeModel.Mode.LINEAR_LAPSE, 1.5)
                .build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("lapseFraction");
    }

    @Test
    void rejectsAnInvalidThermoregulationWindow() {
        assertThatThrownBy(() -> TemperatureSettings.builder()
                .thermoregulation(36.0, 36.0, 50.0, -40.0)
                .build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("body temperature");
    }

    @Test
    void rejectsInvertedEffectThresholds() {
        assertThatThrownBy(() -> TemperatureSettings.builder().effectThresholds(-1.0, 5.0).build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("threshold");
    }

    @Test
    void rejectsANonPositiveTimeConstant() {
        assertThatThrownBy(() -> TemperatureSettings.builder()
                .thermoregulation(0.0, 36.0, -40.0, 60.0)
                .build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be positive");
    }

    @Test
    void emitterDefaultsCoverEveryKind() {
        for (ThermalEmitter emitter : ThermalEmitter.values()) {
            assertThat(emitter.quench()).isPositive();
            assertThat(emitter.sign()).isIn(-1, 1);
        }
    }
}
