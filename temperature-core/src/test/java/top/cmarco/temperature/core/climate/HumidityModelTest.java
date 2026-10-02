package top.cmarco.temperature.core.climate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.Test;
import top.cmarco.temperature.core.config.TemperatureSettings;

class HumidityModelTest {

    private static final BiomeClimate SWAMP =
            new BiomeClimate("minecraft:swamp", ClimateArchetype.TEMPERATE, new ClimateRange(10.0, 32.0, 70.0, 95.0));

    private final HumidityModel model = HumidityModel.from(TemperatureSettings.defaults());

    @Test
    void peaksNearTheConfiguredPeak() {
        double atPeak = model.diurnalScale(model.peakFraction());
        double morning = model.diurnalScale(0.0);
        assertThat(atPeak).isCloseTo(1.0, within(0.02));
        assertThat(atPeak).isGreaterThan(morning);
    }

    @Test
    void fallsToTheFloorAtNight() {
        assertThat(model.diurnalScale(20.0 / 24.0)).isCloseTo(0.031, within(0.01));
    }

    @Test
    void humidityStaysInsideTheBiomeBand() {
        for (double dayFraction = 0.0; dayFraction < 1.0; dayFraction += 0.05) {
            double humidity = model.resolve(SWAMP, dayFraction, false, 0.0);
            assertThat(humidity).isBetween(70.0, 95.0);
        }
    }

    @Test
    void rainRaisesHumidity() {
        double dry = model.resolve(SWAMP, model.peakFraction(), false, 0.0);
        double wet = model.resolve(SWAMP, model.peakFraction(), true, 0.0);
        assertThat(wet).isGreaterThan(dry);
    }

    @Test
    void jitterRaisesHumidityAndIsClamped() {
        double calm = model.resolve(SWAMP, 0.0, false, 0.0);
        double noisy = model.resolve(SWAMP, 0.0, false, 0.5);
        assertThat(noisy).isGreaterThan(calm);
        assertThat(model.resolve(SWAMP, 0.0, true, 500.0)).isEqualTo(100.0);
    }

    @Test
    void isMonotonicAroundThePeak() {
        double rising = model.diurnalScale(0.10);
        double afternoon = model.diurnalScale(0.30);
        double evening = model.diurnalScale(0.45);
        assertThat(rising).isLessThan(afternoon);
        assertThat(evening).isLessThan(afternoon);
    }
}
