package top.cmarco.temperature.plugin.command;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import top.cmarco.temperature.plugin.TemperaturePlugin;
import top.cmarco.temperature.plugin.command.sub.DebugSubCommand;
import top.cmarco.temperature.plugin.command.sub.HelpSubCommand;
import top.cmarco.temperature.plugin.command.sub.ReloadSubCommand;
import top.cmarco.temperature.plugin.command.sub.StatusSubCommand;
import top.cmarco.temperature.plugin.command.sub.ToggleSubCommand;
import top.cmarco.temperature.plugin.command.sub.UnitSubCommand;

/**
 * Dispatches {@code /temperature} and its subcommands.
 *
 * <p>Resolution, permission checking, unknown-name handling and tab completion happen once, here, so
 * no individual subcommand can forget a check. An unknown name is reported rather than ignored, and
 * the help text is generated from the registered subcommands, so the two can never drift.
 */
public final class TemperatureCommand implements TabExecutor {

    private final TemperaturePlugin plugin;
    private final Map<String, SubCommand> byName = new LinkedHashMap<>();
    private final List<SubCommand> ordered = new ArrayList<>();

    /**
     * @param plugin the owning plugin
     */
    public TemperatureCommand(@NotNull TemperaturePlugin plugin) {
        this.plugin = plugin;
        register(new StatusSubCommand(plugin));
        register(new ToggleSubCommand(plugin));
        register(new UnitSubCommand(plugin));
        register(new DebugSubCommand(plugin));
        register(new ReloadSubCommand(plugin));
        register(new HelpSubCommand(plugin, this));
    }

    private void register(SubCommand subCommand) {
        ordered.add(subCommand);
        byName.put(subCommand.name().toLowerCase(Locale.ROOT), subCommand);
        for (String alias : subCommand.aliases()) {
            byName.putIfAbsent(alias.toLowerCase(Locale.ROOT), subCommand);
        }
    }

    /** @return the registered subcommands, in display order. */
    @NotNull
    public List<SubCommand> subCommands() {
        return List.copyOf(ordered);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            return help().map(sub -> run(sub, sender, new String[0])).orElse(true);
        }
        SubCommand subCommand = byName.get(args[0].toLowerCase(Locale.ROOT));
        if (subCommand == null) {
            plugin.messages().send(sender, "unknown-subcommand",
                    Map.of("subcommand", args[0]));
            return true;
        }
        return run(subCommand, sender, java.util.Arrays.copyOfRange(args, 1, args.length));
    }

    private boolean run(SubCommand subCommand, CommandSender sender, String[] args) {
        String permission = subCommand.permission();
        if (permission != null && !sender.hasPermission(permission)) {
            plugin.messages().send(sender, "no-permission", Map.of("permission", permission));
            return true;
        }
        subCommand.execute(sender, args);
        return true;
    }

    private Optional<SubCommand> help() {
        return Optional.ofNullable(byName.get("help"));
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, @NotNull String[] args) {
        if (args.length <= 1) {
            String partial = args.length == 0 ? "" : args[0].toLowerCase(Locale.ROOT);
            List<String> names = new ArrayList<>();
            for (SubCommand subCommand : ordered) {
                String permission = subCommand.permission();
                if (permission == null || sender.hasPermission(permission)) {
                    if (subCommand.name().startsWith(partial)) {
                        names.add(subCommand.name());
                    }
                }
            }
            return names;
        }
        SubCommand subCommand = byName.get(args[0].toLowerCase(Locale.ROOT));
        if (subCommand == null) {
            return List.of();
        }
        String permission = subCommand.permission();
        if (permission != null && !sender.hasPermission(permission)) {
            return List.of();
        }
        return subCommand.complete(sender, java.util.Arrays.copyOfRange(args, 1, args.length));
    }
}
