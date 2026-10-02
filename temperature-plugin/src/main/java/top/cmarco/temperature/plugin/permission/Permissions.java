package top.cmarco.temperature.plugin.permission;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * The plugin's permission nodes, in one place.
 *
 * <p>Keeping them as constants rather than string literals scattered through commands and listeners
 * means a renamed node is a compile error, not a silently broken check that inspects
 * {@code plugin.yml} for a permission nobody grants any more.
 */
public final class Permissions {

    /** Umbrella node granting everything below. */
    public static final String ALL = "temperature.*";

    /** Receive a simulated temperature at all. */
    public static final String USE = "temperature.use";

    /** Toggle one's own display. */
    public static final String TOGGLE = "temperature.toggle";

    /** Change one's own display unit. */
    public static final String UNIT = "temperature.unit";

    /** Reload the configuration. */
    public static final String RELOAD = "temperature.reload";

    /** Receive threshold alerts for other players. */
    public static final String NOTIFY = "temperature.notify";

    private Permissions() {
    }

    /**
     * @param sender the sender to test
     * @param node   the permission node
     * @return {@code true} if the sender holds the node
     */
    public static boolean has(@NotNull CommandSender sender, @NotNull String node) {
        return sender.hasPermission(node);
    }
}
