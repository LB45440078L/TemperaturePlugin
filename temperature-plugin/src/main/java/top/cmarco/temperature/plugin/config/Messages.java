package top.cmarco.temperature.plugin.config;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * Reads and renders {@code messages.yml}.
 *
 * <p>Two deliberate choices, both learned from the legacy plugin's reload bugs:
 *
 * <ul>
 *   <li>The configuration is held in a {@code volatile} field with an in-place {@link #reload()},
 *       so any object that captured this service keeps seeing the current file. The legacy reload
 *       built a fresh {@code StandardConfig} that the already-constructed managers never observed,
 *       which is why corrected messages appeared everywhere except the alerts being fixed.</li>
 *   <li>Colour codes are translated through Adventure's legacy serializer rather than
 *       {@code ChatColor}, which is deprecated on modern Paper, and are translated at send time so
 *       an operator's edits take effect immediately.</li>
 * </ul>
 */
public final class Messages {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();

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
     * Renders a message to a component, substituting {@code %name%} placeholders.
     *
     * @param key          the message key
     * @param placeholders placeholder name to replacement
     * @return the rendered component
     */
    @NotNull
    public Component component(@NotNull String key, @NotNull Map<String, String> placeholders) {
        return LEGACY.deserialize(apply(prefixedRaw(key), placeholders));
    }

    /**
     * Renders a message without the prefix.
     *
     * @param key          the message key
     * @param placeholders placeholder name to replacement
     * @return the rendered component
     */
    @NotNull
    public Component bareComponent(@NotNull String key, @NotNull Map<String, String> placeholders) {
        return LEGACY.deserialize(apply(raw(key), placeholders));
    }

    /**
     * Sends a message to a sender.
     *
     * @param sender       the recipient
     * @param key          the message key
     * @param placeholders placeholder name to replacement
     */
    public void send(@NotNull CommandSender sender, @NotNull String key, @NotNull Map<String, String> placeholders) {
        sender.sendMessage(component(key, placeholders));
    }

    /**
     * @param key the message key
     * @return the key's template rendered with no placeholders
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

    private static String apply(String template, Map<String, String> placeholders) {
        String result = template;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            result = result.replace('%' + entry.getKey() + '%', entry.getValue());
        }
        return result;
    }
}
