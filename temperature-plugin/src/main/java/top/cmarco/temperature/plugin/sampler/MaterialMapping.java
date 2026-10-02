package top.cmarco.temperature.plugin.sampler;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import top.cmarco.temperature.core.environment.ThermalEmitter;

/**
 * The translation from a Bukkit {@link Material} to a core {@link ThermalEmitter}.
 *
 * <p>This is the only place the two vocabularies meet. The core knows nothing of Bukkit materials
 * and the rest of the adapter asks this table, so adding a new heat source is one line here plus a
 * constant in the core — not a new branch in the physics.
 *
 * <p>Materials are resolved once, in the constructor, rather than per block per player per tick:
 * {@code Material.matchMaterial} on a hot path is exactly the kind of allocation the legacy plugin
 * avoided by hard-coding sets, and an enum map keeps that property while staying data-driven.
 */
public final class MaterialMapping {

    private final Map<Material, ThermalEmitter> emitters = new EnumMap<>(Material.class);

    /** Builds the default mapping. */
    public MaterialMapping() {
        put(ThermalEmitter.FURNACE, Material.FURNACE, Material.BLAST_FURNACE);
        put(ThermalEmitter.LAVA, Material.LAVA, Material.MAGMA_BLOCK);
        put(ThermalEmitter.CAMPFIRE, Material.CAMPFIRE, Material.SOUL_CAMPFIRE);
        put(ThermalEmitter.FIRE, Material.FIRE, Material.SOUL_FIRE);
        put(ThermalEmitter.ICE, Material.ICE, Material.PACKED_ICE, Material.BLUE_ICE, Material.FROSTED_ICE);
        put(ThermalEmitter.SNOW, Material.SNOW, Material.SNOW_BLOCK, Material.POWDER_SNOW);
    }

    /**
     * @param material the block material
     * @return the emitter it represents, or empty if it is thermally inert
     */
    @NotNull
    public Optional<ThermalEmitter> emitterFor(@NotNull Material material) {
        return Optional.ofNullable(emitters.get(material));
    }

    /**
     * @param material the block material
     * @return {@code true} if the material contributes to the thermal field
     */
    public boolean isRelevant(@NotNull Material material) {
        return emitters.containsKey(material);
    }

    private void put(ThermalEmitter emitter, Material... materials) {
        for (Material material : materials) {
            emitters.put(material, emitter);
        }
    }
}
