package top.cmarco.temperature.core.config;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import top.cmarco.temperature.core.climate.AltitudeModel;
import top.cmarco.temperature.core.climate.ClimateArchetype;
import top.cmarco.temperature.core.climate.ClimateRange;
import top.cmarco.temperature.core.climate.Season;
import top.cmarco.temperature.core.environment.ThermalEmitter;

/**
 * Every tunable the simulation and the adapter read, in one immutable object.
 *
 * <p>The core never touches YAML. The Spigot adapter parses {@code config.yml} into a
 * {@link Builder}, the builder validates once, and the frozen result is handed to the models. That
 * separation is what lets a unit test construct an exact world — "cold biome, high altitude,
 * raining, three leather pieces" — with no file I/O, and it makes a reload a single atomic swap
 * rather than a scatter of mutated fields.
 *
 * <p>All temperatures are degrees Celsius unless a name says otherwise. Tick counts assume a
 * 24000-tick day unless overridden.
 */
public final class TemperatureSettings {

    private final Map<ClimateArchetype, ClimateRange> climateRanges;
    private final Map<String, ClimateArchetype> biomeOverrides;
    private final Map<Season, Double> seasonOffsets;

    private final long ticksPerDay;
    private final long daysPerSeason;

    private final AltitudeModel.Mode altitudeMode;
    private final double lapseFraction;
    private final double legacyLapseQuadraticA;
    private final double legacyLapseQuadraticB;
    private final double legacyLapseQuadraticC;

    private final double humidityPeakFraction;
    private final double humidityStandardDeviation;
    private final double humidityAmplitude;
    private final double humidityFloor;
    private final double precipitationHumidityBonus;

    private final double emitterInfluenceRadius;
    private final int maxEmittersApplied;
    private final int emitterScanRadiusHorizontal;
    private final int emitterScanRadiusVertical;

    private final double solarGainCelsius;
    private final double leatherGainPerPieceCelsius;
    private final double immersionCoolingCelsius;
    private final boolean immersionEnabled;

    private final double rainCoolingFactor;
    private final double rainCoolingThresholdCelsius;

    private final double windBaseKmh;
    private final double windStormBonusKmh;
    private final double windExposureBonusKmh;

    private final double thermoregulationTimeConstantSeconds;
    private final double initialBodyTemperatureCelsius;
    private final double minBodyTemperatureCelsius;
    private final double maxBodyTemperatureCelsius;

    private final boolean heatIndexEnabled;
    private final double heatIndexMinTemperatureCelsius;
    private final double heatIndexMaxTemperatureCelsius;
    private final boolean windChillEnabled;
    private final double windChillThresholdCelsius;
    private final double windChillMinWindKmh;

    private final boolean diurnalRippleEnabled;
    private final double diurnalRippleScale;

    private final double waterCoolingFactor;
    private final double waterCoolingOffset;
    private final double waterCoolingPeakCelsius;
    private final int waterEffectDurationMinutes;

    private final double heatEffectThresholdCelsius;
    private final double coldEffectThresholdCelsius;

