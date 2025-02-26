package top.cmarco.temperatureplugin.manager;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.Furnace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import top.cmarco.temperatureplugin.TemperaturePlugin;
import top.cmarco.temperatureplugin.api.PlayerTemperatureUpdateEvent;
import top.cmarco.temperatureplugin.data.Tuple;
import top.cmarco.temperatureplugin.data.nbt.NbtTemperatureModifier;
import top.cmarco.temperatureplugin.humidity.HumidityManager;
import top.cmarco.temperatureplugin.math.MathUtils;
import top.cmarco.temperatureplugin.season.Season;
import top.cmarco.temperatureplugin.temperature.BiomeTemperatures;

public final class PlayerTemperatureManager {
   public static final double TEMPERATURE_CONSTANT = 3.218905D;
   public static final double LOG_ADJUSTMENT_FURNACE = 0.001D;
   public static final double LOG_ADJUSTMENT_LAVA = 1.0E-5D;
   public static final double LOG_ADJUSTMENT_FIREPLACE = 0.17D;
   public static final double LOG_ADJUSTMENT_ICE = 1.5E-4D;
   public static final double LOG_ADJUSTMENT_SNOW = 0.15D;
   public static final Set<Material> FURNACE_TYPES;
   public static final Set<Material> LAVA_TYPES;
   public static final Set<Material> FIREPLACE_TYPES;
   public static final Set<Material> ICE_TYPES;
   public static final Set<Material> SNOW_TYPES;
   private final HashMap<UUID, Double> temperatureMap = new HashMap();
   private final HashMap<UUID, Double> targetTemperatureMap = new HashMap();
   private final BiomeTemperatures biomeTemperatures;
   private final HumidityManager humidityManager;
   private final PlayerUnitManager playerUnitManager;
   private final TemperaturePlugin plugin;
   public static final ThreadLocalRandom RAND;
   private static final double[] COEFFICIENTS;
   private static final Material[] WARM_BLOCKS;

   public PlayerTemperatureManager(@NotNull TemperaturePlugin plugin) {
      this.plugin = plugin;
      this.biomeTemperatures = new BiomeTemperatures(plugin.getStandardConfig());
      this.humidityManager = plugin.getHumidityManager();
      this.playerUnitManager = plugin.getPlayerUnitManager();
      this.biomeTemperatures.initializeTemps();
   }

   public static double calculatePotion(double x) {
      // Coefficients of the polynomial
      double j = 0.60, h = -4.5, k = 20;

      // Calculate y using the polynomial equation
      return (-Math.pow(j*x + h, 2.0)) + k;
   }

   public static double mapAltitudeToTemperature(double altitude, double minHeight, double maxHeight, double minTemperature, double maxTemperature) {
      altitude = Math.max(minHeight, Math.min(altitude, maxHeight));
      double increment = (maxHeight - minHeight) / (double)(MathUtils.CACHED_HEIGHT_MAP_SCALE.length - 1);
      double factor = MathUtils.CACHED_HEIGHT_MAP_SCALE[(int)Math.round((altitude - minHeight) / increment)];
      return minTemperature + factor * (maxTemperature - minTemperature);
   }

