package top.cmarco.temperature.plugin.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import org.bukkit.Server;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

/**
 * The worlds the plugin simulates.
 *
 * <p>Resolved from configuration once per reload. A configured name that is not currently loaded is
 * remembered and reported rather than silently dropped, because the usual cause is a typo in
 * {@code config.yml} and the legacy plugin reported it only once, at startup, when nobody was
 * watching.
 */
public final class WorldRegistry {

    private final Set<World> worlds = new HashSet<>();
    private final List<String> unresolved;

    /**
     * @param server the server to resolve names against
     * @param names  the configured world names
     * @param logger the logger for warnings
     */
    public WorldRegistry(@NotNull Server server, @NotNull List<String> names, @NotNull Logger logger) {
        List<String> missing = new java.util.ArrayList<>();
        for (String name : names) {
            World world = server.getWorld(name);
            if (world == null) {
                missing.add(name);
            } else {
                worlds.add(world);
            }
        }
        this.unresolved = List.copyOf(missing);
        if (!missing.isEmpty()) {
            logger.warning("config.yml lists worlds that are not loaded: " + String.join(", ", missing));
        }
    }

    /** @return the simulated worlds. */
    @NotNull
    public Set<World> worlds() {
        return Set.copyOf(worlds);
    }

    /**
     * @param world the world
     * @return {@code true} if the world is simulated
     */
    public boolean isSimulated(@NotNull World world) {
        return worlds.contains(world);
    }

    /** @return the configured names that could not be resolved to a loaded world. */
    @NotNull
    public List<String> unresolved() {
        return unresolved;
    }

    /** @return {@code true} if nothing is being simulated. */
    public boolean isEmpty() {
        return worlds.isEmpty();
    }
}
