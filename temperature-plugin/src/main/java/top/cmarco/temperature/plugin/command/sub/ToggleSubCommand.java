package top.cmarco.temperature.plugin.command.sub;

import java.util.Map;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import top.cmarco.temperature.plugin.TemperaturePlugin;
import top.cmarco.temperature.plugin.command.SubCommand;
import top.cmarco.temperature.plugin.permission.Permissions;

/** Switches a player's action bar on or off. */
public final class ToggleSubCommand implements SubCommand {

    private final TemperaturePlugin plugin;

    /**
     * @param plugin the owning plugin
     */
    public ToggleSubCommand(@NotNull TemperaturePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String name() {
        return "toggle";
    }

    @Override
    public @NotNull java.util.List<String> aliases() {
        return java.util.List.of("hide", "show");
    }

    @Override
    public @NotNull String permission() {
        return Permissions.TOGGLE;
    }

    @Override
    public @NotNull String description() {
        return "Turn your temperature display on or off.";
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.messages().send(sender, "players-only", Map.of());
            return;
        }
        var preferences = plugin.service().preferences();
        boolean enabled = !preferences.displayEnabled(player);
        preferences.setDisplayEnabled(player, enabled);
        plugin.messages().send(player, enabled ? "toggle.now-on" : "toggle.now-off", Map.of());
    }
}
