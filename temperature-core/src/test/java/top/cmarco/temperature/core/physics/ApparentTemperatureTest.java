package top.cmarco.temperature.core.physics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import top.cmarco.temperature.core.config.TemperatureSettings;

class ApparentTemperatureTest {

    private final ApparentTemperature apparent = new ApparentTemperature(TemperatureSettings.defaults());

    @Test
    void heatIndexMatchesTheReferenceRegression() {
        assertThat(ApparentTemperature.heatIndex(35.0, 60.0)).isCloseTo(45.05017, within(1e-4));
        assertThat(ApparentTemperature.heatIndex(30.0, 70.0)).isCloseTo(35.03802, within(1e-4));
    }

    @Test
    void heatIndexGrowsWithHeatAndHumidity() {
        assertThat(ApparentTemperature.heatIndex(35.0, 60.0))
                .isGreaterThan(ApparentTemperature.heatIndex(30.0, 60.0));
        assertThat(ApparentTemperature.heatIndex(35.0, 70.0))
                .isGreaterThan(ApparentTemperature.heatIndex(35.0, 50.0));
    }

    @Test
    void windChillMatchesTheReferenceFormula() {
        assertThat(ApparentTemperature.windChill(0.0, 20.0)).isCloseTo(-5.24222, within(1e-4));
        assertThat(ApparentTemperature.windChill(-10.0, 30.0)).isCloseTo(-19.5205, within(1e-3));
    }

    @Test
    void windChillIsColderThanTheAir() {
        assertThat(ApparentTemperature.windChill(5.0, 15.0)).isLessThan(5.0);
    }

    @Test
    void warmAndHumidUsesHeatIndex() {
        assertThat(apparent.estimate(30.0, 70.0, 0.0)).isCloseTo(35.038, within(1e-3));
    }

    @Test
    void coldAndWindyUsesWindChill() {
        assertThat(apparent.estimate(0.0, 50.0, 20.0)).isCloseTo(-5.242, within(1e-3));
    }

    @Test
    void mildConditionsFallBackToDryBulb() {
        assertThat(apparent.estimate(20.0, 50.0, 0.0)).isEqualTo(20.0);
        assertThat(apparent.estimate(15.0, 90.0, 3.0)).isEqualTo(15.0);
    }

    @Test
    void comfortablyWarmButDryDoesNotUseHeatIndex() {
        // Humidity below 40 percent is outside the regression's validity, so the dry-bulb stands.
        assertThat(apparent.estimate(35.0, 20.0, 0.0)).isEqualTo(35.0);
    }

    @Test
    @DisplayName("the heat index is not extrapolated beyond its fitted range")
    void heatIndexUpperBoundFallsBackToDryBulb() {
        // 49 C is a plausible desert reading, but far above where Rothfusz was fitted; without the
        // upper guard the polynomial returns ~90 C.
        assertThat(apparent.estimate(49.0, 45.0, 0.0)).isEqualTo(49.0);
        assertThat(ApparentTemperature.heatIndex(49.0, 45.0)).isGreaterThan(80.0);
    }

    @Test
    void disabledRegressionsReturnTheDryBulb() {
        TemperatureSettings disabled = TemperatureSettings.builder()
                .apparentTemperature(false, 26.7, 46.0, false, 10.0, 4.8)
                .build();
        ApparentTemperature plain = new ApparentTemperature(disabled);
        assertThat(plain.estimate(35.0, 70.0, 0.0)).isEqualTo(35.0);
        assertThat(plain.estimate(-5.0, 50.0, 40.0)).isEqualTo(-5.0);
    }
}
