package top.cmarco.temperature.core.climate;

/**
 * The broad climate families a biome can belong to.
 *
 * <p>The simulation does not build a bespoke temperature for every one of Minecraft's ~70 biomes.
 * Instead each biome is mapped (by the {@link BiomeClimateRegistry}) onto one of these archetypes,
 * and the archetype carries a base temperature band and a base humidity band. That keeps the model
 * legible and gives operators six numbers to tune instead of seventy.
 *
 * <p>This is the same decomposition the legacy plugin used, with two changes: the "standard"
 * family is renamed {@link #TEMPERATE} for clarity, and the End gets its own {@link #END} family
 * rather than borrowing the temperate one, so an operator can make the End cold without touching
 * overworld defaults.
 */
public enum ClimateArchetype {

    /** Temperate land: plains, most forests, rivers, hills, meadows. */
    TEMPERATE("temperate"),

    /** Cold land and ice: taiga, snowy biomes, frozen peaks. */
    COLD("cold"),

    /** Hot land: desert, jungle, savanna, badlands. */
    WARM("warm"),

    /** Temperate water: lukewarm and warm oceans, most seas. */
    OCEAN("ocean"),

    /** Cold water: cold and frozen oceans. */
    COLD_OCEAN("cold-ocean"),

    /** The Nether. Hot and dry by default. */
    NETHER("nether"),

    /** The End. Cool, dry and airless by default. */
    END("end");

    private final String key;

    ClimateArchetype(String key) {
        this.key = key;
    }

    /** @return a stable lower-case key, used in the configuration file. */
    public String key() {
        return key;
    }
}
