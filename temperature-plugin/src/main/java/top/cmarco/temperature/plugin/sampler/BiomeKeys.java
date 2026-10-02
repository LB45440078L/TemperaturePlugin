package top.cmarco.temperature.plugin.sampler;

import org.bukkit.Registry;
import org.bukkit.block.Biome;
import org.jetbrains.annotations.NotNull;

/**
 * Extracts the namespaced id of a biome.
 *
 * <p>Isolated so the rest of the plugin never touches the registry lookup, and so a change in how
 * Paper exposes biome keys is a one-line fix here rather than a scatter of edits. A biome that the
 * registry cannot key degrades to a neutral id rather than throwing, since an unkeyable biome should
 * behave like a temperate one, not crash the tick.
 */
public final class BiomeKeys {

    private BiomeKeys() {
    }

    /**
     * @param biome the biome
     * @return the namespaced id, e.g. {@code minecraft:desert}, or {@code minecraft:plains} if none
     */
    @NotNull
    public static String keyOf(@NotNull Biome biome) {
        try {
            var key = Registry.BIOME.getKey(biome);
            if (key != null) {
                return key.toString();
            }
        } catch (RuntimeException ignored) {
            // Fall through to the neutral id below.
        }
        return "minecraft:plains";
    }
}
