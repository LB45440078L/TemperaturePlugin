package top.cmarco.temperature.plugin.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.logging.Logger;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.Test;
import top.cmarco.temperature.core.climate.ClimateArchetype;
import top.cmarco.temperature.core.climate.Season;
import top.cmarco.temperature.core.unit.TemperatureUnit;

/**
 * Exercises the YAML-to-settings mapping without a running server, by handing the loader a mocked
 * plugin and a real {@link YamlConfiguration}. This is the class most likely to break when the
 * schema changes, and the failure mode is silent number drift, so it is worth pinning.
 */
class ConfigurationLoaderTest {

    private LoadedConfiguration load(String yaml) {
        Plugin plugin = mock(Plugin.class);
        YamlConfiguration config = new YamlConfiguration();
        try {
            config.loadFromString(yaml);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        when(plugin.getConfig()).thenReturn(config);
        when(plugin.getLogger()).thenReturn(Logger.getLogger("TemperaturePluginTest"));
        return new ConfigurationLoader(plugin).load();
    }

    @Test
    void defaultsAreUsedWhenKeysAreAbsent() {
        LoadedConfiguration loaded = load("worlds:\n  - world\n");
        assertThat(loaded.display().defaultUnit()).isEqualTo(TemperatureUnit.CELSIUS);
        assertThat(loaded.updateIntervalTicks()).isEqualTo(PerformanceProfile.HIGH.updateIntervalTicks());
        assertThat(loaded.simulation().solarGainCelsius()).isEqualTo(4.0);
    }

    @Test
    void configuredValuesWin() {
        LoadedConfiguration loaded = load("""
                config-version: 2
                worlds: [world, world_nether]
                display:
                  default-unit: FAHRENHEIT
                  bar:
                    length: 20
                simulation:
                  performance: LOW
                  time-constant-seconds: 12.5
                climate:
                  warm:
                    temperature: { min: 20.0, max: 45.0 }
                    humidity: { min: 10.0, max: 30.0 }
                """);

        assertThat(loaded.worlds()).containsExactly("world", "world_nether");
        assertThat(loaded.display().defaultUnit()).isEqualTo(TemperatureUnit.FAHRENHEIT);
        assertThat(loaded.display().barLength()).isEqualTo(20);
        assertThat(loaded.updateIntervalTicks()).isEqualTo(PerformanceProfile.LOW.updateIntervalTicks());
        assertThat(loaded.simulation().thermoregulationTimeConstantSeconds()).isEqualTo(12.5);
        assertThat(loaded.simulation().climateRange(ClimateArchetype.WARM).maxTemperatureCelsius())
                .isEqualTo(45.0);
    }

    @Test
    void biomeOverridesAreParsed() {
        LoadedConfiguration loaded = load("""
                biome-overrides:
                  minecraft:desert: COLD
                """);
        assertThat(loaded.simulation().biomeOverride("minecraft:desert"))
                .isEqualTo(ClimateArchetype.COLD);
    }

    @Test
    void seasonOffsetsAreRead() {
        LoadedConfiguration loaded = load("""
                calendar:
                  season-offsets:
                    WINTER: -20.0
                """);
        assertThat(loaded.simulation().seasonOffsetCelsius(Season.WINTER)).isEqualTo(-20.0);
    }

    @Test
    void anUnknownPerformanceValueFallsBackToTheDefault() {
        LoadedConfiguration loaded = load("""
                simulation:
                  performance: TURBO
                """);
        assertThat(loaded.updateIntervalTicks()).isEqualTo(PerformanceProfile.HIGH.updateIntervalTicks());
    }
}
