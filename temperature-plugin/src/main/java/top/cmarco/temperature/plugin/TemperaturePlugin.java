package top.cmarco.temperature.plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import top.cmarco.temperature.core.climate.BiomeClimateRegistry;
import top.cmarco.temperature.core.config.TemperatureSettings;
import top.cmarco.temperature.core.physics.TemperatureModel;
import top.cmarco.temperature.core.service.TemperatureEngine;
import top.cmarco.temperature.plugin.command.TemperatureCommand;
import top.cmarco.temperature.plugin.config.ConfigurationLoader;
import top.cmarco.temperature.plugin.config.LoadedConfiguration;
import top.cmarco.temperature.plugin.config.Messages;
import top.cmarco.temperature.plugin.effect.EffectRegistry;
import top.cmarco.temperature.plugin.integration.TemperaturePlaceholderExpansion;
import top.cmarco.temperature.plugin.listener.ConsumptionListener;
import top.cmarco.temperature.plugin.listener.PlayerSessionListener;
import top.cmarco.temperature.plugin.preference.PlayerPreferences;
import top.cmarco.temperature.plugin.sampler.EmitterScanner;
import top.cmarco.temperature.plugin.sampler.EnvironmentSampler;
import top.cmarco.temperature.plugin.sampler.MaterialMapping;
import top.cmarco.temperature.plugin.service.SeasonTracker;
import top.cmarco.temperature.plugin.service.TemperatureService;
import top.cmarco.temperature.plugin.service.ThermalEffectApplier;
import top.cmarco.temperature.plugin.service.WorldRegistry;
import top.cmarco.temperature.plugin.task.ActionBarTask;
import top.cmarco.temperature.plugin.task.SeasonWatchTask;
import top.cmarco.temperature.plugin.task.TemperatureTickTask;
import top.cmarco.temperature.plugin.ui.ActionBarRenderer;

/**
 * The plugin container: it owns the object graph, the scheduler and the lifecycle.
 *
 * <p>Construction is centralised in {@link #rebuild(Map)} rather than spread across
 * {@code onEnable}, because a reload has to do the same work while preserving player state. The
 * sequence is always the same — read configuration, build the model, build the services, restart the
 * tasks — so there is no path where half of the plugin is on the old configuration.
 *
 * <p>The one piece of durable state, each player's body temperature, is carried across a reload by
 * snapshotting the engine and restoring it into the replacement, so a reload does not send every
 * player on the server back to a neutral reading.
 */
public final class TemperaturePlugin extends JavaPlugin {

    private Messages messages;
    private ConfigurationLoader loader;
    private PlayerPreferences preferences;
    private final EffectRegistry effectRegistry = new EffectRegistry();
    private final ActionBarRenderer renderer = new ActionBarRenderer();
    private final List<BukkitTask> tasks = new ArrayList<>();

    private volatile LoadedConfiguration configuration;
    private TemperatureService service;
    private SeasonTracker seasons;
    private WorldRegistry worlds;
    private TemperatureCommand command;

    @Override
    public void onEnable() {
        this.messages = new Messages(this);
        this.loader = new ConfigurationLoader(this);
        this.preferences = new PlayerPreferences(this);

        rebuild(Map.of());
        registerListeners();
        registerCommand();
        registerPlaceholders();

        getLogger().info("TemperaturePlugin enabled with " + worlds.worlds().size()
                + " simulated world(s).");
    }

    @Override
    public void onDisable() {
        stopTasks();
    }

    /**
     * Reloads the configuration and rebuilds the object graph, preserving player temperatures.
     *
     * <p>Safe to call from the command; the message service reloads itself in place first, so
     * anything it renders afterwards uses the file on disk.
     */
    public void reload() {
        messages.reload();
        Map<UUID, Double> carried = service == null ? Map.of() : service.engine().snapshotBodies();
        rebuild(carried);
        getLogger().info("Configuration reloaded.");
    }

    private void rebuild(@NotNull Map<UUID, Double> carriedBodies) {
        stopTasks();

        LoadedConfiguration loaded = loader.load();
        this.configuration = loaded;
        TemperatureSettings settings = loaded.simulation();

        BiomeClimateRegistry registry = new BiomeClimateRegistry();
        settings.biomeOverrides().forEach(registry::register);

        TemperatureModel model = new TemperatureModel(settings, registry);
        TemperatureEngine engine = new TemperatureEngine(model);
        engine.restoreBodies(carriedBodies, System.currentTimeMillis());

        EmitterScanner scanner = new EmitterScanner(new MaterialMapping(),
                settings.emitterScanRadiusHorizontal(), settings.emitterScanRadiusVertical());
        EnvironmentSampler sampler = new EnvironmentSampler(model, scanner, effectRegistry);
        ThermalEffectApplier effects = new ThermalEffectApplier(settings);

        this.service = new TemperatureService(this, engine, sampler, preferences, effects, loaded.display());
        this.seasons = new SeasonTracker(model, this);
        this.worlds = new WorldRegistry(getServer(), loaded.worlds(), getLogger());

        if (worlds.isEmpty()) {
            getLogger().warning("No simulated worlds are loaded; add world names to config.yml.");
        }
        startTasks(loaded);
    }

    private void startTasks(@NotNull LoadedConfiguration loaded) {
        tasks.add(new TemperatureTickTask(service, worlds)
                .runTaskTimer(this, 1L, loaded.updateIntervalTicks()));
        tasks.add(new ActionBarTask(service, worlds, seasons, renderer)
                .runTaskTimer(this, 2L, loaded.actionBarIntervalTicks()));
        tasks.add(new SeasonWatchTask(seasons, worlds)
                .runTaskTimer(this, 20L, loaded.seasonCheckIntervalTicks()));
    }

    private void stopTasks() {
        for (BukkitTask task : tasks) {
            task.cancel();
        }
        tasks.clear();
    }

    private void registerListeners() {
        PluginManager manager = getServer().getPluginManager();
        manager.registerEvents(new ConsumptionListener(this, effectRegistry), this);
        manager.registerEvents(new PlayerSessionListener(this), this);
    }

    private void registerCommand() {
        PluginCommand pluginCommand = getCommand("temperature");
        if (pluginCommand == null) {
            getLogger().severe("plugin.yml is missing the 'temperature' command; it is unusable.");
            return;
        }
        this.command = new TemperatureCommand(this);
        pluginCommand.setExecutor(command);
        pluginCommand.setTabCompleter(command);
    }

    private void registerPlaceholders() {
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") == null) {
            return;
        }
        if (new TemperaturePlaceholderExpansion(this).register()) {
            getLogger().info("Registered the PlaceholderAPI expansion.");
        }
    }

    /** @return the message service. */
    @NotNull
    public Messages messages() {
        return messages;
    }

    /** @return the temperature service. */
    @NotNull
    public TemperatureService service() {
        return service;
    }

    /** @return the season tracker. */
    @NotNull
    public SeasonTracker seasons() {
        return seasons;
    }

    /** @return the effect registry. */
    @NotNull
    public EffectRegistry effectRegistry() {
        return effectRegistry;
    }

    /** @return the simulations' world registry. */
    @NotNull
    public WorldRegistry worlds() {
        return worlds;
    }

    /** @return the configuration currently in force. */
    @NotNull
    public LoadedConfiguration configuration() {
        return configuration;
    }

    /** @return the action-bar renderer. */
    @NotNull
    public ActionBarRenderer renderer() {
        return renderer;
    }

    /** @return the configuration loader. */
    @NotNull
    public ConfigurationLoader loader() {
        return loader;
    }
}