    private TemperatureSettings(Builder b) {
        this.climateRanges = Map.copyOf(b.climateRanges);
        this.biomeOverrides = Map.copyOf(b.biomeOverrides);
        this.seasonOffsets = Map.copyOf(b.seasonOffsets);
        this.ticksPerDay = b.ticksPerDay;
        this.daysPerSeason = b.daysPerSeason;
        this.altitudeMode = b.altitudeMode;
        this.lapseFraction = b.lapseFraction;
        this.legacyLapseQuadraticA = b.legacyLapseQuadraticA;
        this.legacyLapseQuadraticB = b.legacyLapseQuadraticB;
        this.legacyLapseQuadraticC = b.legacyLapseQuadraticC;
        this.humidityPeakFraction = b.humidityPeakFraction;
        this.humidityStandardDeviation = b.humidityStandardDeviation;
        this.humidityAmplitude = b.humidityAmplitude;
        this.humidityFloor = b.humidityFloor;
        this.precipitationHumidityBonus = b.precipitationHumidityBonus;
        this.emitterInfluenceRadius = b.emitterInfluenceRadius;
        this.maxEmittersApplied = b.maxEmittersApplied;
        this.emitterScanRadiusHorizontal = b.emitterScanRadiusHorizontal;
        this.emitterScanRadiusVertical = b.emitterScanRadiusVertical;
        this.solarGainCelsius = b.solarGainCelsius;
        this.leatherGainPerPieceCelsius = b.leatherGainPerPieceCelsius;
        this.immersionCoolingCelsius = b.immersionCoolingCelsius;
        this.immersionEnabled = b.immersionEnabled;
        this.rainCoolingFactor = b.rainCoolingFactor;
        this.rainCoolingThresholdCelsius = b.rainCoolingThresholdCelsius;
        this.windBaseKmh = b.windBaseKmh;
        this.windStormBonusKmh = b.windStormBonusKmh;
        this.windExposureBonusKmh = b.windExposureBonusKmh;
        this.thermoregulationTimeConstantSeconds = b.thermoregulationTimeConstantSeconds;
        this.initialBodyTemperatureCelsius = b.initialBodyTemperatureCelsius;
        this.minBodyTemperatureCelsius = b.minBodyTemperatureCelsius;
        this.maxBodyTemperatureCelsius = b.maxBodyTemperatureCelsius;
        this.heatIndexEnabled = b.heatIndexEnabled;
        this.heatIndexMinTemperatureCelsius = b.heatIndexMinTemperatureCelsius;
        this.heatIndexMaxTemperatureCelsius = b.heatIndexMaxTemperatureCelsius;
        this.windChillEnabled = b.windChillEnabled;
        this.windChillThresholdCelsius = b.windChillThresholdCelsius;
        this.windChillMinWindKmh = b.windChillMinWindKmh;
        this.diurnalRippleEnabled = b.diurnalRippleEnabled;
        this.diurnalRippleScale = b.diurnalRippleScale;
        this.waterCoolingFactor = b.waterCoolingFactor;
        this.waterCoolingOffset = b.waterCoolingOffset;
        this.waterCoolingPeakCelsius = b.waterCoolingPeakCelsius;
        this.waterEffectDurationMinutes = b.waterEffectDurationMinutes;
        this.heatEffectThresholdCelsius = b.heatEffectThresholdCelsius;
        this.coldEffectThresholdCelsius = b.coldEffectThresholdCelsius;
    }

    /** @return a builder pre-loaded with the documented defaults. */
    public static Builder builder() {
        return new Builder();
    }

    /** @return a settings instance using only the defaults. */
    public static TemperatureSettings defaults() {
        return builder().build();
    }

    // --- climate -------------------------------------------------------------------------------

    /** @return the temperature and humidity band for a climate family. */
    public ClimateRange climateRange(ClimateArchetype archetype) {
        ClimateRange range = climateRanges.get(archetype);
        if (range == null) {
            throw new IllegalStateException("No climate range configured for " + archetype);
        }
        return range;
    }

    /** @return an operator-defined biome-to-family override, or {@code null} if none. */
    public ClimateArchetype biomeOverride(String biomeKey) {
        return biomeOverrides.get(biomeKey);
    }

    /** @return the biome overrides as an immutable map. */
    public Map<String, ClimateArchetype> biomeOverrides() {
        return biomeOverrides;
    }

    // --- calendar ------------------------------------------------------------------------------

    public long ticksPerDay() {
        return ticksPerDay;
    }

    public long daysPerSeason() {
        return daysPerSeason;
    }

    /** @return the seasonal temperature offset for a season, in degrees Celsius. */
    public double seasonOffsetCelsius(Season season) {
        return seasonOffsets.getOrDefault(season, 0.0);
    }

    // --- altitude ------------------------------------------------------------------------------

    public AltitudeModel.Mode altitudeMode() {
        return altitudeMode;
    }

    public double lapseFraction() {
        return lapseFraction;
    }

    public double legacyLapseQuadraticA() {
        return legacyLapseQuadraticA;
    }

    public double legacyLapseQuadraticB() {
        return legacyLapseQuadraticB;
    }

    public double legacyLapseQuadraticC() {
        return legacyLapseQuadraticC;
    }

    // --- humidity ------------------------------------------------------------------------------

    public double humidityPeakFraction() {
        return humidityPeakFraction;
    }

    public double humidityStandardDeviation() {
        return humidityStandardDeviation;
    }

    public double humidityAmplitude() {
        return humidityAmplitude;
    }

    public double humidityFloor() {
        return humidityFloor;
    }

    public double precipitationHumidityBonus() {
        return precipitationHumidityBonus;
    }

    // --- emitters ------------------------------------------------------------------------------

    /** @return the distance, in blocks, at which an emitter's contribution reaches zero. */
    public double emitterInfluenceRadius() {
        return emitterInfluenceRadius;
    }

    /** @return the most emitters ever summed in one reading, the strongest nearest first. */
    public int maxEmittersApplied() {
        return maxEmittersApplied;
    }

    public int emitterScanRadiusHorizontal() {
        return emitterScanRadiusHorizontal;
    }

