package top.cmarco.temperatureplugin.temperature;

import java.util.*;

import org.bukkit.Registry;
import org.bukkit.block.Biome;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.cmarco.temperatureplugin.config.StandardConfig;
import top.cmarco.temperatureplugin.data.Tuple;
import top.cmarco.temperatureplugin.data.impl.DoublesTuple;

public final class BiomeTemperatures {
   private final StandardConfig config;
   private final Map<TemperatureType, Tuple<Double, Double>> loadedTemperatureTypes = new HashMap<>();
   private final Map<Biome, Tuple<Double, Double>> biomeTemps = new HashMap<>();

   public BiomeTemperatures(@NotNull StandardConfig standardConfig) {
      this.config = standardConfig;
   }

   public void initializeTemps() {
      Tuple<Double, Double> STANDARD_TEMPS = DoublesTuple.getBuilder().setFirst(this.config.getStandardTempsMinimum()).setSecond(this.config.getStandardTempsMaximum()).build();
      Tuple<Double, Double> COLD_TEMPS = DoublesTuple.getBuilder().setFirst(this.config.getColdTempsMinimum()).setSecond(this.config.getColdTempsMaximum()).build();
      Tuple<Double, Double> WARM_TEMPS = DoublesTuple.getBuilder().setFirst(this.config.getWarmTempsMinimum()).setSecond(this.config.getWarmTempsMaximum()).build();
      Tuple<Double, Double> OCEAN_TEMPS = DoublesTuple.getBuilder().setFirst(this.config.getOceanTempsMinimum()).setSecond(this.config.getOceanTempsMaximum()).build();
      Tuple<Double, Double> COLD_OCEAN_TEMPS = DoublesTuple.getBuilder().setFirst(this.config.getColdOceanTempsMinimum()).setSecond(this.config.getColdOceanTempsMaximum()).build();
      Tuple<Double, Double> NETHER_TEMPS = DoublesTuple.getBuilder().setFirst(this.config.getNetherTempsMinimum()).setSecond(this.config.getNetherTempsMaximum()).build();
      this.loadedTemperatureTypes.put(TemperatureType.STANDARD_TEMPS, STANDARD_TEMPS);
      this.loadedTemperatureTypes.put(TemperatureType.COLD_TEMPS, COLD_TEMPS);
      this.loadedTemperatureTypes.put(TemperatureType.WARM_TEMPS, WARM_TEMPS);
      this.loadedTemperatureTypes.put(TemperatureType.OCEAN_TEMPS, OCEAN_TEMPS);
      this.loadedTemperatureTypes.put(TemperatureType.COLD_OCEAN_TEMPS, COLD_OCEAN_TEMPS);
      this.loadedTemperatureTypes.put(TemperatureType.NETHER_TEMPS, NETHER_TEMPS);
      this.assignTemperatures();
   }

   private void assignTemperatures() {
      Registry.BIOME.forEach(biome -> this.biomeTemps.put(biome, Objects.requireNonNull(this.loadedTemperatureTypes.get(this.determineTemperature(biome)), "Unknown TemperatureType")));
   }

   @NotNull
   private TemperatureType determineTemperature(@NotNull Biome biome) {
      String biomeName = biome.name().toLowerCase();
      if (biomeName.contains("ocean")) {
         return TemperatureType.OCEAN_TEMPS;
      } else if (!biomeName.contains("plains") && !biomeName.contains("river") && !biomeName.contains("shore") && !biomeName.contains("beach") && !biomeName.contains("plateau") && !biomeName.contains("islands") && !biomeName.contains("forest") && !biomeName.contains("caves") && !biomeName.contains("meadow") && !biomeName.contains("grove") && !biomeName.contains("peaks")) {
         if (!biomeName.contains("desert") && !biomeName.contains("jungle") && !biomeName.contains("savanna") && !biomeName.contains("badlands") && !biomeName.contains("sand") && !biomeName.contains("barrens") && !biomeName.contains("wasteland") && !biomeName.contains("valley") && !biomeName.contains("dark") && !biomeName.contains("stony")) {
            if (!biomeName.contains("taiga") && !biomeName.contains("snow") && !biomeName.contains("frozen") && !biomeName.contains("ice") && !biomeName.contains("peak")) {
               return biomeName.contains("nether") ? TemperatureType.NETHER_TEMPS : TemperatureType.STANDARD_TEMPS;
            } else {
               return TemperatureType.COLD_TEMPS;
            }
         } else {
            return TemperatureType.WARM_TEMPS;
         }
      } else {
         return TemperatureType.STANDARD_TEMPS;
      }
   }

   @Nullable
   public Tuple<Double, Double> getBiomeLoadedTemp(@NotNull Biome biome) {
      return (Tuple)this.biomeTemps.get(biome);
   }
}
