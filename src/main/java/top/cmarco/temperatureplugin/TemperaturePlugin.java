package top.cmarco.temperatureplugin;

import de.tr7zw.changeme.nbtapi.NBT;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import top.cmarco.temperatureplugin.api.TemperatureExpansion;
import top.cmarco.temperatureplugin.commands.TemperatureCommand;
import top.cmarco.temperatureplugin.config.StandardConfig;
import top.cmarco.temperatureplugin.humidity.HumidityManager;
import top.cmarco.temperatureplugin.listener.TemperatureChangeListener;
import top.cmarco.temperatureplugin.manager.NamespaceManager;
import top.cmarco.temperatureplugin.manager.PlayerUnitManager;
import top.cmarco.temperatureplugin.manager.WorldManager;
import top.cmarco.temperatureplugin.task.WorldSeasonUpdateRunnable;
import top.cmarco.temperatureplugin.ui.TemperatureDisplay;

public final class TemperaturePlugin extends JavaPlugin {
   private StandardConfig standardConfig = null;
   private WorldManager worldManager = null;
   private NamespaceManager namespaceManager = null;
   private HumidityManager humidityManager = null;
   private TemperatureDisplay temperatureDisplay = null;
   private PlayerUnitManager playerUnitManager;
   private TemperatureChangeListener temperatureChangeListener = null;

   public void onEnable() {

      if (!NBT.preloadApi()) {
         getLogger().warning("NBT-API wasn't initialized properly, disabling the plugin.");
         getPluginLoader().disablePlugin(this);
         return;
      }

      this.loadConfig();
      this.worldManager = new WorldManager(this);
      this.namespaceManager = new NamespaceManager(this);
      this.playerUnitManager = new PlayerUnitManager(this);
      this.loadHumidityManager();
      this.startSeasonUpdater();
      this.loadTempDisplay();
      this.registerListeners();
      this.assignCommand();
      this.registerPapi();
   }

   public void reload() {
      this.standardConfig = new StandardConfig(this);
      this.playerUnitManager.clearAll();
      this.temperatureDisplay.getPlayerTemperatureManager().clearAll();
      this.temperatureDisplay.stopTask();
      this.temperatureDisplay.startTask();
   }

   public void onDisable() {
      this.temperatureDisplay.stopTask();
      this.humidityManager.stopTask();
   }

   private void loadConfig() {
      this.standardConfig = new StandardConfig(this);
   }

   private void assignCommand() {
      this.getCommand("temperature").setExecutor(new TemperatureCommand(this));
   }

   private void startSeasonUpdater() {
      this.getServer().getScheduler().runTaskTimer(this, new WorldSeasonUpdateRunnable(this.worldManager), 0L, 1200L);
   }

   private void loadTempDisplay() {
      this.temperatureDisplay = new TemperatureDisplay(this);
      this.temperatureDisplay.startTask();
   }

   private void loadHumidityManager() {
      this.humidityManager = new HumidityManager(this);
      this.humidityManager.startTask();
   }

   private void registerListeners() {
      PluginManager pluginManager = this.getServer().getPluginManager();
      if (this.temperatureChangeListener != null) {
         this.temperatureChangeListener = null;
      }

      this.temperatureChangeListener = new TemperatureChangeListener(this);
      pluginManager.registerEvents(this.temperatureChangeListener, this);
   }

   private void registerPapi() {
      if (this.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
         TemperatureExpansion temperatureExpansion = new TemperatureExpansion(this);
         if (temperatureExpansion.canRegister()) {
            temperatureExpansion.register();
            this.getLogger().info("Successfully registered Temperature PlaceholderAPI expansion!");
         }
      }

   }

   @NotNull
   public StandardConfig getStandardConfig() {
      return this.standardConfig;
   }

   @NotNull
   public WorldManager getWorldManager() {
      return this.worldManager;
   }

   @NotNull
   public NamespaceManager getNamespaceManager() {
      return this.namespaceManager;
   }

   @NotNull
   public TemperatureDisplay getTemperatureDisplay() {
      return this.temperatureDisplay;
   }

   @NotNull
   public HumidityManager getHumidityManager() {
      return this.humidityManager;
   }

   public PlayerUnitManager getPlayerUnitManager() {
      return this.playerUnitManager;
   }

   public TemperatureChangeListener getTemperatureChangeListener() {
      return temperatureChangeListener;
   }
}
