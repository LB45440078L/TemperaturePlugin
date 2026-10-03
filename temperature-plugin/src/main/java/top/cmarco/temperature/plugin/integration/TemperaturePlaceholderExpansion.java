package top.cmarco.temperature.plugin.integration;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.cmarco.temperature.core.service.TemperatureReading;
import top.cmarco.temperature.core.unit.TemperatureUnit;
import top.cmarco.temperature.plugin.TemperaturePlugin;

/**
 * Exposes readings to PlaceholderAPI.
 *
 * <p>Registered only when PlaceholderAPI is present, so the plugin still starts on a server without
 * it. Placeholders are answered from the last simulation step, which keeps the values a scoreboard
 * shows identical to the values the physics produced.
 *
 * <p>Supported identifiers: {@code temperature} (or {@code temp}), {@code celsius},
 * {@code fahrenheit}, {@code kelvin}, {@code unit}, {@code humidity}, {@code season}, {@code air},
 * {@code apparent} and {@code wind}.
 */
public final class TemperaturePlaceholderExpansion extends PlaceholderExpansion {

    private final TemperaturePlugin plugin;

    /**
     * @param plugin the owning plugin
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
        return "CMarco";
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
    public @Nullable String onPlaceholderRequest(@Nullable Player player, @NotNull String identifier) {
        if (player == null) {
            return "";
        }
        var optional = plugin.service().lastReading(player);
        if (optional.isEmpty()) {
            return "";
        }
        TemperatureReading reading = optional.get();
        TemperatureUnit unit = plugin.service().unitOf(player);

        return switch (identifier.toLowerCase(java.util.Locale.ROOT)) {
            case "temperature", "temp" -> format(unit.fromCelsius(reading.bodyCelsius()));
            case "celsius" -> format(reading.bodyCelsius());
            case "fahrenheit" -> format(TemperatureUnit.FAHRENHEIT.fromCelsius(reading.bodyCelsius()));
            case "kelvin" -> format(TemperatureUnit.KELVIN.fromCelsius(reading.bodyCelsius()));
            case "unit" -> unit.name();
            case "symbol" -> unit.symbol();
            case "humidity" -> format(reading.ambient().humidityPercent());
            case "season" -> reading.ambient().season().name();
            case "air" -> format(reading.ambient().airCelsius());
            case "apparent" -> format(reading.ambient().apparentCelsius());
            case "wind" -> format(reading.ambient().windSpeedKmh());
            default -> null;
        };
    }

    private static String format(double value) {
        return String.format(java.util.Locale.ROOT, "%.1f", value);
    }
}
