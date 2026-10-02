package top.cmarco.temperature.plugin.sampler;

import static org.assertj.core.api.Assertions.assertThat;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;
import top.cmarco.temperature.core.environment.ThermalEmitter;

class MaterialMappingTest {

    private final MaterialMapping mapping = new MaterialMapping();

    @Test
    void knownHeatSourcesMapToTheirEmitter() {
        assertThat(mapping.emitterFor(Material.FURNACE)).contains(ThermalEmitter.FURNACE);
        assertThat(mapping.emitterFor(Material.BLAST_FURNACE)).contains(ThermalEmitter.FURNACE);
        assertThat(mapping.emitterFor(Material.LAVA)).contains(ThermalEmitter.LAVA);
        assertThat(mapping.emitterFor(Material.MAGMA_BLOCK)).contains(ThermalEmitter.LAVA);
        assertThat(mapping.emitterFor(Material.CAMPFIRE)).contains(ThermalEmitter.CAMPFIRE);
        assertThat(mapping.emitterFor(Material.SOUL_CAMPFIRE)).contains(ThermalEmitter.CAMPFIRE);
        assertThat(mapping.emitterFor(Material.FIRE)).contains(ThermalEmitter.FIRE);
    }

    @Test
    void knownColdSourcesMapToTheirEmitter() {
        assertThat(mapping.emitterFor(Material.ICE)).contains(ThermalEmitter.ICE);
        assertThat(mapping.emitterFor(Material.PACKED_ICE)).contains(ThermalEmitter.ICE);
        assertThat(mapping.emitterFor(Material.BLUE_ICE)).contains(ThermalEmitter.ICE);
        assertThat(mapping.emitterFor(Material.SNOW)).contains(ThermalEmitter.SNOW);
        assertThat(mapping.emitterFor(Material.POWDER_SNOW)).contains(ThermalEmitter.SNOW);
    }

    @Test
    void inertMaterialsAreNotRelevant() {
        assertThat(mapping.emitterFor(Material.STONE)).isEmpty();
        assertThat(mapping.emitterFor(Material.AIR)).isEmpty();
        assertThat(mapping.isRelevant(Material.DIRT)).isFalse();
        assertThat(mapping.isRelevant(Material.LAVA)).isTrue();
    }
}
