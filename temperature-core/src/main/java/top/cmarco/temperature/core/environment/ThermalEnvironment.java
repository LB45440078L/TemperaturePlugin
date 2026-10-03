package top.cmarco.temperature.core.environment;

import java.util.List;
import java.util.Objects;
import top.cmarco.temperature.core.climate.BiomeClimate;

/**
 * A frozen snapshot of everything about the player's surroundings that can influence perceived
 * temperature.
 *
 * <p>This is the single seam between the platform and the simulation. The Spigot adapter measures
 * the world and fills this in; the simulation reads it and never asks the server a question. That
 * is what lets the whole model be exercised in a unit test with no server, no scheduler and no
 * world — and it is why the record is immutable: a tick's computation cannot be perturbed by a
 * later mutation of its input.
 *
 * <p>Built through {@link #builder()}; the record itself is only the product.
 *
 * @param climate         the biome's resolved climate band
 * @param altitude        the sample's height above the world floor, in blocks
 * @param worldMinY       the world's lowest buildable height
 * @param worldMaxY       the world's highest buildable height
 * @param exposedToSky    whether nothing solid stands between the sample and the sky
 * @param precipitating   whether it is raining or snowing at the sample
 * @param submerged       whether the player's body is in water
 * @param gameTime        the world's game time, in ticks
 * @param emitters        the relevant blocks found near the player
 * @param armor           what the player is wearing
 * @param activeEffects   timed additive effects currently in force
 * @param humidityJitter  a small noise term added to humidity, in percent
 */
public record ThermalEnvironment(
        BiomeClimate climate,
        double altitude,
        double worldMinY,
        double worldMaxY,
        boolean exposedToSky,
        boolean precipitating,
        boolean submerged,
        long gameTime,
        List<EmitterSample> emitters,
        ArmorProfile armor,
        List<ActiveEffect> activeEffects,
        double humidityJitter) {

    public ThermalEnvironment {
        Objects.requireNonNull(climate, "climate");
        Objects.requireNonNull(armor, "armor");
        emitters = List.copyOf(emitters);
        activeEffects = List.copyOf(activeEffects);
    }

    /** @return a builder with conservative defaults, ready to be specialised. */
    public static Builder builder() {
        return new Builder();
    }

    /** Mutable builder for {@link ThermalEnvironment}. */
    public static final class Builder {

        private BiomeClimate climate;
        private double altitude;
        private double worldMinY = -64.0;
        private double worldMaxY = 320.0;
        private boolean exposedToSky = true;
        private boolean precipitating;
        private boolean submerged;
        private long gameTime;
        private List<EmitterSample> emitters = List.of();
        private ArmorProfile armor = ArmorProfile.NONE;
        private List<ActiveEffect> activeEffects = List.of();
        private double humidityJitter;

        private Builder() {
        }

        public Builder climate(BiomeClimate climate) {
            this.climate = climate;
            return this;
        }

        public Builder altitude(double altitude) {
            this.altitude = altitude;
            return this;
        }

        public Builder worldBounds(double worldMinY, double worldMaxY) {
            this.worldMinY = worldMinY;
            this.worldMaxY = worldMaxY;
            return this;
        }

        public Builder exposedToSky(boolean exposedToSky) {
            this.exposedToSky = exposedToSky;
            return this;
        }

        public Builder precipitating(boolean precipitating) {
            this.precipitating = precipitating;
            return this;
        }

        public Builder submerged(boolean submerged) {
            this.submerged = submerged;
            return this;
        }

        public Builder gameTime(long gameTime) {
            this.gameTime = gameTime;
            return this;
        }

        public Builder emitters(List<EmitterSample> emitters) {
            this.emitters = emitters;
            return this;
        }

        public Builder armor(ArmorProfile armor) {
            this.armor = armor;
            return this;
        }

        public Builder activeEffects(List<ActiveEffect> activeEffects) {
            this.activeEffects = activeEffects;
            return this;
        }

        public Builder humidityJitter(double humidityJitter) {
            this.humidityJitter = humidityJitter;
            return this;
        }

        public ThermalEnvironment build() {
            return new ThermalEnvironment(climate, altitude, worldMinY, worldMaxY, exposedToSky,
                    precipitating, submerged, gameTime, emitters, armor, activeEffects,
                    humidityJitter);
        }
    }
}
