package top.cmarco.temperature.core.climate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.Test;
import top.cmarco.temperature.core.config.TemperatureSettings;

class SeasonCycleTest {

    private final SeasonCycle cycle = new SeasonCycle(24_000L, 31L);

    @Test
    void startsInSummer() {
        assertThat(cycle.seasonAt(0L)).isEqualTo(Season.SUMMER);
    }

    @Test
    void advancesOneSeasonPerThirtyOneDays() {
        long season = 24_000L * 31L;
        assertThat(cycle.seasonAt(season)).isEqualTo(Season.AUTUMN);
        assertThat(cycle.seasonAt(2 * season)).isEqualTo(Season.WINTER);
        assertThat(cycle.seasonAt(3 * season)).isEqualTo(Season.SPRING);
        assertThat(cycle.seasonAt(4 * season)).isEqualTo(Season.SUMMER);
    }

    @Test
    void isDeterministicFromGameTime() {
        assertThat(cycle.seasonAt(12_345_678L)).isEqualTo(cycle.seasonAt(12_345_678L));
    }

    @Test
    void progressWithinSeasonIsNormalised() {
        long season = 24_000L * 31L;
        assertThat(cycle.progressWithinSeason(0L)).isEqualTo(0.0);
        assertThat(cycle.progressWithinSeason(season / 2)).isCloseTo(0.5, within(1e-9));
        assertThat(cycle.progressWithinSeason(season - 1)).isLessThan(1.0);
    }

    @Test
    void dayFractionWraps() {
        assertThat(cycle.dayFraction(0L)).isEqualTo(0.0);
        assertThat(cycle.dayFraction(6_000L)).isCloseTo(0.25, within(1e-9));
        assertThat(cycle.dayFraction(24_000L)).isEqualTo(0.0);
    }

    @Test
    void seasonalOffsetMatchesConfiguredSeasonAtItsStart() {
        TemperatureSettings settings = TemperatureSettings.defaults();
        assertThat(cycle.seasonalOffsetCelsius(0L, settings))
                .isCloseTo(settings.seasonOffsetCelsius(Season.SUMMER), within(1e-9));
        long autumn = 24_000L * 31L;
        assertThat(cycle.seasonalOffsetCelsius(autumn, settings))
                .isCloseTo(settings.seasonOffsetCelsius(Season.AUTUMN), within(1e-9));
    }

    @Test
    void seasonalOffsetIsContinuousAcrossABoundary() {
        TemperatureSettings settings = TemperatureSettings.defaults();
        long boundary = 24_000L * 31L;
        double before = cycle.seasonalOffsetCelsius(boundary - 1, settings);
        double after = cycle.seasonalOffsetCelsius(boundary, settings);
        assertThat(after).isCloseTo(before, within(0.01));
    }

    @Test
    void rejectsNonPositiveCalendar() {
        assertThat(catchThrowable(() -> new SeasonCycle(0L, 31L))).isInstanceOf(IllegalArgumentException.class);
        assertThat(catchThrowable(() -> new SeasonCycle(24_000L, 0L))).isInstanceOf(IllegalArgumentException.class);
    }

    private static Throwable catchThrowable(Runnable runnable) {
        try {
            runnable.run();
            return null;
        } catch (Throwable t) {
            return t;
        }
    }
}
