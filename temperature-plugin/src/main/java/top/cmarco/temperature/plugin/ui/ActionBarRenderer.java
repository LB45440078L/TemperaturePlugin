package top.cmarco.temperature.plugin.ui;

import org.jetbrains.annotations.NotNull;
import top.cmarco.temperature.core.climate.Season;
import top.cmarco.temperature.core.unit.TemperatureUnit;
import top.cmarco.temperature.plugin.config.DisplaySettings;
import top.cmarco.temperature.plugin.config.Messages;

/**
 * Renders a reading into the action bar.
 *
 * <p>Returns a legacy-coloured {@link String} rather than an Adventure {@code Component}. That is a
 * hard requirement, not a style choice: Paper bundles Adventure inside its own jar and Spigot does
 * not, so any signature mentioning {@code Component} makes the plugin unloadable on Spigot — the
 * failure is a {@code NoClassDefFoundError} while the plugin class initialises, before any of this
 * code ever runs. The caller converts the string to a chat component with the bungee-chat API that
 * Spigot does bundle.
 *
 * <p>The template supports three tokens, all rendered from the canonical Celsius value so a player
 * who switches unit sees the same physics in different numbers rather than a differently-shaped bar:
 *
 * <ul>
 *   <li>{@code {BAR}} — a progress bar with one cell per configured length.</li>
 *   <li>{@code {TEMP}} — the reading in the player's unit, coloured by how extreme it is.</li>
 *   <li>{@code {SEASON}} — the season's coloured label.</li>
 * </ul>
 *
 * <p>The bar spans a fixed physical range — from the coldest to the warmest air the model can
 * produce — so its fill is meaningful rather than relative to the current biome.
 */
public final class ActionBarRenderer {

    /** The physical range the bar spans, in degrees Celsius. */
    private static final double BAR_MIN_CELSIUS = -40.0;
    private static final double BAR_MAX_CELSIUS = 40.0;

    /**
     * Renders a reading.
     *
     * @param unit        the player's display unit
     * @param bodyCelsius the reading, in degrees Celsius
     * @param season      the season to label
     * @param display     the display tunables
     * @return a legacy-coloured string, ready to convert to a chat component
     */
    @NotNull
    public String render(@NotNull TemperatureUnit unit, double bodyCelsius, @NotNull Season season,
                         @NotNull DisplaySettings display) {
        String colour = display.colourFor(bodyCelsius);
        String temperature = colour
                + String.format(display.numberFormat(), unit.fromCelsius(bodyCelsius))
                + unit.symbol();
        String rendered = display.actionBarFormat()
                .replace("{BAR}", bar(bodyCelsius, display))
                .replace("{TEMP}", temperature)
                .replace("{SEASON}", display.label(season));
        return Messages.colour(rendered);
    }

    /**
     * Renders the progress bar alone.
     *
     * @param bodyCelsius the reading, in degrees Celsius
     * @param display     the display tunables
     * @return a coloured bar string, still carrying {@code &} codes
     */
    @NotNull
    public String bar(double bodyCelsius, @NotNull DisplaySettings display) {
        double fraction = (bodyCelsius - BAR_MIN_CELSIUS) / (BAR_MAX_CELSIUS - BAR_MIN_CELSIUS);
        fraction = Math.max(0.0, Math.min(1.0, fraction));
        int filled = (int) Math.round(fraction * display.barLength());
        String colour = display.colourFor(bodyCelsius);

        StringBuilder builder = new StringBuilder(display.barLength() * 4);
        for (int cell = 0; cell < display.barLength(); cell++) {
            builder.append(cell < filled ? colour : "&7")
                    .append(cell < filled ? display.barReached() : display.barUnreached());
        }
        return builder.toString();
    }
}
