package top.cmarco.temperature.plugin.config;

import java.util.Map;
import top.cmarco.temperature.core.climate.Season;
import top.cmarco.temperature.core.unit.TemperatureUnit;

/**
 * Everything the action bar needs, in one immutable bundle.
 *
 * @param enabled            whether the action bar is drawn at all
 * @param defaultUnit        the unit given to a player who has never chosen one
 * @param actionBarFormat    the template, supporting {@code {BAR}}, {@code {TEMP}} and {@code {SEASON}}
 * @param numberFormat       a {@link String#format} pattern for the temperature number
 * @param barLength          how many cells the progress bar has
 * @param barReached         the glyph for a filled cell
 * @param barUnreached       the glyph for an empty cell
 * @param coldColour         the colour code used below {@code coldThreshold}
 * @param temperateColour    the colour code used between the two thresholds
 * @param hotColour          the colour code used above {@code hotThreshold}
 * @param coldThreshold      the temperature below which the cold colour applies, in Celsius
 * @param hotThreshold       the temperature above which the hot colour applies, in Celsius
 * @param seasonLabels       the coloured label for each season
 */
public record DisplaySettings(
        boolean enabled,
        TemperatureUnit defaultUnit,
        String actionBarFormat,
        String numberFormat,
        int barLength,
        String barReached,
        String barUnreached,
        String coldColour,
        String temperateColour,
        String hotColour,
        double coldThreshold,
        double hotThreshold,
        Map<Season, String> seasonLabels) {

    public DisplaySettings {
        if (barLength < 1) {
            throw new IllegalArgumentException("barLength must be at least 1");
        }
        seasonLabels = Map.copyOf(seasonLabels);
    }

    /**
     * @param celsius a body temperature in degrees Celsius
     * @return the colour code for that temperature
     */
    public String colourFor(double celsius) {
        if (celsius < coldThreshold) {
            return coldColour;
        }
        return celsius > hotThreshold ? hotColour : temperateColour;
    }

    /**
     * @param season the season
     * @return the configured label, or the season's plain name if none was set
     */
    public String label(Season season) {
        return seasonLabels.getOrDefault(season, season.displayName());
    }
}
