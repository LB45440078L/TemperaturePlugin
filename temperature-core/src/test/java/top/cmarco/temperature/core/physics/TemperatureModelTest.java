package top.cmarco.temperature.core.physics;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import top.cmarco.temperature.core.climate.ClimateArchetype;
import top.cmarco.temperature.core.config.TemperatureSettings;
import top.cmarco.temperature.core.environment.ArmorProfile;
import top.cmarco.temperature.core.environment.EmitterSample;
import top.cmarco.temperature.core.environment.ThermalEmitter;
import top.cmarco.temperature.core.environment.ThermalEnvironment;

/**
 * End-to-end checks of the composed model: each physical lever must move the reading in the
 * direction the physics predicts, with everything else held constant.
 */
class TemperatureModelTest {

    private static final long NOON = 6_000L;
    private final TemperatureSettings settings = TemperatureSettings.defaults();
    private final TemperatureModel model = new TemperatureModel(settings);

    private ThermalEnvironment.Builder base(String biome) {
        return ThermalEnvironment.builder()
                .climate(model.resolveClimate(biome))
                .worldBounds(-64.0, 320.0)
                .altitude(64.0)
                .gameTime(NOON)
                .exposedToSky(true);
    }

    @Test
    void aDesertIsWarmerThanATaiga() {
        double desert = model.ambient(base("minecraft:desert").build(), 0L).airCelsius();
        double taiga = model.ambient(base("minecraft:taiga").build(), 0L).airCelsius();
        assertThat(desert).isGreaterThan(taiga);
    }

    @Test
    void higherGroundIsColder() {
        double low = model.ambient(base("minecraft:plains").altitude(-32.0).build(), 0L).airCelsius();
        double high = model.ambient(base("minecraft:plains").altitude(200.0).build(), 0L).airCelsius();
        assertThat(high).isLessThan(low);
    }

    @Test
    @DisplayName("shelter removes the solar bonus the original applied to everyone")
    void shelterIsCoolerThanOpenSky() {
        double open = model.ambient(base("minecraft:plains").exposedToSky(true).build(), 0L).airCelsius();
        double roofed = model.ambient(base("minecraft:plains").exposedToSky(false).build(), 0L).airCelsius();
        assertThat(open - roofed).isCloseTo(settings.solarGainCelsius(), org.assertj.core.api.Assertions.within(1e-9));
    }

    @Test
    void lavaOpensTheReading() {
        double plain = model.ambient(base("minecraft:plains").build(), 0L).airCelsius();
        double besideLava = model.ambient(base("minecraft:plains")
                .emitters(List.of(EmitterSample.of(ThermalEmitter.LAVA, 1.0)))
                .build(), 0L).airCelsius();
        assertThat(besideLava).isGreaterThan(plain);
    }

    @Test
    void snowLowersTheReading() {
        double plain = model.ambient(base("minecraft:plains").build(), 0L).airCelsius();
        double inSnow = model.ambient(base("minecraft:plains")
                .emitters(List.of(EmitterSample.of(ThermalEmitter.SNOW, 1.0)))
                .build(), 0L).airCelsius();
        assertThat(inSnow).isLessThan(plain);
    }

    @Test
    void leatherInsulates() {
        double bare = model.ambient(base("minecraft:plains").build(), 0L).airCelsius();
        double dressed = model.ambient(base("minecraft:plains")
                .armor(new ArmorProfile(4, 0))
                .build(), 0L).airCelsius();
        assertThat(dressed - bare).isCloseTo(10.0, org.assertj.core.api.Assertions.within(1e-9));
    }

    @Test
    void waterIsCooling() {
        double dry = model.ambient(base("minecraft:warm_ocean").submerged(false).build(), 0L).airCelsius();
        double wet = model.ambient(base("minecraft:warm_ocean").submerged(true).build(), 0L).airCelsius();
        assertThat(wet).isLessThan(dry);
    }

    @Test
    void winterIsColderThanSummer() {
        long summer = 6_000L;
        long winter = 24_000L * 31L * 2 + 6_000L;
        double summerTemp = model.ambient(base("minecraft:plains").gameTime(summer).build(), 0L).airCelsius();
        double winterTemp = model.ambient(base("minecraft:plains").gameTime(winter).build(), 0L).airCelsius();
        assertThat(winterTemp).isLessThan(summerTemp);
    }

    @Test
    @DisplayName("rain scrubs heat from hot air")
    void rainCoolsHotAir() {
        ThermalEnvironment hot = ThermalEnvironment.builder()
                .climate(model.resolveClimate("minecraft:desert"))
                .worldBounds(-64.0, 320.0)
                .altitude(64.0)
                .gameTime(NOON)
                .exposedToSky(true)
                .precipitating(true)
                .build();
        ThermalEnvironment dry = ThermalEnvironment.builder()
                .climate(model.resolveClimate("minecraft:desert"))
                .worldBounds(-64.0, 320.0)
                .altitude(64.0)
                .gameTime(NOON)
                .exposedToSky(true)
                .precipitating(false)
                .build();
        assertThat(model.ambient(hot, 0L).airCelsius())
                .isLessThan(model.ambient(dry, 0L).airCelsius());
    }

    @Test
    void breakdownNamesEveryModifier() {
        var contributions = model.breakdown(base("minecraft:plains").build(), 0L);
        assertThat(contributions)
                .containsKeys("biome-altitude", "sunlight", "season", "emitters", "armor",
                        "active-effects", "immersion", "rain");
    }

    @Test
    void winterClimesResolveToTheColdArchetype() {
        assertThat(model.resolveClimate("minecraft:frozen_ocean").archetype())
                .isEqualTo(ClimateArchetype.COLD_OCEAN);
    }
}
