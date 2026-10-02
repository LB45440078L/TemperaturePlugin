package top.cmarco.temperature.plugin.command.sub;

import java.util.List;
import java.util.Map;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import top.cmarco.temperature.plugin.TemperaturePlugin;
import top.cmarco.temperature.plugin.command.SubCommand;
import top.cmarco.temperature.plugin.command.TemperatureCommand;

/**
 * Lists the available subcommands.
 *
 * <p>Built from the command's own registry, so a new subcommand appears in help automatically and a
 * subcommand a sender may not use is hidden rather than advertised and then refused.
 */
public final class HelpSubCommand implements SubCommand {

    private final TemperaturePlugin plugin;
    private final TemperatureCommand command;

    /**
     * @param plugin  the owning plugin
     * @param command the command providing the subcommand list
     */
    public HelpSubCommand(@NotNull TemperaturePlugin plugin, @NotNull TemperatureCommand command) {
        this.plugin = plugin;
        this.command = command;
    }

    @Override
    public @NotNull String name() {
        return "help";
    }

    @Override
    public @NotNull List<String> aliases() {
        return List.of("?");
    }

    @Override
    public @NotNull String description() {
        return "Show this command list.";
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        plugin.messages().send(sender, "help.header", Map.of());
        for (SubCommand subCommand : command.subCommands()) {
            String permission = subCommand.permission();
            if (permission != null && !sender.hasPermission(permission)) {
                continue;
            }
            plugin.messages().send(sender, "help.line", Map.of(
                    "usage", subCommand.usage(),
                    "description", subCommand.description()));
        }
    }
}
