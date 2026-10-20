package top.cmarco.temperature.plugin.integration;

import java.util.Locale;
import java.util.Optional;
import java.util.function.ToDoubleFunction;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.cmarco.temperature.core.service.TemperatureReading;
import top.cmarco.temperature.core.unit.TemperatureUnit;
import top.cmarco.temperature.plugin.TemperaturePlugin;
import top.cmarco.temperature.plugin.config.Messages;
import top.cmarco.temperature.plugin.sampler.BiomeKeys;

/**
 * Exposes the simulation to PlaceholderAPI.
 *
 * <p>An <em>internal</em> expansion: it lives inside this plugin rather than being a separate download
 * from the eCloud, so it registers itself at startup and {@link #persist()} returns {@code true} — the
 * two things the PlaceholderAPI documentation requires of an internal expansion (without
 * {@code persist()} PlaceholderAPI unregisters it on a plugin reload and the placeholders silently
 * stop working).
 *
 * <p>{@link #onRequest(OfflinePlayer, String)} is overridden rather than the older
 * {@code onPlaceholderRequest(Player, String)}: it is the method the documentation recommends, and it
 * is the one PlaceholderAPI calls first.
 *
 * <p>Every value is read from the <em>last simulation step</em> rather than recomputed, so a
 * scoreboard and the action bar can never disagree. A player who is offline, or who has not been
 * simulated yet, yields an empty string rather than a wrong number; an identifier this expansion does
 * not know yields {@code null}, which tells PlaceholderAPI the placeholder was invalid so a typo is
 * visible instead of silently rendering as nothing.
 *
 * <p>Numeric values are rendered to one decimal place. The {@code bar} and {@code action_bar}
 * placeholders carry legacy section-sign colour codes.
 */
public final class TemperaturePlaceholderExpansion extends PlaceholderExpansion {

    private final TemperaturePlugin plugin;

    /**
     * @param plugin the owning plugin, used to reach the services and the configuration
     */
    public TemperaturePlaceholderExpansion(@NotNull TemperaturePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "temperature";
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", plugin.getDescription().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(@Nullable OfflinePlayer player, @NotNull String params) {
        if (player == null) {
            return "";
        }
        // Only online players are simulated, so an offline player has no reading to report. The
        // placeholders that come from configuration or the calendar still answer for them.
        Player online = player.getPlayer();

        String key = params.toLowerCase(Locale.ROOT);
        TemperatureUnit unit = online == null
                ? plugin.configuration().display().defaultUnit()
                : plugin.service().unitOf(online);
        Optional<TemperatureReading> reading = online == null
                ? Optional.empty()
                : plugin.service().lastReading(online);

        return switch (key) {
            // ---- the unit and how the player displays it --------------------------------------
            case "unit", "unit_name" -> unit.name();
            case "unit_display" -> unit.displayName();
            case "unit_symbol", "symbol" -> unit.symbol();
            case "display_enabled", "display", "toggle" ->
                    online != null && plugin.service().preferences().displayEnabled(online) ? "true" : "false";

            // ---- where the player is ----------------------------------------------------------
            case "biome" -> online == null ? "" : BiomeKeys.keyOf(online.getLocation().getBlock().getBiome());
            case "world" -> online == null ? "" : online.getWorld().getName();
            case "simulated", "is_simulated" ->
                    online != null && plugin.worlds().isSimulated(online.getWorld()) ? "true" : "false";

            // ---- the calendar -----------------------------------------------------------------
            case "season" -> online == null ? "" : plugin.seasons().season(online.getWorld()).name();
            case "season_display" -> online == null ? "" : Messages.colour(
                    plugin.service().display().label(plugin.seasons().season(online.getWorld())));

            // ---- configured thresholds --------------------------------------------------------
            case "heat_threshold", "heat_limit" ->
                    numeric(plugin.configuration().simulation().heatEffectThresholdCelsius());
            case "cold_threshold", "cold_limit" ->
                    numeric(plugin.configuration().simulation().coldEffectThresholdCelsius());

            // ---- the reading, in the player's own unit ----------------------------------------
            case "temperature", "temp" -> of(reading, r -> unit.fromCelsius(r.bodyCelsius()));
            case "temperature_int", "temp_int" -> ofInt(reading, r -> unit.fromCelsius(r.bodyCelsius()));

            // ---- the reading, in each named scale ---------------------------------------------
            case "temperature_celsius", "celsius", "body", "body_celsius" ->
                    of(reading, TemperatureReading::bodyCelsius);
            case "temperature_fahrenheit", "fahrenheit" ->
                    of(reading, r -> TemperatureUnit.FAHRENHEIT.fromCelsius(r.bodyCelsius()));
            case "temperature_kelvin", "kelvin" ->
                    of(reading, r -> TemperatureUnit.KELVIN.fromCelsius(r.bodyCelsius()));
            case "temperature_rankine", "rankine" ->
                    of(reading, r -> TemperatureUnit.RANKINE.fromCelsius(r.bodyCelsius()));
            case "temperature_reaumur", "reaumur" ->
                    of(reading, r -> TemperatureUnit.REAUMUR.fromCelsius(r.bodyCelsius()));
            case "temperature_newton", "newton" ->
                    of(reading, r -> TemperatureUnit.NEWTON.fromCelsius(r.bodyCelsius()));
            case "temperature_delisle", "delisle" ->
                    of(reading, r -> TemperatureUnit.DELISLE.fromCelsius(r.bodyCelsius()));
            case "temperature_romer", "romer" ->
                    of(reading, r -> TemperatureUnit.ROMER.fromCelsius(r.bodyCelsius()));

            // ---- the environment behind the reading -------------------------------------------
            case "air", "air_celsius", "air_temperature" -> of(reading, r -> r.ambient().airCelsius());
            case "air_fahrenheit" ->
                    of(reading, r -> TemperatureUnit.FAHRENHEIT.fromCelsius(r.ambient().airCelsius()));
            case "apparent", "apparent_celsius", "feels_like" ->
                    of(reading, r -> r.ambient().apparentCelsius());
            case "apparent_fahrenheit" ->
                    of(reading, r -> TemperatureUnit.FAHRENHEIT.fromCelsius(r.ambient().apparentCelsius()));
            case "humidity" -> of(reading, r -> r.ambient().humidityPercent());
            case "humidity_int" -> ofInt(reading, r -> r.ambient().humidityPercent());
            case "wind", "wind_speed" -> of(reading, r -> r.ambient().windSpeedKmh());
            case "wind_int" -> ofInt(reading, r -> r.ambient().windSpeedKmh());

            // ---- derived state ----------------------------------------------------------------
            case "state", "condition" -> reading.map(r -> state(r.bodyCelsius())).orElse("");
            case "frozen" -> reading.map(this::isFrozen).orElse("");
            case "burning", "on_fire" -> reading.map(this::isBurning).orElse("");

            // ---- the rendered interface -------------------------------------------------------
            case "bar", "progress_bar" -> reading
                    .map(r -> Messages.colour(plugin.renderer().bar(r.bodyCelsius(), plugin.service().display())))
                    .orElse("");
            case "action_bar", "actionbar" -> reading
                    .map(r -> plugin.renderer().render(
                            unit, r.bodyCelsius(), r.ambient().season(), plugin.service().display()))
                    .orElse("");

            default -> null;
        };
    }

