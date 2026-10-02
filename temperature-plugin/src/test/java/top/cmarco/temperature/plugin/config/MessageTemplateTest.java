package top.cmarco.temperature.plugin.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

/**
 * Guards the shipped {@code messages.yml} against placeholders nothing supplies.
 *
 * <p>A template naming a placeholder the code never fills prints the literal {@code %name%} to a
 * player or the console and reads as a broken plugin, and nothing else catches it — not the compiler,
 * not a rendering test that only checks the keys it already knows about. This walks every string in
 * the shipped file, extracts every {@code %placeholder%}, and requires each to be one the plugin's
 * send calls actually provide.
 */
class MessageTemplateTest {

    private static final Pattern PLACEHOLDER = Pattern.compile("%([A-Za-z0-9_-]+)%");

    /**
     * Every placeholder name any {@code messages.send(...)} call supplies, plus the ones the loader
     * substitutes itself.
     */
    private static final Set<String> SUPPLIED = Set.of(
            "permission", "subcommand",
            "usage", "description",
            "temperature", "air", "apparent", "biome", "humidity", "wind", "season",
            "unit", "symbol", "units", "value",
            "ms", "error", "name");

    @Test
    void everyPlaceholderInTheShippedMessagesIsSupplied() {
        YamlConfiguration configuration = new YamlConfiguration();
        try (InputStream stream = MessageTemplateTest.class.getResourceAsStream("/messages.yml")) {
            assertThat(stream).as("messages.yml must be on the classpath").isNotNull();
            configuration.load(new InputStreamReader(stream, StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException("could not read the bundled messages.yml", e);
        }

        List<String> unknown = new ArrayList<>();
        for (String key : configuration.getKeys(true)) {
            for (String template : configuration.getStringList(key)) {
                collectUnknown(template, key, unknown);
            }
            String scalar = configuration.getString(key);
            if (scalar != null) {
                collectUnknown(scalar, key, unknown);
            }
        }
        assertThat(unknown).as("placeholders no code supplies").isEmpty();
    }

    private void collectUnknown(String template, String key, List<String> unknown) {
        Matcher matcher = PLACEHOLDER.matcher(template);
        while (matcher.find()) {
            String name = matcher.group(1);
            if (!SUPPLIED.contains(name)) {
                unknown.add(key + " -> %" + name + '%');
            }
        }
    }
}
