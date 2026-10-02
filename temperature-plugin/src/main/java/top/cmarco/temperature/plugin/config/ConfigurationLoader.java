package top.cmarco.temperature.plugin.config;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import top.cmarco.temperature.core.climate.AltitudeModel;
import top.cmarco.temperature.core.climate.ClimateArchetype;
import top.cmarco.temperature.core.climate.ClimateRange;
import top.cmarco.temperature.core.climate.Season;
import top.cmarco.temperature.core.config.TemperatureSettings;
import top.cmarco.temperature.core.unit.TemperatureUnit;

/**
 * Parses {@code config.yml} into a {@link LoadedConfiguration}.
 *
 * <p>All YAML knowledge lives here. The core is handed an already-validated
 * {@link TemperatureSettings}, and every path is read through a helper that falls back to the
 * core's own default when a key is missing — so a config file from an older version still loads, and
 * a typo degrades to the default with a warning rather than an exception.
 *
 * <p>The {@code config-version} key is checked, not merely shipped: a file newer than this build
 * understands is reported loudly, because silently ignoring settings an operator believes are in
 * force is worse than refusing to start.
 */
public final class ConfigurationLoader {

    /** The configuration schema this build understands. */
    public static final int SUPPORTED_CONFIG_VERSION = 2;

    private final Plugin plugin;
    private final Logger logger;

    /**
     * @param plugin the owning plugin
     */
    public ConfigurationLoader(@NotNull Plugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    /**
     * Exports the bundled default if needed, then parses the active file.
     *
     * @return the loaded configuration
     */
    @NotNull
    public LoadedConfiguration load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();
        checkVersion(config);
        return new LoadedConfiguration(
                simulation(config),
                display(config),
                config.getStringList("worlds"),
                PerformanceProfile.parse(config.getString("simulation.performance"))
                        .orElse(PerformanceProfile.HIGH).updateIntervalTicks(),
                positive(config, "simulation.season-check-interval-ticks", 1200L),
                positive(config, "simulation.action-bar-interval-ticks", 10L));
    }

    private void checkVersion(FileConfiguration config) {
        int version = config.getInt("config-version", 0);
        if (version == 0) {
            logger.warning("config.yml has no config-version; assuming it is current. "
                    + "Delete it and restart to regenerate the annotated default.");
        } else if (version > SUPPORTED_CONFIG_VERSION) {
            logger.warning("config.yml is version " + version + " but this build understands "
                    + SUPPORTED_CONFIG_VERSION + "; newer settings may be ignored.");
        }
    }

