package top.cmarco.temperature.core.climate;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Maps a biome onto a {@link ClimateArchetype}.
 *
 * <p>Two mechanisms, consulted in order, so that a sensible answer always exists:
 *
 * <ol>
 *   <li>An explicit table. Every vanilla biome the plugin knows about is listed here once, and an
 *       operator's configuration can add to or override the table.</li>
 *   <li>A keyword classifier. A biome that is not in the table — a datapack biome, or one added by
 *       a future Minecraft release — is classified from the words in its id. This is the layer that
 *       keeps the plugin working on content it has never seen; it is deliberately the fallback, not
 *       the primary path, because keyword matching is fragile when it is load-bearing.</li>
 * </ol>
 *
 * <p>The legacy plugin classified <em>only</em> by keyword, and its expression had an operator
 * precedence bug that made the cold branch unreachable. A table plus a tested fallback removes
 * that whole class of defect.
 *
 * <p>Instances are mutable during configuration load and treated as frozen thereafter. Access from
 * the server thread only.
 */
public final class BiomeClimateRegistry {

    private final Map<String, ClimateArchetype> table = new HashMap<>();

    /** Creates a registry pre-populated with a mapping for every vanilla biome. */
    public BiomeClimateRegistry() {
        installVanillaDefaults();
    }

    /**
     * Overrides or adds a mapping.
     *
     * @param biomeKey the biome id, with or without its {@code minecraft:} namespace
     * @param archetype the family to classify it as
     */
    public void register(String biomeKey, ClimateArchetype archetype) {
        table.put(normalise(biomeKey), Objects.requireNonNull(archetype, "archetype"));
    }

    /**
     * Resolves a biome id to its climate family.
     *
     * @param biomeKey the biome id, with or without its namespace
     * @return the mapped family, or the keyword guess when the id is unknown
     */
    public ClimateArchetype archetypeOf(String biomeKey) {
        String key = normalise(biomeKey);
        ClimateArchetype mapped = table.get(key);
        return mapped != null ? mapped : classifyByKeyword(key);
    }

    /**
     * Builds a fully resolved {@link BiomeClimate} for a biome.
     *
     * @param biomeKey the biome id, with or without its namespace
     * @param ranges   a lookup from climate family to the band that family spans
     * @return the resolved climate
     */
    public BiomeClimate resolve(String biomeKey, Function<ClimateArchetype, ClimateRange> ranges) {
        String key = normalise(biomeKey);
        ClimateArchetype archetype = archetypeOf(key);
        ClimateRange range = ranges.apply(archetype);
        if (range == null) {
            throw new IllegalStateException("No climate range configured for " + archetype);
        }
        return new BiomeClimate("minecraft:" + key, archetype, range);
    }

    /** @return an unmodifiable copy of the explicit table, primarily for diagnostics. */
    public Map<String, ClimateArchetype> mappings() {
        return Map.copyOf(table);
    }

    private static String normalise(String biomeKey) {
        if (biomeKey == null) {
            return "";
        }
        String key = biomeKey.trim().toLowerCase(Locale.ROOT);
        int colon = key.indexOf(':');
        return colon >= 0 ? key.substring(colon + 1) : key;
    }

    /**
     * Classifies an unknown biome from its id.
     *
     * <p>Order matters and is asserted by tests: nether and end first (they contain words like
     * "forest" that would otherwise match a land family), then oceans, then cold, then warm, and
     * temperate as the final default.
     */
    static ClimateArchetype classifyByKeyword(String key) {
        if (key.isEmpty()) {
            return ClimateArchetype.TEMPERATE;
        }
        if (containsAny(key, "nether", "soul_sand", "crimson", "warped", "basalt")) {
            return ClimateArchetype.NETHER;
        }
        if (containsAny(key, "end_", "the_end")) {
            return ClimateArchetype.END;
        }
        if (key.contains("ocean")) {
            return containsAny(key, "frozen", "cold") ? ClimateArchetype.COLD_OCEAN : ClimateArchetype.OCEAN;
        }
        if (containsAny(key, "snow", "frozen", "ice", "taiga", "grove", "peak", "slope")) {
            return ClimateArchetype.COLD;
        }
        if (containsAny(key, "desert", "jungle", "savanna", "badland", "mesa", "sand")) {
            return ClimateArchetype.WARM;
        }
        return ClimateArchetype.TEMPERATE;
    }

    private static boolean containsAny(String haystack, String... needles) {
        for (String needle : needles) {
            if (haystack.contains(needle)) {
                return true;
            }
        }
        return false;
    }

    private void installVanillaDefaults() {
        putAll(ClimateArchetype.OCEAN,
                "ocean", "deep_ocean", "warm_ocean", "lukewarm_ocean", "deep_lukewarm_ocean");
        putAll(ClimateArchetype.COLD_OCEAN,
                "cold_ocean", "deep_cold_ocean", "frozen_ocean", "deep_frozen_ocean");

        putAll(ClimateArchetype.COLD,
                "snowy_plains", "ice_spikes", "snowy_taiga", "snowy_beach", "frozen_river",
                "snowy_slopes", "frozen_peaks", "jagged_peaks", "grove", "taiga",
                "old_growth_pine_taiga", "old_growth_spruce_taiga");

        putAll(ClimateArchetype.WARM,
                "desert", "jungle", "sparse_jungle", "bamboo_jungle", "savanna",
                "savanna_plateau", "windswept_savanna", "badlands", "eroded_badlands",
                "wooded_badlands");

        putAll(ClimateArchetype.TEMPERATE,
                "plains", "sunflower_plains", "forest", "flower_forest", "birch_forest",
                "dark_forest", "old_growth_birch_forest", "pale_garden", "river", "beach",
                "stony_shore", "swamp", "mangrove_swamp", "windswept_hills",
                "windswept_gravelly_hills", "windswept_forest", "mushroom_fields", "meadow",
                "cherry_grove", "lush_caves", "dripstone_caves", "deep_dark", "stony_peaks",
                "the_void");

        putAll(ClimateArchetype.NETHER,
                "nether_wastes", "soul_sand_valley", "crimson_forest", "warped_forest",
                "basalt_deltas");

        putAll(ClimateArchetype.END,
                "the_end", "small_end_islands", "end_midlands", "end_highlands", "end_barrens");
    }

    private void putAll(ClimateArchetype archetype, String... keys) {
        for (String key : keys) {
            table.put(key, archetype);
        }
    }
}
