package top.cmarco.temperature.core.climate;

/**
 * A biome's resolved climate: which family it belongs to and the band that family spans.
 *
 * <p>This is the value the sampler looks up once per reading, so it is a small immutable record —
 * cheap to create, safe to cache and trivial to assert against in tests.
 *
 * @param biomeKey the namespaced biome id, e.g. {@code minecraft:desert}
 * @param archetype the climate family the biome was mapped onto
 * @param range     the temperature and humidity band the family spans
 */
public record BiomeClimate(String biomeKey, ClimateArchetype archetype, ClimateRange range) {

    public BiomeClimate {
        if (biomeKey == null || biomeKey.isBlank()) {
            throw new IllegalArgumentException("biomeKey must not be blank");
        }
        if (archetype == null || range == null) {
            throw new IllegalArgumentException("archetype and range are required");
        }
    }
}
