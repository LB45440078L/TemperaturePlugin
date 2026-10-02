package top.cmarco.temperature.core.climate;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import top.cmarco.temperature.core.config.TemperatureSettings;

class BiomeClimateRegistryTest {

    private final BiomeClimateRegistry registry = new BiomeClimateRegistry();
    private final TemperatureSettings settings = TemperatureSettings.defaults();

    @Test
    void knownBiomesUseTheTable() {
        assertThat(registry.archetypeOf("minecraft:desert")).isEqualTo(ClimateArchetype.WARM);
        assertThat(registry.archetypeOf("minecraft:taiga")).isEqualTo(ClimateArchetype.COLD);
        assertThat(registry.archetypeOf("minecraft:plains")).isEqualTo(ClimateArchetype.TEMPERATE);
        assertThat(registry.archetypeOf("minecraft:warm_ocean")).isEqualTo(ClimateArchetype.OCEAN);
        assertThat(registry.archetypeOf("minecraft:frozen_ocean")).isEqualTo(ClimateArchetype.COLD_OCEAN);
        assertThat(registry.archetypeOf("minecraft:the_end")).isEqualTo(ClimateArchetype.END);
    }

    @Test
    @DisplayName("nether biomes beat the land keywords their names contain")
    void netherWinsOverForestKeyword() {
        // "crimson_forest" contains "forest"; the classifier must still reach NETHER.
        assertThat(registry.archetypeOf("minecraft:crimson_forest")).isEqualTo(ClimateArchetype.NETHER);
        assertThat(registry.archetypeOf("minecraft:warped_forest")).isEqualTo(ClimateArchetype.NETHER);
        assertThat(registry.archetypeOf("minecraft:soul_sand_valley")).isEqualTo(ClimateArchetype.NETHER);
    }

    @Test
    void namespaceIsOptional() {
        assertThat(registry.archetypeOf("desert")).isEqualTo(ClimateArchetype.WARM);
        assertThat(registry.archetypeOf("  MINECRAFT:Dessert  ".toLowerCase())).isNotNull();
    }

    @Test
    @DisplayName("unknown biomes fall back to the keyword classifier")
    void unknownBiomesFallBack() {
        assertThat(registry.archetypeOf("mythical:snowy_peaks")).isEqualTo(ClimateArchetype.COLD);
        assertThat(registry.archetypeOf("mythical:scorching_desert")).isEqualTo(ClimateArchetype.WARM);
        assertThat(registry.archetypeOf("mythical:deep_cold_ocean")).isEqualTo(ClimateArchetype.COLD_OCEAN);
        assertThat(registry.archetypeOf("mythical:everyday_fields")).isEqualTo(ClimateArchetype.TEMPERATE);
    }

    @Test
    void overridesReplaceTheTable() {
        registry.register("minecraft:desert", ClimateArchetype.COLD);
        assertThat(registry.archetypeOf("minecraft:desert")).isEqualTo(ClimateArchetype.COLD);
    }

    @Test
    void resolveCombinesKeyAndRange() {
        BiomeClimate climate = registry.resolve("minecraft:desert", settings::climateRange);
        assertThat(climate.biomeKey()).isEqualTo("minecraft:desert");
        assertThat(climate.archetype()).isEqualTo(ClimateArchetype.WARM);
        assertThat(climate.range()).isEqualTo(settings.climateRange(ClimateArchetype.WARM));
    }

    @Test
    void blankKeyIsTemperate() {
        assertThat(registry.archetypeOf(null)).isEqualTo(ClimateArchetype.TEMPERATE);
        assertThat(registry.archetypeOf("")).isEqualTo(ClimateArchetype.TEMPERATE);
    }
}
