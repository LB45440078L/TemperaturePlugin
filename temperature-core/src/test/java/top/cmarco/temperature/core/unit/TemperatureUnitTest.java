package top.cmarco.temperature.core.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class TemperatureUnitTest {

    @Nested
    @DisplayName("known conversions")
    class KnownConversions {

        @Test
        void celsiusIsIdentity() {
            assertThat(TemperatureUnit.CELSIUS.fromCelsius(21.5)).isEqualTo(21.5);
            assertThat(TemperatureUnit.CELSIUS.toCelsius(21.5)).isEqualTo(21.5);
        }

        @Test
        void fahrenheitFreezingAndBoiling() {
            assertThat(TemperatureUnit.FAHRENHEIT.fromCelsius(0.0)).isCloseTo(32.0, within(1e-9));
            assertThat(TemperatureUnit.FAHRENHEIT.fromCelsius(100.0)).isCloseTo(212.0, within(1e-9));
        }

        @Test
        void kelvinUsesAbsoluteZero() {
            assertThat(TemperatureUnit.KELVIN.fromCelsius(TemperatureUnit.ABSOLUTE_ZERO_CELSIUS))
                    .isCloseTo(0.0, within(1e-9));
            assertThat(TemperatureUnit.KELVIN.fromCelsius(0.0)).isCloseTo(273.15, within(1e-9));
        }

        @Test
        @DisplayName("newton inverse is the corrected one, not Fahrenheit's")
        void newtonRoundTrips() {
            // The legacy plugin's Newton inverse was really the Fahrenheit inverse; 100 C must be
            // 33 N, not 37.7 N.
            assertThat(TemperatureUnit.NEWTON.fromCelsius(100.0)).isCloseTo(33.0, within(1e-9));
            assertThat(TemperatureUnit.NEWTON.toCelsius(33.0)).isCloseTo(100.0, within(1e-9));
        }

        @Test
        void delisleRunsBackwards() {
            assertThat(TemperatureUnit.DELISLE.fromCelsius(100.0)).isCloseTo(0.0, within(1e-9));
            assertThat(TemperatureUnit.DELISLE.fromCelsius(0.0)).isCloseTo(150.0, within(1e-9));
        }

        @Test
        void romerFreezingAndBoiling() {
            assertThat(TemperatureUnit.ROMER.fromCelsius(0.0)).isCloseTo(7.5, within(1e-9));
            assertThat(TemperatureUnit.ROMER.fromCelsius(100.0)).isCloseTo(60.0, within(1e-9));
        }
    }

    @ParameterizedTest
    @EnumSource(TemperatureUnit.class)
    @DisplayName("every scale round-trips within floating-point tolerance")
    void everyScaleRoundTrips(TemperatureUnit unit) {
        double[] samples = {-273.15, -40.0, -1.0, 0.0, 11.25, 36.0, 100.0, 250.0};
        for (double celsius : samples) {
            double converted = unit.fromCelsius(celsius);
            assertThat(unit.toCelsius(converted)).as("%s at %s", unit, celsius).isCloseTo(celsius, within(1e-9));
        }
    }

    @ParameterizedTest
    @EnumSource(TemperatureUnit.class)
    @DisplayName("convert() agrees with a from/to round trip")
    void convertMatchesRoundTrip(TemperatureUnit unit) {
        double celsius = 27.5;
        for (TemperatureUnit target : TemperatureUnit.values()) {
            assertThat(unit.convert(unit.fromCelsius(celsius), target))
                    .isCloseTo(target.fromCelsius(celsius), within(1e-9));
        }
    }

    @Test
    void parseAcceptsNameAndDisplayName() {
        assertThat(TemperatureUnit.parse("celsius")).contains(TemperatureUnit.CELSIUS);
        assertThat(TemperatureUnit.parse("FAHRENHEIT")).contains(TemperatureUnit.FAHRENHEIT);
        assertThat(TemperatureUnit.parse("Kelvin")).contains(TemperatureUnit.KELVIN);
        assertThat(TemperatureUnit.parse("  romer ")).contains(TemperatureUnit.ROMER);
    }

    @Test
    void parseRejectsUnknownAndBlank() {
        assertThat(TemperatureUnit.parse("furlongs")).isEmpty();
        assertThat(TemperatureUnit.parse("")).isEmpty();
        assertThat(TemperatureUnit.parse(null)).isEmpty();
    }

    @Test
    void symbolsAreUniqueAndNonBlank() {
        assertThat(TemperatureUnit.values())
                .extracting(TemperatureUnit::symbol)
                .doesNotHaveDuplicates()
                .allSatisfy(symbol -> assertThat(symbol).isNotBlank());
    }
}
