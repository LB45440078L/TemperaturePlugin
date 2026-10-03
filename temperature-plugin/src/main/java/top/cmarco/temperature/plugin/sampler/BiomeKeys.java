package top.cmarco.temperature.plugin.sampler;

import org.bukkit.block.Biome;
import org.jetbrains.annotations.NotNull;

/**
 * Extracts the namespaced id of a biome.
 *
 * <p>Isolated so the rest of the plugin never touches the lookup, and so a change in how the server
 * exposes biome keys is a one-line fix here rather than a scatter of edits.
 *
 * <p>{@code Biome} is a {@link org.bukkit.Keyed} on the Bukkit API, so {@link Biome#getKey()} is the
 * correct and portable route, yielding the familiar {@code minecraft:desert} form. The previous
 * implementation used Paper's {@code Registry#getKey(T)}, which does not exist on Spigot — exactly
 * the sort of Paper-only call that compiles happily in development and then fails to load on a
 * Spigot server.
 *
 * <p>A biome that somehow cannot be keyed degrades to a neutral id rather than throwing, since an
 * unkeyable biome should behave like a temperate one, not crash the tick.
 */
public final class BiomeKeys {

    private static final String FALLBACK = "minecraft:plains";

    private BiomeKeys() {
    }

    /**
     * @param biome the biome
     * @return the namespaced id, e.g. {@code minecraft:desert}, or {@code minecraft:plains} if none
     */
    @NotNull
    public static String keyOf(@NotNull Biome biome) {
        try {
            var key = biome.getKey();
            if (key != null) {
                return key.toString();
            }
        } catch (RuntimeException ignored) {
            // Fall through to the neutral id below.
        }
        return FALLBACK;
    }
}
