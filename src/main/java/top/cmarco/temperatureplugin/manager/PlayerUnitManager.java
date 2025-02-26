package top.cmarco.temperatureplugin.manager;

import java.util.HashMap;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import top.cmarco.temperatureplugin.TemperaturePlugin;
import top.cmarco.temperatureplugin.temperature.Temperature;

public final class PlayerUnitManager {
   private final TemperaturePlugin temperaturePlugin;
   private final HashMap<UUID, Temperature> playerUnit = new HashMap();
   private final HashMap<UUID, Long> unitUpdate = new HashMap();

   public PlayerUnitManager(TemperaturePlugin temperaturePlugin) {
      this.temperaturePlugin = temperaturePlugin;
   }

   @NotNull
   public Temperature getPlayerTemperature(@NotNull Player player) {
      UUID uuid = player.getUniqueId();
      if (this.playerUnit.containsKey(uuid) && (double)(System.currentTimeMillis() - (Long)this.unitUpdate.get(uuid)) < 10000.0D) {
         return (Temperature)this.playerUnit.get(uuid);
      } else {
         NamespaceManager namespaceManager = this.temperaturePlugin.getNamespaceManager();
         Temperature temperature = namespaceManager.getPlayerTemperature(player);
         if (temperature == null) {
            namespaceManager.setTemperatureUnit(player, this.temperaturePlugin.getStandardConfig().getUnit());
            temperature = namespaceManager.getPlayerTemperature(player);
         }

         this.playerUnit.put(uuid, temperature);
         this.unitUpdate.put(uuid, System.currentTimeMillis());
         return temperature;
      }
   }

   public void setTemperature(@NotNull Player player, @NotNull Temperature temperature) {
      NamespaceManager namespaceManager = this.temperaturePlugin.getNamespaceManager();
      UUID uuid = player.getUniqueId();
      this.playerUnit.put(uuid, temperature);
      this.unitUpdate.put(uuid, System.currentTimeMillis());
      namespaceManager.setTemperatureUnit(player, temperature.getName());
   }

   public void clearAll() {
      this.unitUpdate.clear();
      this.playerUnit.clear();
   }
}
