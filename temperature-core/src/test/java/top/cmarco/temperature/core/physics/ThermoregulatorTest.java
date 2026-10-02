package top.cmarco.temperature.core.physics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.Test;
import top.cmarco.temperature.core.config.TemperatureSettings;

class ThermoregulatorTest {

    private final Thermoregulator regulator = new Thermoregulator(TemperatureSettings.defaults());

    @Test
    void noTimeMeansNoChange() {
        assertThat(regulator.relax(36.0, 50.0, 0.0)).isEqualTo(36.0);
        assertThat(regulator.relax(36.0, 50.0, -1.0)).isEqualTo(36.0);
    }

    @Test
    void closesSixtyThreePercentOfTheGapInOneTimeConstant() {
        double tau = regulator.timeConstantSeconds();
        double result = regulator.relax(36.0, 46.0, tau);
        assertThat(result).isCloseTo(36.0 + 0.632 * 10.0, within(0.05));
    }

    @Test
    void convergesTowardsTheTarget() {
        double value = 36.0;
        for (int i = 0; i < 500; i++) {
            value = regulator.relax(value, 5.0, 1.0);
        }
        assertThat(value).isCloseTo(5.0, within(0.001));
    }

    @Test
    void isIndependentOfStepSize() {
        // Thirty 2-second steps and sixty 1-second steps must agree: the closed-form step is exact.
        double coarse = 36.0;
        for (int i = 0; i < 30; i++) {
            coarse = regulator.relax(coarse, 50.0, 2.0);
        }
        double fine = 36.0;
        for (int i = 0; i < 60; i++) {
            fine = regulator.relax(fine, 50.0, 1.0);
        }
        assertThat(coarse).isCloseTo(fine, within(1e-9));
    }

    @Test
    void staysWithinConfiguredBounds() {
        double hot = regulator.relax(36.0, 10_000.0, 10_000.0);
        double cold = regulator.relax(36.0, -10_000.0, 10_000.0);
        assertThat(hot).isLessThanOrEqualTo(60.0);
        assertThat(cold).isGreaterThanOrEqualTo(-40.0);
    }
}
