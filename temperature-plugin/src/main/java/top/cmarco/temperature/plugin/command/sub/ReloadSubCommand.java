package top.cmarco.temperature.plugin.command.sub;

import java.util.Map;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import top.cmarco.temperature.plugin.TemperaturePlugin;
import top.cmarco.temperature.plugin.command.SubCommand;
import top.cmarco.temperature.plugin.permission.Permissions;

/**
 * Reloads the configuration.
 *
 * <p>Runs for console as well as players — a reload is exactly the kind of thing an operator wants
 * to do from the console — and reports the failure reason when a reload throws, instead of leaving
 * the operator to find it in the log.
 */
public final class ReloadSubCommand implements SubCommand {

    private final TemperaturePlugin plugin;

    /**
     * @param plugin the owning plugin
     */
    public ReloadSubCommand(@NotNull TemperaturePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String name() {
        return "reload";
    }

    @Override
    public @NotNull String permission() {
        return Permissions.RELOAD;
    }

    @Override
    public @NotNull String description() {
        return "Reload config.yml and messages.yml.";
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        long startedAt = System.currentTimeMillis();
        try {
            plugin.reload();
            plugin.messages().send(sender, "reload.success", Map.of(
                    "ms", String.valueOf(System.currentTimeMillis() - startedAt)));
        } catch (RuntimeException e) {
            plugin.getLogger().warning("Reload failed: " + e.getMessage());
            plugin.messages().send(sender, "reload.failed", Map.of(
                    "error", String.valueOf(e.getMessage())));
        }
    }
}
