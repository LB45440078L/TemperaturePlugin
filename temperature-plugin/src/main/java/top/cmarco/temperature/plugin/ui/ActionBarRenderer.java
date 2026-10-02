package top.cmarco.temperature.plugin.ui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;
import top.cmarco.temperature.core.climate.Season;
import top.cmarco.temperature.core.unit.TemperatureUnit;
import top.cmarco.temperature.plugin.config.DisplaySettings;

/**
 * Renders a reading into the action bar.
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

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();

    /**
     * Renders a reading.
     *
     * @param unit         the player's display unit
     * @param bodyCelsius  the reading, in degrees Celsius
     * @param season       the season to label
     * @param display      the display tunables
     * @return the rendered component
     */
    @NotNull
    public Component render(@NotNull TemperatureUnit unit, double bodyCelsius, @NotNull Season season,
                            @NotNull DisplaySettings display) {
        String colour = display.colourFor(bodyCelsius);
        String temperature = colour
                + String.format(display.numberFormat(), unit.fromCelsius(bodyCelsius))
                + unit.symbol();
        String rendered = display.actionBarFormat()
                .replace("{BAR}", bar(bodyCelsius, display))
                .replace("{TEMP}", temperature)
                .replace("{SEASON}", display.label(season));
        return LEGACY.deserialize(rendered);
    }

    /**
     * Renders the progress bar alone.
     *
     * @param bodyCelsius the reading, in degrees Celsius
     * @param display     the display tunables
     * @return a coloured bar string
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
