package top.cmarco.temperature.plugin.command.sub;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import top.cmarco.temperature.plugin.TemperaturePlugin;
import top.cmarco.temperature.plugin.command.SubCommand;
import top.cmarco.temperature.plugin.permission.Permissions;

/**
 * Prints each modifier's contribution to the player's reading.
 *
 * <p>This is the tool a baffled operator needs: instead of "why am I on fire", it answers with the
 * biome's baseline, the season's offset, what the nearby blocks contributed and how much the armour
 * added — the same numbers the pipeline actually summed.
 */
public final class DebugSubCommand implements SubCommand {

    private final TemperaturePlugin plugin;

    /**
     * @param plugin the owning plugin
     */
    public DebugSubCommand(@NotNull TemperaturePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String name() {
        return "debug";
    }

    @Override
    public @NotNull String permission() {
        return Permissions.RELOAD;
    }

    @Override
    public @NotNull String description() {
        return "Explain the contributions behind your reading.";
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.messages().send(sender, "players-only", Map.of());
            return;
        }
        Map<String, Double> contributions = plugin.service().diagnose(player);
        plugin.messages().send(player, "debug.header", Map.of());
        double total = 0.0;
        for (Map.Entry<String, Double> entry : contributions.entrySet()) {
            total += entry.getValue();
            plugin.messages().send(player, "debug.line", Map.of(
                    "name", entry.getKey(),
                    "value", String.format(Locale.ROOT, "%+.2f", entry.getValue())));
        }
        plugin.messages().send(player, "debug.total", Map.of(
                "value", String.format(Locale.ROOT, "%+.2f", total)));
    }

    @Override
    public @NotNull List<String> complete(@NotNull CommandSender sender, @NotNull String[] args) {
        return new ArrayList<>();
    }
}
