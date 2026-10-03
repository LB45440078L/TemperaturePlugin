package top.cmarco.temperature.plugin.config;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * Reads and renders {@code messages.yml}.
 *
 * <p>Three deliberate choices:
 *
 * <ul>
 *   <li>The configuration is held in a {@code volatile} field with an in-place {@link #reload()}, so
 *       any object that captured this service keeps seeing the current file. The legacy plugin's
 *       reload built a fresh config object that the already-constructed managers never observed,
 *       which is why corrected messages appeared everywhere except the alerts being fixed.</li>
 *   <li>Templates render to plain {@link String}s with legacy {@code &} colour codes translated.
 *       This deliberately avoids Adventure's {@code Component}: Paper bundles Adventure inside its
 *       own jar and Spigot does not, so a single signature mentioning it makes the whole plugin
 *       unloadable on a Spigot server — with a {@code NoClassDefFoundError} during plugin class
 *       initialisation, long before the message code is reached.</li>
 *   <li>Translation happens at send time, so an operator's edits take effect immediately.</li>
 * </ul>
 */
public final class Messages {

    private final Plugin plugin;
    private volatile FileConfiguration configuration;
    private volatile String prefix;

    /**
     * @param plugin the owning plugin, used for the data folder and the resource
     */
    public Messages(@NotNull Plugin plugin) {
        this.plugin = plugin;
        reload();
    }

    /** Re-reads {@code messages.yml} from disk, exporting the bundled default if it is absent. */
    public void reload() {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        this.configuration = YamlConfiguration.loadConfiguration(file);
        this.prefix = configuration.getString("prefix", "");
    }

    /**
     * @param key the message key
     * @return the raw template, or a visible {@code <missing: key>} marker if the key is unmapped
     */
    @NotNull
    public String raw(@NotNull String key) {
        String value = configuration.getString(key);
        if (value == null) {
            plugin.getLogger().warning("messages.yml has no entry for key '" + key + "'");
            return "<missing:" + key + '>';
        }
        return value;
    }

    /**
     * @param key the message key
     * @return the template with the prefix prepended
     */
    @NotNull
    public String prefixedRaw(@NotNull String key) {
        return prefix + raw(key);
    }

    /**
     * Renders a prefixed, coloured message.
     *
     * @param key          the message key
     * @param placeholders placeholder name to replacement
     * @return the rendered text, ready to send
     */
    @NotNull
    public String render(@NotNull String key, @NotNull Map<String, String> placeholders) {
        return colour(apply(prefixedRaw(key), placeholders));
    }

    /**
     * Renders a coloured message without the prefix.
     *
     * @param key          the message key
     * @param placeholders placeholder name to replacement
     * @return the rendered text, ready to send
     */
    @NotNull
    public String renderBare(@NotNull String key, @NotNull Map<String, String> placeholders) {
        return colour(apply(raw(key), placeholders));
    }

    /**
     * Sends a message to a sender.
     *
     * @param sender       the recipient
     * @param key          the message key
     * @param placeholders placeholder name to replacement
     */
    public void send(@NotNull CommandSender sender, @NotNull String key,
                     @NotNull Map<String, String> placeholders) {
        sender.sendMessage(render(key, placeholders));
    }

    /**
     * @param key the message key
     * @return the key's template, uncoloured and unsubstituted
     */
    @NotNull
    public String plain(@NotNull String key) {
        return raw(key);
    }

    /** @return an empty, immutable placeholder map, for callers with nothing to substitute. */
    @NotNull
    public static Map<String, String> noPlaceholders() {
        return Map.of();
    }

    /**
     * Convenience for a single placeholder.
     *
     * @param name  the placeholder name
     * @param value the replacement
     * @return a one-entry map
     */
    @NotNull
    public static Map<String, String> of(@NotNull String name, @NotNull Object value) {
        Map<String, String> map = new HashMap<>(1);
        map.put(name, String.valueOf(value));
        return map;
    }

    /**
     * Translates {@code &}-prefixed colour codes.
     *
     * <p>Delegates to the server's own translator rather than a blanket {@code &} to {@code §}
     * replace, so a literal ampersand survives. Note the documented exception: an {@code &} followed
     * by a colour character <em>is</em> a code, so {@code R&D} renders as {@code R} plus a colour
     * code. That is a property of every ampersand-colour system, not a bug here.
     *
     * @param text the template
     * @return the text with colour codes translated
     */
    @NotNull
    public static String colour(@NotNull String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    private static String apply(String template, Map<String, String> placeholders) {
        String result = template;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            result = result.replace('%' + entry.getKey() + '%', entry.getValue());
        }
        return result;
    }
}