    public int emitterScanRadiusVertical() {
        return emitterScanRadiusVertical;
    }

    // --- insulation, solar, immersion ----------------------------------------------------------

    public double solarGainCelsius() {
        return solarGainCelsius;
    }

    public double leatherGainPerPieceCelsius() {
        return leatherGainPerPieceCelsius;
    }

    public double immersionCoolingCelsius() {
        return immersionCoolingCelsius;
    }

    public boolean immersionEnabled() {
        return immersionEnabled;
    }

    // --- weather -------------------------------------------------------------------------------

    public double rainCoolingFactor() {
        return rainCoolingFactor;
    }

    public double rainCoolingThresholdCelsius() {
        return rainCoolingThresholdCelsius;
    }

    public double windBaseKmh() {
        return windBaseKmh;
    }

    public double windStormBonusKmh() {
        return windStormBonusKmh;
    }

    public double windExposureBonusKmh() {
        return windExposureBonusKmh;
    }

    // --- thermoregulation ----------------------------------------------------------------------

    public double thermoregulationTimeConstantSeconds() {
        return thermoregulationTimeConstantSeconds;
    }

    public double initialBodyTemperatureCelsius() {
        return initialBodyTemperatureCelsius;
    }

    public double minBodyTemperatureCelsius() {
        return minBodyTemperatureCelsius;
    }

    public double maxBodyTemperatureCelsius() {
        return maxBodyTemperatureCelsius;
    }

    // --- apparent temperature ------------------------------------------------------------------

    public boolean heatIndexEnabled() {
        return heatIndexEnabled;
    }

    public double heatIndexMinTemperatureCelsius() {
        return heatIndexMinTemperatureCelsius;
    }

    public double heatIndexMaxTemperatureCelsius() {
        return heatIndexMaxTemperatureCelsius;
    }

    public boolean windChillEnabled() {
        return windChillEnabled;
    }

    public double windChillThresholdCelsius() {
        return windChillThresholdCelsius;
    }

    public double windChillMinWindKmh() {
        return windChillMinWindKmh;
    }

    public boolean diurnalRippleEnabled() {
        return diurnalRippleEnabled;
    }

    public double diurnalRippleScale() {
        return diurnalRippleScale;
    }

    // --- consumption ---------------------------------------------------------------------------

    public double waterCoolingFactor() {
        return waterCoolingFactor;
    }

    public double waterCoolingOffset() {
        return waterCoolingOffset;
    }

    public double waterCoolingPeakCelsius() {
        return waterCoolingPeakCelsius;
    }

    public int waterEffectDurationMinutes() {
        return waterEffectDurationMinutes;
    }

    // --- effects -------------------------------------------------------------------------------

    public double heatEffectThresholdCelsius() {
        return heatEffectThresholdCelsius;
    }

    public double coldEffectThresholdCelsius() {
        return coldEffectThresholdCelsius;
    }

    /** Mutable builder for {@link TemperatureSettings}. */
    public static final class Builder {

        private final Map<ClimateArchetype, ClimateRange> climateRanges = new EnumMap<>(ClimateArchetype.class);
        private final Map<String, ClimateArchetype> biomeOverrides = new HashMap<>();
        private final Map<Season, Double> seasonOffsets = new EnumMap<>(Season.class);

        private long ticksPerDay = 24_000L;
        private long daysPerSeason = 31L;

        private AltitudeModel.Mode altitudeMode = AltitudeModel.Mode.LINEAR_LAPSE;
        private double lapseFraction = 0.85;
        private double legacyLapseQuadraticA = 3.981796231733e-6;
        private double legacyLapseQuadraticB = -0.00322799;
        private double legacyLapseQuadraticC = 0.695913;

        private double humidityPeakFraction = 6500.0 / 24000.0;
        private double humidityStandardDeviation = 4700.0 / 24000.0;
        private double humidityAmplitude = 0.9844;
        private double humidityFloor = 0.015;
        private double precipitationHumidityBonus = 15.0;

        private double emitterInfluenceRadius = 5.0;
        private int maxEmittersApplied = 10;
        private int emitterScanRadiusHorizontal = 3;
        private int emitterScanRadiusVertical = 1;

        private double solarGainCelsius = 4.0;
        private double leatherGainPerPieceCelsius = 2.5;
        private double immersionCoolingCelsius = 4.0;
        private boolean immersionEnabled = true;

        private double rainCoolingFactor = 0.725;
        private double rainCoolingThresholdCelsius = 27.5;

        private double windBaseKmh = 5.0;
        private double windStormBonusKmh = 25.0;
        private double windExposureBonusKmh = 10.0;