    /** @return the reading's value under {@code reader}, or empty when there is no reading yet. */
    private static String of(Optional<TemperatureReading> reading, ToDoubleFunction<TemperatureReading> reader) {
        return reading.map(r -> numeric(reader.applyAsDouble(r))).orElse("");
    }

    /** @return the reading's value rounded to a whole number, or empty when there is no reading yet. */
    private static String ofInt(Optional<TemperatureReading> reading, ToDoubleFunction<TemperatureReading> reader) {
        return reading.map(r -> String.valueOf(Math.round(reader.applyAsDouble(r)))).orElse("");
    }

    /**
     * Classifies a reading into a single word, using the configured thresholds so the word always
     * agrees with when the plugin actually sets a player on fire or starts freezing them.
     *
     * @param bodyCelsius the reading, in degrees Celsius
     * @return one of {@code freezing}, {@code cold}, {@code comfortable}, {@code hot}, {@code scorching}
     */
    private String state(double bodyCelsius) {
        double heatEffect = plugin.configuration().simulation().heatEffectThresholdCelsius();
        double coldEffect = plugin.configuration().simulation().coldEffectThresholdCelsius();
        double hot = plugin.service().display().hotThreshold();
        double cold = plugin.service().display().coldThreshold();

        if (bodyCelsius <= coldEffect) {
            return "freezing";
        }
        if (bodyCelsius <= cold) {
            return "cold";
        }
        if (bodyCelsius >= heatEffect) {
            return "scorching";
        }
        return bodyCelsius >= hot ? "hot" : "comfortable";
    }

    private String isFrozen(TemperatureReading reading) {
        return String.valueOf(reading.bodyCelsius()
                <= plugin.configuration().simulation().coldEffectThresholdCelsius());
    }

    private String isBurning(TemperatureReading reading) {
        return String.valueOf(reading.bodyCelsius()
                >= plugin.configuration().simulation().heatEffectThresholdCelsius());
    }

    /** @return the value to one decimal place, in the ROOT locale so a server's locale cannot alter it. */
    private static String numeric(double value) {
        return String.format(Locale.ROOT, "%.1f", value);
    }
}
