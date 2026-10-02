package top.cmarco.temperature.plugin.command.sub;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import top.cmarco.temperature.core.unit.TemperatureUnit;
import top.cmarco.temperature.plugin.TemperaturePlugin;
import top.cmarco.temperature.plugin.command.SubCommand;
import top.cmarco.temperature.plugin.permission.Permissions;

/** Changes the unit a player's temperature is displayed in. */
public final class UnitSubCommand implements SubCommand {

    private final TemperaturePlugin plugin;

    /**
     * @param plugin the owning plugin
     */
    public UnitSubCommand(@NotNull TemperaturePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String name() {
        return "unit";
    }

    @Override
    public @NotNull List<String> aliases() {
        return List.of("units", "scale");
    }

    @Override
    public @NotNull String permission() {
        return Permissions.UNIT;
    }

    @Override
    public @NotNull String description() {
        return "Choose the unit your temperature is shown in.";
    }

    @Override
    public @NotNull String usage() {
        return "unit <" + String.join("|", names(false)) + ">";
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.messages().send(sender, "players-only", Map.of());
            return;
        }
        if (args.length < 1) {
            plugin.messages().send(player, "unit.current", Map.of(
                    "unit", plugin.service().unitOf(player).displayName(),
                    "units", String.join(", ", names(true))));
            return;
        }
        var parsed = TemperatureUnit.parse(args[0]);
        if (parsed.isEmpty()) {
            plugin.messages().send(player, "unit.invalid", Map.of(
                    "value", args[0],
                    "units", String.join(", ", names(true))));
            return;
        }
        plugin.service().preferences().setUnit(player, parsed.get());
        plugin.messages().send(player, "unit.changed", Map.of(
                "unit", parsed.get().displayName(),
                "symbol", parsed.get().symbol()));
    }

    @Override
    public @NotNull List<String> complete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length != 1) {
            return List.of();
        }
        String partial = args[0].toUpperCase(Locale.ROOT);
        List<String> matches = new ArrayList<>();
        for (TemperatureUnit unit : TemperatureUnit.values()) {
            if (unit.name().startsWith(partial)) {
                matches.add(unit.name());
            }
        }
        return matches;
    }

    private static List<String> names(boolean capitalised) {
        List<String> names = new ArrayList<>();
        for (TemperatureUnit unit : TemperatureUnit.values()) {
            names.add(capitalised ? unit.displayName() : unit.name().toLowerCase(Locale.ROOT));
        }
        return names;
    }
}