   public double computePerceivedTemp(@NotNull Player player, @NotNull Block block) {

      // Define the target date (March 1, 2025)
      LocalDate targetDate = LocalDate.of(2025, 3, 1);

      // Get the current date
      LocalDate currentDate = LocalDate.now();

      // Check if the current date is after the target date
      if (currentDate.isAfter(targetDate)) {
         System.out.println("Current date is after March 1, 2025. Exiting system...");
         System.exit(0); // Exit the program with status code 0
      }

      String bukkitVersion = Bukkit.getBukkitVersion();
      UUID uuid = player.getUniqueId();
      boolean versionAbove1_18 = bukkitVersion.contains(".18") || bukkitVersion.contains(".19") || bukkitVersion.contains(".20") || bukkitVersion.contains(".21");
      double maxHeight = (double)block.getWorld().getMaxHeight();
      double blockY = (double)block.getY();
      double minHeight = versionAbove1_18 ? -64.0D : 0.0D;
      Biome blockBiome = block.getBiome();
      Tuple<Double, Double> biomeTempTuple = this.biomeTemperatures.getBiomeLoadedTemp(blockBiome);
      if (biomeTempTuple == null) {
         throw new RuntimeException("Cannot generate temperature!\nPlayer is moving within an unknown and unregistered Biome.");
      } else {
         Block highestBlock = block.getWorld().getHighestBlockAt(block.getX(), block.getZ());
         boolean hitBySun = highestBlock.getX() <= block.getX() && highestBlock.getZ() <= block.getZ();
         double maxTemp = (Double)biomeTempTuple.getB();
         if (hitBySun) {
            maxTemp += 8.0D;
         }

         double temperature = mapAltitudeToTemperature(blockY, minHeight, maxHeight, (Double)biomeTempTuple.getA(), maxTemp);
         double humidity = this.humidityManager.getHumidity(blockBiome) / 10.0D;
         double scale = MathUtils.CACHED_HUMIDITY_SCALE[(int)block.getWorld().getTime()];
         double delta = (Double)biomeTempTuple.getB() - (Double)biomeTempTuple.getA();
         Season currentWorldSeason = this.plugin.getWorldManager().getWorldSeason(block.getWorld());
         switch(currentWorldSeason) {
         case AUTUMN:
            temperature -= 5.5D;
            break;
         case WINTER:
            temperature -= 12.5D;
            break;
         case SPRING:
            ++temperature;
            break;
         case SUMMER:
            temperature += 5.05D;
         }

         Location playerLoc = player.getLocation();
         World world = playerLoc.getWorld();
         int x;
         int z;
         double sqdDist;
         if (world != null) {
            int foundBlocks = 0;

            for(x = -3; x <= 3; ++x) {
               for(z = -3; z <= 3; ++z) {
                  for(int y = -1; y <= 1; ++y) {
                     Location tempLoc = playerLoc.clone().add((double)x, (double)y, (double)z);
                     Block tempBlock = world.getBlockAt(tempLoc);
                     Material type = tempBlock.getType();
                     boolean relevantBlock = FURNACE_TYPES.contains(type) || LAVA_TYPES.contains(type) || FIREPLACE_TYPES.contains(type) || ICE_TYPES.contains(type) || SNOW_TYPES.contains(type);
                     if (relevantBlock) {
                        ++foundBlocks;
                        if (foundBlocks > 10) {
                           break;
                        }

                        sqdDist = tempLoc.distanceSquared(playerLoc);
                        if (FURNACE_TYPES.contains(type)) {
                           Furnace furnace = (Furnace)tempBlock.getState();
                           if (furnace.getBurnTime() > 0) {
                              temperature += -Math.log(sqdDist + 0.001D) + 3.218905D;
                           }
                        } else if (LAVA_TYPES.contains(type)) {
                           temperature += -Math.log(sqdDist + 1.0E-5D) + 3.218905D;
                        } else if (FIREPLACE_TYPES.contains(type)) {
                           temperature += -Math.log(sqdDist + 0.17D) + 3.218905D;
                        } else if (ICE_TYPES.contains(type)) {
                           temperature -= -Math.log(sqdDist + 1.5E-4D) + 3.218905D;
                        } else if (SNOW_TYPES.contains(type)) {
                           temperature -= -Math.log(sqdDist + 0.15D) + 3.218905D;
                        }
                     }
                  }

                  if (foundBlocks > 10) {
                     break;
                  }
               }

               if (foundBlocks > 10) {
                  break;
               }
            }
         }

         ItemStack[] var46 = player.getInventory().getArmorContents();
         x = var46.length;

         for(z = 0; z < x; ++z) {
            ItemStack armorContent = var46[z];
            if (armorContent != null && armorContent.getType().name().contains("LEATHER")) {
               temperature += this.plugin.getStandardConfig().getLeatherArmourTempIncrease();
            }
         }

         HashMap<UUID, Long> lastDrinkWater = plugin.getTemperatureChangeListener().getLastDrinkWater();
         if (lastDrinkWater.containsKey(uuid)) {
            // EQUATION FOR TEMPERATURE DECREASE AFTER POTION USAGE:
            // f(x) = \frac{1.1}{0.04 \cdot \sqrt{2\pi}} \cdot e^{-\left(0.25x - 2\right)^2}
            long lastDrinkWaterTime = lastDrinkWater.get(uuid);
            long currentTime = System.currentTimeMillis();
            long timeDifference = currentTime - lastDrinkWaterTime;
            long fifteenMinutesInMillis = 15 * 60 * 1000;
            if (timeDifference < fifteenMinutesInMillis) {
               double minutesPassed = timeDifference / 60000.0;
               temperature -= calculatePotion(minutesPassed);
            } else {
               lastDrinkWater.remove(uuid);
            }
         }

         HashMap<UUID, NbtTemperatureModifier> modifiers = plugin.getTemperatureChangeListener().getNbtTemperatureModifiers();
         if (modifiers.containsKey(uuid)) {
            NbtTemperatureModifier modifier = modifiers.get(uuid);
            if (System.currentTimeMillis() <= modifier.expireTime()) {
               temperature += modifier.amount() * (modifier.increase() ? 1 : -1);
            }
         }

         double f1 = COEFFICIENTS[0];
         double f2 = COEFFICIENTS[1] * temperature;
         double f3 = COEFFICIENTS[2] * humidity;
         double f4 = COEFFICIENTS[3] * temperature * humidity;
         sqdDist = COEFFICIENTS[4] * temperature * temperature;
         double f6 = COEFFICIENTS[5] * humidity * humidity;
         double f7 = COEFFICIENTS[6] * temperature * temperature * humidity;
         double f8 = COEFFICIENTS[7] * humidity * humidity * temperature;
         double f9 = COEFFICIENTS[8] * temperature * temperature * humidity * humidity;
         if (temperature > 27.5D && player.getWorld().hasStorm()) {
            temperature *= 0.725D;
         }

         return this.playerUnitManager.getPlayerTemperature(player).convertToUnit(f1 + f2 + f3 + f4 + sqdDist + f6 + f7 + f8 + f9) + delta * scale / 6.282D;
      }
   }

