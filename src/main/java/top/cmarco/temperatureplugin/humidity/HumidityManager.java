package top.cmarco.temperatureplugin.humidity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;
import java.util.Map.Entry;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import top.cmarco.temperatureplugin.TemperaturePlugin;
import top.cmarco.temperatureplugin.data.Tuple;
import top.cmarco.temperatureplugin.manager.PlayerTemperatureManager;
import top.cmarco.temperatureplugin.manager.WorldManager;
import top.cmarco.temperatureplugin.math.MathUtils;

public final class HumidityManager {
   public static final long DAY = 1000L;
   public static final long NIGHT = 13000L;
   public static final long NOON = 6000L;
   public static final long MIDNIGHT = 18000L;
   public static final long SUNRISE = 23000L;
   public static final long SUNSET = 12000L;
   private final HashMap<Biome, Double> biomesHumidity = new HashMap();
   private final TemperaturePlugin plugin;
   private final WorldManager worldManager;
   private final ArrayList<BukkitTask> bukkitTasks = new ArrayList();

   public HumidityManager(@NotNull TemperaturePlugin plugin) {
      this.plugin = plugin;
      this.worldManager = plugin.getWorldManager();
   }

   public void startTask() {
      if (!this.bukkitTasks.isEmpty()) {
         this.stopTask();
      }

      BukkitScheduler scheduler = this.plugin.getServer().getScheduler();
      Iterator var2 = this.worldManager.getWorlds().iterator();

      while(var2.hasNext()) {
         World world = (World)var2.next();
         long timeTicks = world.getTime();
         double scale = MathUtils.CACHED_HUMIDITY_SCALE[(int)timeTicks];
         this.bukkitTasks.add(scheduler.runTaskTimer(this.plugin, () -> {
            Iterator var3 = BiomeHumidity.BIOME_HUMIDITY.entrySet().iterator();

            while(var3.hasNext()) {
               Entry<Biome, Tuple<Double, Double>> entry = (Entry)var3.next();
               Tuple<Double, Double> value = (Tuple)entry.getValue();
               double humDeltaBiome = (Double)value.getB() - (Double)value.getA();
               double hum = humDeltaBiome * scale + (Double)value.getA() + Math.sin(PlayerTemperatureManager.RAND.nextDouble(0.0D, 3.141592653589793D));
               this.biomesHumidity.put((Biome)entry.getKey(), hum);
            }

         }, 1L, 1200L));
      }

   }

   public void stopTask() {
      this.bukkitTasks.stream().filter(Objects::nonNull).forEach(BukkitTask::cancel);
      this.bukkitTasks.clear();
   }

   public double getHumidity(@NotNull Biome biome) {
      return (Double)this.biomesHumidity.get(biome);
   }
}
