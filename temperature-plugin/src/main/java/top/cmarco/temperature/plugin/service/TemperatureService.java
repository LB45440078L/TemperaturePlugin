package top.cmarco.temperature.plugin.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import top.cmarco.temperature.core.environment.ThermalEnvironment;
import top.cmarco.temperature.core.service.TemperatureEngine;
import top.cmarco.temperature.core.service.TemperatureReading;
import top.cmarco.temperature.core.unit.TemperatureUnit;
import top.cmarco.temperature.plugin.api.PlayerTemperatureUpdateEvent;
import top.cmarco.temperature.plugin.config.DisplaySettings;
import top.cmarco.temperature.plugin.preference.PlayerPreferences;
import top.cmarco.temperature.plugin.sampler.EnvironmentSampler;

/**
 * The adapter's front door: one call advances a player's temperature, informs listeners, applies the
 * threshold consequences and returns the reading.
 *
 * <p>Ordering here is deliberate. The step is computed, then the public event is fired so other
 * plugins can inspect or veto it, then the value the event settled on is written back, then the
 * threshold effects run. That means an external plugin can lower a reader's temperature and have the
 * fire effect follow, and a vetoed reading cannot accidentally trigger burning.
 *
 * <p>The most recent reading per player is also cached, so the status command, the debug command and
 * the PlaceholderAPI expansion all describe exactly the values the last tick used rather than
 * re-deriving them and risking a disagreement.
 */
public final class TemperatureService {

    private final Plugin plugin;
    private final TemperatureEngine engine;
    private final EnvironmentSampler sampler;
    private final PlayerPreferences preferences;
    private final ThermalEffectApplier effects;
    private final Map<UUID, TemperatureReading> readings = new HashMap<>();
    private volatile DisplaySettings display;

    /**
     * @param plugin       the owning plugin, used to fire events
     * @param engine       the stateful core engine
     * @param sampler      the environment sampler
     * @param preferences  the per-player preference store
     * @param effects      the threshold-effect applier
     * @param display      the initial display tunables
     */
    public TemperatureService(@NotNull Plugin plugin, @NotNull TemperatureEngine engine,
                              @NotNull EnvironmentSampler sampler, @NotNull PlayerPreferences preferences,
                              @NotNull ThermalEffectApplier effects, @NotNull DisplaySettings display) {
        this.plugin = plugin;
        this.engine = engine;
        this.sampler = sampler;
        this.preferences = preferences;
        this.effects = effects;
        this.display = display;
    }

    /**
     * Advances one player by one step.
     *
     * @param player     the player
     * @param nowMillis  the current instant
     * @return the reading after listeners
     */
    @NotNull
    public TemperatureReading tick(@NotNull Player player, long nowMillis) {
        UUID id = player.getUniqueId();
        double previous = engine.bodyTemperature(id).orElse(Double.NaN);

        ThermalEnvironment environment = sampler.sample(player, nowMillis);
        TemperatureReading reading = engine.update(id, environment, nowMillis);
        TemperatureUnit unit = preferences.unit(player, display.defaultUnit());

        PlayerTemperatureUpdateEvent event = new PlayerTemperatureUpdateEvent(
                player, unit, reading.bodyCelsius(), reading.ambient());
        plugin.getServer().getPluginManager().callEvent(event);

        double settled = event.isCancelled()
                ? (Double.isNaN(previous) ? reading.bodyCelsius() : previous)
                : event.bodyCelsius();
        engine.setBody(id, settled, nowMillis);

        TemperatureReading finalReading = new TemperatureReading(id, settled, reading.ambient(), nowMillis);
        readings.put(id, finalReading);
        effects.apply(player, finalReading, nowMillis);
        return finalReading;
    }

    /**
     * @param player the player
     * @return the player's last reading, or empty before their first tick
     */
    @NotNull
    public Optional<TemperatureReading> lastReading(@NotNull Player player) {
        return Optional.ofNullable(readings.get(player.getUniqueId()));
    }

    /**
     * @param player the player
     * @return the player's last body temperature, or empty before their first tick
     */
    @NotNull
    public OptionalDouble bodyTemperature(@NotNull Player player) {
        return engine.bodyTemperature(player.getUniqueId());
    }

    /**
     * @param player the player
     * @return the unit the player is displaying in
     */
    @NotNull
    public TemperatureUnit unitOf(@NotNull Player player) {
        return preferences.unit(player, display.defaultUnit());
    }

    /**
     * Recomputes the environment and reports each modifier's contribution, for the debug command.
     *
     * @param player the player
     * @return contributions keyed by modifier name
     */
    @NotNull
    public Map<String, Double> diagnose(@NotNull Player player) {
        long now = System.currentTimeMillis();
        ThermalEnvironment environment = sampler.sample(player, now);
        return engine.model().breakdown(environment, now);
    }

    /**
     * Forgets a player's cached reading.
     *
     * @param playerId the player
     */
    public void forget(@NotNull UUID playerId) {
        readings.remove(playerId);
    }

    /**
     * Swaps in new display tunables after a reload.
     *
     * @param display the new display tunables
     */
    public void setDisplay(@NotNull DisplaySettings display) {
        this.display = display;
    }

    /** @return the display tunables currently in force. */
    @NotNull
    public DisplaySettings display() {
        return display;
    }

    /** @return the engine, for diagnostics and status. */
    @NotNull
    public TemperatureEngine engine() {
        return engine;
    }

    /** @return the preference store. */
    @NotNull
    public PlayerPreferences preferences() {
        return preferences;
    }

    /** @return the effect applier. */
    @NotNull
    public ThermalEffectApplier effects() {
        return effects;
    }
}
