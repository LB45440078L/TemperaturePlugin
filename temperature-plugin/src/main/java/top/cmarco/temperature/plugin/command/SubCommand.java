package top.cmarco.temperature.plugin.command;

import java.util.Collections;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * One {@code /temperature} subcommand.
 *
 * <p>The command is composed of these rather than a switch over argument zero, so adding a
 * subcommand is adding a class and a registration line, permission handling is uniform, and each
 * subcommand's tab completion lives with its logic instead of in one growing method.
 */
public interface SubCommand {

    /** @return the primary name, without the leading slash. */
    @NotNull
    String name();

    /** @return extra names that also invoke this subcommand. */
    @NotNull
    default List<String> aliases() {
        return Collections.emptyList();
    }

    /** @return the permission required, or {@code null} if anyone may run it. */
    @Nullable
    default String permission() {
        return null;
    }

    /** @return a one-line description, shown by the help subcommand. */
    @NotNull
    String description();

    /** @return the argument synopsis, shown by the help subcommand. */
    @NotNull
    default String usage() {
        return name();
    }

    /**
     * Runs the subcommand.
     *
     * @param sender the sender
     * @param args   the arguments after the subcommand name
     */
    void execute(@NotNull CommandSender sender, @NotNull String[] args);

    /**
     * Completes arguments for this subcommand.
     *
     * @param sender the sender
     * @param args   the arguments typed so far, the last of which is a partial word
     * @return candidate completions
     */
    @NotNull
    default List<String> complete(@NotNull CommandSender sender, @NotNull String[] args) {
        return Collections.emptyList();
    }
}