        private double thermoregulationTimeConstantSeconds = 36.0;
        private double initialBodyTemperatureCelsius = 36.0;
        private double minBodyTemperatureCelsius = -40.0;
        private double maxBodyTemperatureCelsius = 60.0;

        private boolean heatIndexEnabled = true;
        private double heatIndexMinTemperatureCelsius = 26.7;
        private double heatIndexMaxTemperatureCelsius = 46.0;
        private boolean windChillEnabled = true;
        private double windChillThresholdCelsius = 10.0;
        private double windChillMinWindKmh = 4.8;

        private boolean diurnalRippleEnabled = true;
        private double diurnalRippleScale = 1.0 / (2.0 * Math.PI);

        private double waterCoolingFactor = 0.6;
        private double waterCoolingOffset = 4.5;
        private double waterCoolingPeakCelsius = 20.0;
        private int waterEffectDurationMinutes = 15;

        private double heatEffectThresholdCelsius = 40.5;
        private double coldEffectThresholdCelsius = -0.5;

        private Builder() {
            climateRanges.put(ClimateArchetype.TEMPERATE, new ClimateRange(5.0, 28.0, 40.0, 65.0));
            climateRanges.put(ClimateArchetype.COLD, new ClimateRange(-35.0, 5.0, 30.0, 60.0));
            climateRanges.put(ClimateArchetype.WARM, new ClimateRange(20.0, 38.0, 15.0, 45.0));
            climateRanges.put(ClimateArchetype.OCEAN, new ClimateRange(2.0, 18.0, 60.0, 85.0));
            climateRanges.put(ClimateArchetype.COLD_OCEAN, new ClimateRange(-2.0, 8.0, 45.0, 70.0));
            climateRanges.put(ClimateArchetype.NETHER, new ClimateRange(35.0, 60.0, 2.0, 15.0));
            climateRanges.put(ClimateArchetype.END, new ClimateRange(-10.0, 15.0, 5.0, 20.0));

            seasonOffsets.put(Season.AUTUMN, -5.5);
            seasonOffsets.put(Season.WINTER, -12.5);
            seasonOffsets.put(Season.SPRING, 1.0);
            seasonOffsets.put(Season.SUMMER, 5.05);
        }

        public Builder climateRange(ClimateArchetype archetype, ClimateRange range) {
            climateRanges.put(Objects.requireNonNull(archetype), Objects.requireNonNull(range));
            return this;
        }

        public Builder biomeOverride(String biomeKey, ClimateArchetype archetype) {
            biomeOverrides.put(biomeKey, archetype);
            return this;
        }

        public Builder seasonOffset(Season season, double celsius) {
            seasonOffsets.put(season, celsius);
            return this;
        }

        public Builder calendar(long ticksPerDay, long daysPerSeason) {
            this.ticksPerDay = ticksPerDay;
            this.daysPerSeason = daysPerSeason;
            return this;
        }

        public Builder altitude(AltitudeModel.Mode mode, double lapseFraction) {
            this.altitudeMode = Objects.requireNonNull(mode);
            this.lapseFraction = lapseFraction;
            return this;
        }

        public Builder legacyLapseQuadratic(double a, double b, double c) {
            this.legacyLapseQuadraticA = a;
            this.legacyLapseQuadraticB = b;
            this.legacyLapseQuadraticC = c;
            return this;
        }

        public Builder humidityCurve(double peakFraction, double standardDeviation, double amplitude, double floor) {
            this.humidityPeakFraction = peakFraction;
            this.humidityStandardDeviation = standardDeviation;
            this.humidityAmplitude = amplitude;
            this.humidityFloor = floor;
            return this;
        }

        public Builder precipitationHumidityBonus(double bonus) {
            this.precipitationHumidityBonus = bonus;
            return this;
        }

        public Builder emitterField(double influenceRadius, int maxApplied) {
            this.emitterInfluenceRadius = influenceRadius;
            this.maxEmittersApplied = maxApplied;
            return this;
        }

        public Builder emitterScan(int horizontal, int vertical) {
            this.emitterScanRadiusHorizontal = horizontal;
            this.emitterScanRadiusVertical = vertical;
            return this;
        }

        public Builder solarGain(double celsius) {
            this.solarGainCelsius = celsius;
            return this;
        }

        public Builder leatherGain(double celsiusPerPiece) {
            this.leatherGainPerPieceCelsius = celsiusPerPiece;
            return this;
        }

        public Builder immersion(boolean enabled, double coolingCelsius) {
            this.immersionEnabled = enabled;
            this.immersionCoolingCelsius = coolingCelsius;
            return this;
        }

