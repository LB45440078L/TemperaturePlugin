package top.cmarco.temperatureplugin.ui;

import org.bukkit.Server;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import top.cmarco.temperatureplugin.TemperaturePlugin;
import top.cmarco.temperatureplugin.manager.PlayerTemperatureManager;
import top.cmarco.temperatureplugin.performance.Performance;
import top.cmarco.temperatureplugin.task.TemperatureDisplayRunnable;
import top.cmarco.temperatureplugin.task.TemperatureUpdateRunnable;

public final class TemperatureDisplay {
   private final TemperaturePlugin plugin;
   private BukkitTask updaterTask = null;
   private TemperatureDisplayRunnable temperatureDisplayRunnable = null;
   private TemperatureUpdateRunnable temperatureUpdateRunnable = null;
   private final PlayerTemperatureManager playerTemperatureManager;

   public TemperatureDisplay(@NotNull TemperaturePlugin plugin) {
      this.plugin = plugin;
      this.playerTemperatureManager = new PlayerTemperatureManager(plugin);
   }

   public void startTask() {
      if (this.updaterTask != null) {
         this.stopTask();
      }

      Server server = this.plugin.getServer();
      BukkitScheduler scheduler = server.getScheduler();
      this.temperatureDisplayRunnable = new TemperatureDisplayRunnable(this.plugin.getWorldManager(), this.plugin.getNamespaceManager(), this.playerTemperatureManager, this.plugin);
      this.temperatureUpdateRunnable = new TemperatureUpdateRunnable(this.playerTemperatureManager, this.plugin.getWorldManager());
      Performance performanceOption = this.plugin.getStandardConfig().getPerformance();
      scheduler.runTaskTimer(this.plugin, this.temperatureUpdateRunnable, 1L, performanceOption.getUpdateValue());
      scheduler.runTaskTimer(this.plugin, this.temperatureDisplayRunnable, 2L, 1L);
   }

   public void stopTask() {
      if (this.updaterTask != null) {
         this.updaterTask.cancel();
         this.updaterTask = null;
         this.temperatureDisplayRunnable = null;
      }
   }

   @NotNull
   public PlayerTemperatureManager getPlayerTemperatureManager() {
      return this.playerTemperatureManager;
   }
}
