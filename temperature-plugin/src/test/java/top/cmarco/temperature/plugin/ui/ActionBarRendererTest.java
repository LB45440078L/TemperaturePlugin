package top.cmarco.temperature.plugin.ui;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.EnumMap;
import java.util.Map;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.Test;
import top.cmarco.temperature.core.climate.Season;
import top.cmarco.temperature.core.unit.TemperatureUnit;
import top.cmarco.temperature.plugin.config.DisplaySettings;

class ActionBarRendererTest {

    private final ActionBarRenderer renderer = new ActionBarRenderer();

    private DisplaySettings display() {
        Map<Season, String> labels = new EnumMap<>(Season.class);
        for (Season season : Season.values()) {
            labels.put(season, season.displayName());
        }
        return new DisplaySettings(true, TemperatureUnit.CELSIUS, "&7[{BAR}&7] {TEMP} {SEASON}",
                "%.1f", 10, "|", ".", "&b", "&a", "&c", 5.0, 29.5, labels);
    }

    @Test
    void barIsEmptyAtTheColdExtreme() {
        assertThat(renderer.bar(-40.0, display())).doesNotContain("|");
    }

    @Test
    void barIsFullAtTheWarmExtreme() {
        assertThat(count(renderer.bar(40.0, display()), '|')).isEqualTo(10);
    }

    @Test
    void barIsHalfFullAtTheMidpoint() {
        assertThat(count(renderer.bar(0.0, display()), '|')).isEqualTo(5);
    }

    @Test
    void barIsClampedBeyondTheRange() {
        assertThat(count(renderer.bar(-500.0, display()), '|')).isZero();
        assertThat(count(renderer.bar(500.0, display()), '|')).isEqualTo(10);
    }

    @Test
    void renderSubstitutesEveryToken() {
        Component component = renderer.render(TemperatureUnit.CELSIUS, 21.0, Season.SUMMER, display());
        String text = PlainTextComponentSerializer.plainText().serialize(component);

        assertThat(text).contains("21.0").contains("Summer").doesNotContain("{BAR}")
                .doesNotContain("{TEMP}").doesNotContain("{SEASON}");
    }

    @Test
    void renderUsesThePlayersUnit() {
        Component fahrenheit = renderer.render(TemperatureUnit.FAHRENHEIT, 20.0, Season.SPRING, display());
        String text = PlainTextComponentSerializer.plainText().serialize(fahrenheit);
        assertThat(text).contains("68.0");
    }

    private static int count(String haystack, char needle) {
        int total = 0;
        for (int i = 0; i < haystack.length(); i++) {
            if (haystack.charAt(i) == needle) {
                total++;
            }
        }
        return total;
    }
}