    private TemperatureSettings simulation(FileConfiguration config) {
        TemperatureSettings.Builder builder = TemperatureSettings.builder();

        builder.calendar(
                positive(config, "calendar.ticks-per-day", 24_000L),
                positive(config, "calendar.days-per-season", 31L));
        for (Season season : Season.values()) {
            builder.seasonOffset(season,
                    config.getDouble("calendar.season-offsets." + season.name(), 0.0));
        }

        AltitudeModel.Mode mode = parseEnum(AltitudeModel.Mode.class,
                config.getString("altitude.mode"), AltitudeModel.Mode.LINEAR_LAPSE, "altitude.mode");
        builder.altitude(mode, config.getDouble("altitude.lapse-fraction", 0.85));
        builder.legacyLapseQuadratic(
                config.getDouble("altitude.legacy-quadratic.a", 3.981796231733e-6),
                config.getDouble("altitude.legacy-quadratic.b", -0.00322799),
                config.getDouble("altitude.legacy-quadratic.c", 0.695913));

        builder.humidityCurve(
                config.getDouble("humidity.peak-time-fraction", 6500.0 / 24000.0),
                config.getDouble("humidity.standard-deviation", 4700.0 / 24000.0),
                config.getDouble("humidity.amplitude", 0.9844),
                config.getDouble("humidity.floor", 0.015));
        builder.precipitationHumidityBonus(config.getDouble("humidity.precipitation-bonus", 15.0));

        for (ClimateArchetype archetype : ClimateArchetype.values()) {
            String path = "climate." + archetype.key();
            if (config.isConfigurationSection(path)) {
                builder.climateRange(archetype, new ClimateRange(
                        config.getDouble(path + ".temperature.min", 0.0),
                        config.getDouble(path + ".temperature.max", 30.0),
                        config.getDouble(path + ".humidity.min", 20.0),
                        config.getDouble(path + ".humidity.max", 80.0)));
            }
        }
        if (config.isConfigurationSection("biome-overrides")) {
            for (String biomeKey : config.getConfigurationSection("biome-overrides").getKeys(false)) {
                String raw = config.getString("biome-overrides." + biomeKey);
                parseEnumOptional(ClimateArchetype.class, raw).ifPresent(archetype ->
                        builder.biomeOverride(biomeKey, archetype));
            }
        }

        builder.emitterField(
                config.getDouble("emitters.influence-radius", 5.0),
                config.getInt("emitters.max-applied", 10));
        builder.emitterScan(
                config.getInt("emitters.scan.horizontal", 3),
                config.getInt("emitters.scan.vertical", 1));

        builder.solarGain(config.getDouble("solar-gain", 4.0));
        builder.leatherGain(config.getDouble("insulation.leather-per-piece", 2.5));
        builder.immersion(
                config.getBoolean("immersion.enabled", true),
                config.getDouble("immersion.cooling", 4.0));

        builder.rainCooling(
                config.getDouble("weather.rain-cooling-factor", 0.725),
                config.getDouble("weather.rain-cooling-threshold", 27.5));
        builder.wind(
                config.getDouble("weather.wind.base", 5.0),
                config.getDouble("weather.wind.storm-bonus", 25.0),
                config.getDouble("weather.wind.exposure-bonus", 10.0));

        builder.thermoregulation(
                config.getDouble("simulation.time-constant-seconds", 36.0),
                config.getDouble("simulation.initial-body-temperature", 36.0),
                config.getDouble("simulation.min-body-temperature", -40.0),
                config.getDouble("simulation.max-body-temperature", 60.0));

        builder.apparentTemperature(
                config.getBoolean("apparent-temperature.heat-index.enabled", true),
                config.getDouble("apparent-temperature.heat-index.min-temperature", 26.7),
                config.getDouble("apparent-temperature.heat-index.max-temperature", 46.0),
                config.getBoolean("apparent-temperature.wind-chill.enabled", true),
                config.getDouble("apparent-temperature.wind-chill.max-temperature", 10.0),
                config.getDouble("apparent-temperature.wind-chill.min-wind", 4.8));

        builder.diurnalRipple(
                config.getBoolean("diurnal-ripple.enabled", true),
                config.getDouble("diurnal-ripple.scale", 1.0 / (2.0 * Math.PI)));

        builder.waterCooling(
                config.getDouble("consumption.water.cooling-factor", 0.6),
                config.getDouble("consumption.water.cooling-offset", 4.5),
                config.getDouble("consumption.water.peak-cooling", 20.0),
                config.getInt("consumption.water.effect-duration-minutes", 15));

        builder.effectThresholds(
                config.getDouble("effects.heat-threshold", 40.5),
                config.getDouble("effects.cold-threshold", -0.5));

        return builder.build();
    }

    private DisplaySettings display(FileConfiguration config) {
        Map<Season, String> labels = new EnumMap<>(Season.class);
        for (Season season : Season.values()) {
            labels.put(season, config.getString("display.season-labels." + season.name(),
                    season.displayName()));
        }
        TemperatureUnit unit = TemperatureUnit.parse(config.getString("display.default-unit"))
                .orElse(TemperatureUnit.CELSIUS);
        return new DisplaySettings(
                config.getBoolean("display.enabled", true),
                unit,
                config.getString("display.action-bar", "&7[{BAR}&7] {TEMP} {SEASON}"),
                config.getString("display.number-format", "%.1f"),
                Math.max(1, config.getInt("display.bar.length", 10)),
                config.getString("display.bar.reached", "|"),
                config.getString("display.bar.unreached", "."),
                config.getString("display.colours.cold", "&b"),
                config.getString("display.colours.temperate", "&a"),
                config.getString("display.colours.hot", "&c"),
                config.getDouble("display.thresholds.cold", 5.0),
                config.getDouble("display.thresholds.hot", 29.5),
                labels);
    }

    private long positive(FileConfiguration config, String path, long fallback) {
        long value = config.getLong(path, fallback);
        if (value <= 0L) {
            logger.warning(path + " must be positive; using " + fallback);
            return fallback;
        }
        return value;
    }

    private <E extends Enum<E>> E parseEnum(Class<E> type, String raw, E fallback, String path) {
        return parseEnumOptional(type, raw).orElseGet(() -> {
            if (raw != null && !raw.isBlank()) {
                logger.warning(path + " has unrecognised value '" + raw + "'; using " + fallback);
            }
            return fallback;
        });
    }

    private <E extends Enum<E>> java.util.Optional<E> parseEnumOptional(Class<E> type, String raw) {
        if (raw == null || raw.isBlank()) {
            return java.util.Optional.empty();
        }
        try {
            return java.util.Optional.of(Enum.valueOf(type, raw.trim().toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException e) {
            return java.util.Optional.empty();
        }
    }
}
