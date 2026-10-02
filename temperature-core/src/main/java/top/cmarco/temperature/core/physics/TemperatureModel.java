package top.cmarco.temperature.core.physics;

import java.util.List;
import java.util.Map;
import top.cmarco.temperature.core.climate.AltitudeModel;
import top.cmarco.temperature.core.climate.BiomeClimate;
import top.cmarco.temperature.core.climate.BiomeClimateRegistry;
import top.cmarco.temperature.core.climate.HumidityModel;
import top.cmarco.temperature.core.climate.Season;
import top.cmarco.temperature.core.climate.SeasonCycle;
import top.cmarco.temperature.core.config.TemperatureSettings;
import top.cmarco.temperature.core.environment.ThermalEnvironment;
import top.cmarco.temperature.core.physics.modifier.ActiveEffectModifier;
import top.cmarco.temperature.core.physics.modifier.ArmorModifier;
import top.cmarco.temperature.core.physics.modifier.BiomeAltitudeModifier;
import top.cmarco.temperature.core.physics.modifier.EmitterFieldModifier;
import top.cmarco.temperature.core.physics.modifier.ImmersionModifier;
import top.cmarco.temperature.core.physics.modifier.RainCoolingModifier;
import top.cmarco.temperature.core.physics.modifier.SeasonalModifier;
import top.cmarco.temperature.core.physics.modifier.SunlightModifier;

/**
 * The facade over the whole thermodynamic model.
 *
 * <p>Wires the pieces together once — the vertical profile, the humidity curve, the emitter field,
 * the collection of modifiers, the apparent-temperature regressions and the body integrator — and
 * exposes two operations: {@link #ambient} to read a point in the world, and {@link #relax} to advance
 * a body towards it. It is stateless and therefore safe to share; the per-player state lives in the
 * engine.
 *
 * <p>This is the only type the adapter needs to hold to get a temperature.
 */
public final class TemperatureModel {

    private final TemperatureSettings settings;
    private final BiomeClimateRegistry biomeRegistry;
    private final SeasonCycle seasonCycle;
    private final AltitudeModel altitudeModel;
    private final HumidityModel humidityModel;
    private final WindModel windModel;
    private final ApparentTemperature apparentTemperature;
    private final Thermoregulator thermoregulator;
    private final ConsumptionModel consumptionModel;
    private final TemperaturePipeline pipeline;

    /**
     * @param settings     the tunables; must be fully validated
     * @param biomeRegistry the biome-to-family registry, including operator overrides
     */
    public TemperatureModel(TemperatureSettings settings, BiomeClimateRegistry biomeRegistry) {
        this.settings = settings;
        this.biomeRegistry = biomeRegistry;
        this.seasonCycle = new SeasonCycle(settings.ticksPerDay(), settings.daysPerSeason());
        this.altitudeModel = AltitudeModel.from(settings);
        this.humidityModel = HumidityModel.from(settings);
        this.windModel = new WindModel(settings);
        this.apparentTemperature = new ApparentTemperature(settings);
        this.thermoregulator = new Thermoregulator(settings);
        this.consumptionModel = new ConsumptionModel(settings);
        this.pipeline = new TemperaturePipeline(List.of(
                new BiomeAltitudeModifier(altitudeModel),
                new SunlightModifier(settings),
                new SeasonalModifier(),
                new EmitterFieldModifier(new EmitterField(settings)),
                new ArmorModifier(settings),
                new ActiveEffectModifier(),
                new ImmersionModifier(settings),
                new RainCoolingModifier(settings)));
    }

    /**
     * @param settings the tunables, with a default biome registry
     */
    public TemperatureModel(TemperatureSettings settings) {
        this(settings, new BiomeClimateRegistry());
    }

    /**
     * Resolves a biome id into the climate this model will use for it.
     *
     * @param biomeKey the biome id, with or without its namespace
     * @return the resolved climate
     */
    public BiomeClimate resolveClimate(String biomeKey) {
        return biomeRegistry.resolve(biomeKey, settings::climateRange);
    }

    /**
     * Reads the ambient state at a point.
     *
     * @param environment    the measured surroundings
     * @param nowEpochMillis the instant of the reading, for timed effects
     * @return the ambient state
     */
    public AmbientState ambient(ThermalEnvironment environment, long nowEpochMillis) {
        double dayFraction = seasonCycle.dayFraction(environment.gameTime());
        double humidity = humidityModel.resolve(
                environment.climate(),
                dayFraction,
                environment.precipitating(),
                environment.humidityJitter());
        Season season = seasonCycle.seasonAt(environment.gameTime());

        ThermalContext context = new ThermalContext(
                environment, settings, seasonCycle, humidity, season, nowEpochMillis);
        double air = pipeline.airTemperatureCelsius(context);
        double wind = windModel.windSpeedKmh(environment);
        double apparent = apparentTemperature.estimate(air, humidity, wind);
        apparent += diurnalRipple(environment, dayFraction);

        return new AmbientState(air, apparent, humidity, wind, season);
    }

    /**
     * Provides the additive contribution of each modifier, for the debug command.
     *
     * @param environment    the measured surroundings
     * @param nowEpochMillis the instant of the reading
     * @return contributions keyed by modifier name, in pipeline order
     */
    public Map<String, Double> breakdown(ThermalEnvironment environment, long nowEpochMillis) {
        double dayFraction = seasonCycle.dayFraction(environment.gameTime());
        double humidity = humidityModel.resolve(
                environment.climate(), dayFraction, environment.precipitating(),
                environment.humidityJitter());
        Season season = seasonCycle.seasonAt(environment.gameTime());
        ThermalContext context = new ThermalContext(
                environment, settings, seasonCycle, humidity, season, nowEpochMillis);
        return pipeline.breakdown(context);
    }

    /**
     * Advances a body temperature towards an ambient apparent temperature.
     *
     * @param currentCelsius the body temperature now
     * @param targetCelsius  the ambient apparent temperature to approach
     * @param deltaSeconds   the elapsed time; non-positive returns the input
     * @return the advanced body temperature
     */
    public double relax(double currentCelsius, double targetCelsius, double deltaSeconds) {
        return thermoregulator.relax(currentCelsius, targetCelsius, deltaSeconds);
    }

    /**
     * A small diurnal oscillation riding on the apparent temperature.
     *
     * <p>Proportional to the biome's temperature span and to the same diurnal curve that drives
     * humidity, so warm biomes swing a little more than cool ones. Reproduces the original's
     * {@code span * scale / 2pi} term, with the {@code 1/2pi} exposed as a tunable.
     */
    private double diurnalRipple(ThermalEnvironment environment, double dayFraction) {
        if (!settings.diurnalRippleEnabled()) {
            return 0.0;
        }
        double span = environment.climate().range().temperatureSpan();
        return settings.diurnalRippleScale() * span * humidityModel.diurnalScale(dayFraction);
    }

    /** @return the settings in force. */
    public TemperatureSettings settings() {
        return settings;
    }

    /** @return the calendar in force, for callers that need the raw season arithmetic. */
    public SeasonCycle seasonCycle() {
        return seasonCycle;
    }

    /** @return the consumption curve, so the adapter can schedule a water effect's lifetime. */
    public ConsumptionModel consumptionModel() {
        return consumptionModel;
    }

    /** @return the relaxation time constant, in seconds, for callers annotating their logs. */
    public double thermoregulationTimeConstantSeconds() {
        return thermoregulator.timeConstantSeconds();
    }
}