        public Builder rainCooling(double factor, double thresholdCelsius) {
            this.rainCoolingFactor = factor;
            this.rainCoolingThresholdCelsius = thresholdCelsius;
            return this;
        }

        public Builder wind(double baseKmh, double stormBonusKmh, double exposureBonusKmh) {
            this.windBaseKmh = baseKmh;
            this.windStormBonusKmh = stormBonusKmh;
            this.windExposureBonusKmh = exposureBonusKmh;
            return this;
        }

        public Builder thermoregulation(double timeConstantSeconds, double initialBodyCelsius,
                                        double minBodyCelsius, double maxBodyCelsius) {
            this.thermoregulationTimeConstantSeconds = timeConstantSeconds;
            this.initialBodyTemperatureCelsius = initialBodyCelsius;
            this.minBodyTemperatureCelsius = minBodyCelsius;
            this.maxBodyTemperatureCelsius = maxBodyCelsius;
            return this;
        }

        public Builder apparentTemperature(boolean heatIndexEnabled, double heatIndexMinCelsius,
                                           double heatIndexMaxCelsius, boolean windChillEnabled,
                                           double windChillThresholdCelsius, double windChillMinWindKmh) {
            this.heatIndexEnabled = heatIndexEnabled;
            this.heatIndexMinTemperatureCelsius = heatIndexMinCelsius;
            this.heatIndexMaxTemperatureCelsius = heatIndexMaxCelsius;
            this.windChillEnabled = windChillEnabled;
            this.windChillThresholdCelsius = windChillThresholdCelsius;
            this.windChillMinWindKmh = windChillMinWindKmh;
            return this;
        }

        public Builder diurnalRipple(boolean enabled, double scale) {
            this.diurnalRippleEnabled = enabled;
            this.diurnalRippleScale = scale;
            return this;
        }

        public Builder waterCooling(double factor, double offset, double peakCelsius, int durationMinutes) {
            this.waterCoolingFactor = factor;
            this.waterCoolingOffset = offset;
            this.waterCoolingPeakCelsius = peakCelsius;
            this.waterEffectDurationMinutes = durationMinutes;
            return this;
        }

        public Builder effectThresholds(double heatCelsius, double coldCelsius) {
            this.heatEffectThresholdCelsius = heatCelsius;
            this.coldEffectThresholdCelsius = coldCelsius;
            return this;
        }

        /**
         * Validates every invariant and freezes the settings.
         *
         * @return the immutable settings
         * @throws IllegalStateException if any value is out of range
         */
        public TemperatureSettings build() {
            validate();
            return new TemperatureSettings(this);
        }

        private void validate() {
            require(ticksPerDay > 0, "ticksPerDay must be positive");
            require(daysPerSeason > 0, "daysPerSeason must be positive");
            require(lapseFraction >= 0.0 && lapseFraction <= 1.0, "lapseFraction must lie within [0, 1]");
            require(humidityStandardDeviation > 0.0, "humidityStandardDeviation must be positive");
            require(humidityPeakFraction >= 0.0 && humidityPeakFraction < 1.0,
                    "humidityPeakFraction must lie within [0, 1)");
            require(emitterInfluenceRadius > 0.0, "emitterInfluenceRadius must be positive");
            require(maxEmittersApplied >= 0, "maxEmittersApplied must not be negative");
            require(emitterScanRadiusHorizontal >= 0 && emitterScanRadiusVertical >= 0,
                    "emitter scan radii must not be negative");
            require(thermoregulationTimeConstantSeconds > 0.0,
                    "thermoregulationTimeConstantSeconds must be positive");
            require(minBodyTemperatureCelsius <= initialBodyTemperatureCelsius
                            && initialBodyTemperatureCelsius <= maxBodyTemperatureCelsius,
                    "initial body temperature must lie within its bounds");
            require(rainCoolingFactor > 0.0, "rainCoolingFactor must be positive");
            require(waterCoolingPeakCelsius >= 0.0, "waterCoolingPeakCelsius must not be negative");
            require(heatIndexMaxTemperatureCelsius > heatIndexMinTemperatureCelsius,
                    "the heat index upper bound must exceed the lower bound");
            require(heatEffectThresholdCelsius > coldEffectThresholdCelsius,
                    "the heat threshold must exceed the cold threshold");
            for (ClimateArchetype archetype : ClimateArchetype.values()) {
                require(climateRanges.containsKey(archetype),
                        "no climate range configured for " + archetype);
            }
        }

        private static void require(boolean condition, String message) {
            if (!condition) {
                throw new IllegalStateException("Invalid temperature settings: " + message);
            }
        }
    }
}
