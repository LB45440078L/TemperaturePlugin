package top.cmarco.temperatureplugin.config;

import java.util.List;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import top.cmarco.temperatureplugin.TemperaturePlugin;
import top.cmarco.temperatureplugin.performance.Performance;

public final class StandardConfig {
   private final TemperaturePlugin plugin;
   private FileConfiguration config = null;

   public StandardConfig(@NotNull TemperaturePlugin plugin) {
      plugin.reloadConfig();
      this.plugin = plugin;
      plugin.saveDefaultConfig();
      this.config = plugin.getConfig();
   }

   @NotNull
   public List<String> getWorlds() {
      return this.config.getStringList("worlds");
   }

   @NotNull
   public String getUnit() {
      return this.config.getString("temperature-unit", "CELSIUS");
   }

   @NotNull
   public Performance getPerformance() {
      return Performance.valueOf(this.config.getString("performance", "MEDIUM"));
   }

   @NotNull
   public String getActionBarFormat() {
      return this.config.getString("format.action-bar");
   }

   @NotNull
   public String getBarProgressReached() {
      return this.config.getString("format.bar-progress.reached");
   }

   @NotNull
   public String getBarProgressUnreached() {
      return this.config.getString("format.bar-progress.unreached");
   }

   public double getStandardTempsMinimum() {
      return this.config.getDouble("biomes.STANDARD_TEMPS.minimum");
   }

   public double getStandardTempsMaximum() {
      return this.config.getDouble("biomes.STANDARD_TEMPS.maximum");
   }

   public double getColdTempsMinimum() {
      return this.config.getDouble("biomes.COLD_TEMPS.minimum");
   }

   public double getColdTempsMaximum() {
      return this.config.getDouble("biomes.COLD_TEMPS.maximum");
   }

   public double getWarmTempsMinimum() {
      return this.config.getDouble("biomes.WARM_TEMPS.minimum");
   }

   public double getWarmTempsMaximum() {
      return this.config.getDouble("biomes.WARM_TEMPS.maximum");
   }

   public double getOceanTempsMinimum() {
      return this.config.getDouble("biomes.OCEAN_TEMPS.minimum");
   }

   public double getOceanTempsMaximum() {
      return this.config.getDouble("biomes.OCEAN_TEMPS.maximum");
   }

   public double getColdOceanTempsMinimum() {
      return this.config.getDouble("biomes.COLD_OCEAN_TEMPS.minimum");
   }

   public double getColdOceanTempsMaximum() {
      return this.config.getDouble("biomes.COLD_OCEAN_TEMPS.maximum");
   }

   public double getNetherTempsMinimum() {
      return this.config.getDouble("biomes.NETHER_TEMPS.minimum");
   }

   public double getNetherTempsMaximum() {
      return this.config.getDouble("biomes.NETHER_TEMPS.maximum");
   }

   public boolean getActionBarEnabled() {
      return this.config.getBoolean("format.enabled");
   }

   public int getFreezingAnimationSpeed() {
      return this.config.getInt("freezing.animation-speed", 10);
   }

   public double getInterpolation() {
      return this.config.getDouble("interpolation");
   }

   public double getLeatherArmourTempIncrease() {
      return this.config.getDouble("leather-armour-temp-increase");
   }
}
