package top.cmarco.temperature.plugin.command.sub;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import top.cmarco.temperature.core.service.TemperatureReading;
import top.cmarco.temperature.core.unit.TemperatureUnit;
import top.cmarco.temperature.plugin.TemperaturePlugin;
import top.cmarco.temperature.plugin.command.SubCommand;

/**
 * Reports a player's current reading and the environment behind it.
 *
 * <p>Everything shown is read from the last simulation step rather than recomputed, so the number a
 * player sees is the number the physics actually used.
 */
public final class StatusSubCommand implements SubCommand {

    private final TemperaturePlugin plugin;

    /**
     * @param plugin the owning plugin
     */
    public StatusSubCommand(@NotNull TemperaturePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String name() {
        return "status";
    }

    @Override
    public @NotNull List<String> aliases() {
        return List.of("info", "check");
    }

    @Override
    public @NotNull String description() {
        return "Show your current reading and its environment.";
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.messages().send(sender, "players-only", Map.of());
            return;
        }
        Optional<TemperatureReading> optional = plugin.service().lastReading(player);
        if (optional.isEmpty()) {
            plugin.messages().send(player, "status.no-reading", Map.of());
            return;
        }

        TemperatureReading reading = optional.get();
        TemperatureUnit unit = plugin.service().unitOf(player);
        plugin.messages().send(player, "status.line", Map.of(
                "temperature", String.format("%.1f%s",
                        unit.fromCelsius(reading.bodyCelsius()), unit.symbol()),
                "air", String.format(Locale.ROOT, "%.1f", reading.ambient().airCelsius()),
                "apparent", String.format(Locale.ROOT, "%.1f", reading.ambient().apparentCelsius()),
                "biome", player.getLocation().getBlock().getBiome().toString(),
                "humidity", String.format(Locale.ROOT, "%.0f", reading.ambient().humidityPercent()),
                "wind", String.format(Locale.ROOT, "%.0f", reading.ambient().windSpeedKmh()),
                "season", reading.ambient().season().displayName()));
    }
}