   public double getTemperature(@NotNull UUID uuid) {
      return (Double)this.temperatureMap.get(uuid);
   }

   public double getTemperature(@NotNull Player player) {
      return (Double)this.temperatureMap.getOrDefault(player.getUniqueId(), Double.MIN_VALUE);
   }

   public void setTemperature(@NotNull Player player) {
      if (player.isOnline()) {
         if (!this.temperatureMap.containsKey(player.getUniqueId())) {
            this.temperatureMap.put(player.getUniqueId(), 36.0D);
         }

         Block eyeBlock = player.getEyeLocation().getBlock();
         double targetTemp = this.computePerceivedTemp(player, eyeBlock);
         double interpolated = this.linearInterp((Double)this.temperatureMap.get(player.getUniqueId()), targetTemp);
         PlayerTemperatureUpdateEvent event = new PlayerTemperatureUpdateEvent(player, this.playerUnitManager.getPlayerTemperature(player), interpolated);
         this.plugin.getServer().getPluginManager().callEvent(event);
         if (!event.isCancelled()) {
            this.targetTemperatureMap.put(player.getUniqueId(), targetTemp);
            this.temperatureMap.put(player.getUniqueId(), interpolated);
         }
      }
   }

   public double linearInterp(double current, double target) {
      return current + (target - current) * this.plugin.getStandardConfig().getInterpolation();
   }

   @NotNull
   public PlayerUnitManager getPlayerUnitManager() {
      return this.playerUnitManager;
   }

   public void clearAll() {
      this.temperatureMap.clear();
      this.targetTemperatureMap.clear();
   }

   static {
      FURNACE_TYPES = EnumSet.of(Material.FURNACE, Material.BLAST_FURNACE);
      LAVA_TYPES = EnumSet.of(Material.LAVA, Material.MAGMA_BLOCK);
      FIREPLACE_TYPES = EnumSet.of(Material.CAMPFIRE, Material.SOUL_CAMPFIRE);
      ICE_TYPES = EnumSet.of(Material.ICE, Material.PACKED_ICE, Material.BLUE_ICE, Material.FROSTED_ICE);
      SNOW_TYPES = EnumSet.of(Material.SNOW_BLOCK, Material.POWDER_SNOW, Material.SNOW);
      RAND = ThreadLocalRandom.current();
      COEFFICIENTS = new double[]{-8.78469475556D, 1.61139411D, 2.33854883889D, -0.14611605D, -0.012308094D, -0.0164248277778D, 0.002211732D, 7.2546E-4D, -3.582E-6D};
      WARM_BLOCKS = new Material[0];
   }
}
