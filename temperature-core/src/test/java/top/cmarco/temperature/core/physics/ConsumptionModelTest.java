package top.cmarco.temperature.core.physics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.Test;
import top.cmarco.temperature.core.config.TemperatureSettings;

class ConsumptionModelTest {

    private final ConsumptionModel model = new ConsumptionModel(TemperatureSettings.defaults());

    @Test
    void coolingPeaksAtSevenAndAHalfMinutes() {
        assertThat(model.waterCoolingCelsius(7.5)).isCloseTo(20.0, within(1e-9));
    }

    @Test
    void coolingIsNeverNegative() {
        for (double minutes = 0.0; minutes <= 60.0; minutes += 0.25) {
            assertThat(model.waterCoolingCelsius(minutes)).isGreaterThanOrEqualTo(0.0);
        }
    }

    @Test
    void coolingFadesWithinTheDuration() {
        assertThat(model.waterCoolingCelsius(15.0)).isCloseTo(0.0, within(0.02));
        assertThat(model.waterCoolingCelsius(model.durationMinutes() + 1.0)).isZero();
    }

    @Test
    void coolingRampsUpBeforeThePeak() {
        assertThat(model.waterCoolingCelsius(3.0)).isLessThan(model.waterCoolingCelsius(7.5));
        assertThat(model.waterCoolingCelsius(6.0)).isGreaterThan(model.waterCoolingCelsius(1.0));
    }

    @Test
    void negativeElapsedTimeIsTreatedAsJustDrunk() {
        assertThat(model.waterCoolingCelsius(-5.0)).isEqualTo(model.waterCoolingCelsius(0.0));
    }
}
