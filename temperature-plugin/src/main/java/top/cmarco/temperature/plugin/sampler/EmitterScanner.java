package top.cmarco.temperature.plugin.sampler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Furnace;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import top.cmarco.temperature.core.environment.EmitterSample;
import top.cmarco.temperature.core.environment.ThermalEmitter;

/**
 * Finds the thermally relevant blocks around a player.
 *
 * <p>Scans a box of {@code (2h+1) x (2v+1) x (2h+1)} blocks centred on the player, skipping air, and
 * emits a sample for each block that maps to an emitter and is actually emitting. The scan radii
 * come from configuration, so an operator can trade fidelity for cost.
 *
 * <p>Two refinements over the legacy scan, both visible to a player:
 *
 * <ul>
 *   <li>A furnace only counts while it is burning and a campfire only while it is lit. The legacy
 *       code checked the furnace's burn time but treated every campfire as hot, lit or not.</li>
 *   <li>Distances are measured to the block's centre rather than its corner, so the field is not
 *       skewed by the integer block origin and is symmetric around a source.</li>
 * </ul>
 */
public final class EmitterScanner {

    private final MaterialMapping mapping;
    private final int radiusHorizontal;
    private final int radiusVertical;

    /**
     * @param mapping          the material-to-emitter table
     * @param radiusHorizontal the scan half-extent on the X and Z axes
     * @param radiusVertical   the scan half-extent on the Y axis
     */
    public EmitterScanner(@NotNull MaterialMapping mapping, int radiusHorizontal, int radiusVertical) {
        this.mapping = mapping;
        this.radiusHorizontal = radiusHorizontal;
        this.radiusVertical = radiusVertical;
    }

    /**
     * @param player the player to scan around
     * @return every emitting block within the scan box
     */
    @NotNull
    public List<EmitterSample> scan(@NotNull Player player) {
        Location origin = player.getLocation();
        World world = origin.getWorld();
        if (world == null) {
            return List.of();
        }

        List<EmitterSample> samples = new ArrayList<>();
        int baseX = origin.getBlockX();
        int baseY = origin.getBlockY();
        int baseZ = origin.getBlockZ();

        for (int dx = -radiusHorizontal; dx <= radiusHorizontal; dx++) {
            for (int dz = -radiusHorizontal; dz <= radiusHorizontal; dz++) {
                for (int dy = -radiusVertical; dy <= radiusVertical; dy++) {
                    Block block = world.getBlockAt(baseX + dx, baseY + dy, baseZ + dz);
                    Optional<ThermalEmitter> emitter = mapping.emitterFor(block.getType());
                    if (emitter.isEmpty() || !isEmitting(block, emitter.get())) {
                        continue;
                    }
                    Location centre = block.getLocation().add(0.5, 0.5, 0.5);
                    samples.add(EmitterSample.of(emitter.get(), origin.distanceSquared(centre)));
                }
            }
        }
        return samples;
    }

    private boolean isEmitting(Block block, ThermalEmitter emitter) {
        return switch (emitter) {
            case FURNACE -> block.getState() instanceof Furnace furnace && furnace.getBurnTime() > 0;
            case CAMPFIRE -> block.getBlockData() instanceof org.bukkit.block.data.type.Campfire campfire
                    && campfire.isLit();
            // Lava, fire, ice and snow are always in play when present; ice melts and snow thins on
            // their own schedule and the plugin follows the world rather than second-guessing it.
            case LAVA, FIRE, ICE, SNOW -> true;
        };
    }
}
