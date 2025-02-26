package top.cmarco.temperatureplugin.manager;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.cmarco.temperatureplugin.TemperaturePlugin;
import top.cmarco.temperatureplugin.temperature.Temperature;

public final class NamespaceManager {
   private final TemperaturePlugin plugin;
   private final NamespacedKey enabledKey;
   private final NamespacedKey unitKey;

   public NamespaceManager(@NotNull TemperaturePlugin plugin) {
      this.plugin = plugin;
      this.enabledKey = new NamespacedKey(plugin, "temperature_enabled");
      this.unitKey = new NamespacedKey(plugin, "temperature_unit");
   }

   public void enableTemperature(@NotNull Player player) {
      PersistentDataContainer pdc = player.getPersistentDataContainer();
      pdc.set(this.enabledKey, PersistentDataType.BYTE, (byte)0);
   }

   public void disableTemperature(@NotNull Player player) {
      PersistentDataContainer pdc = player.getPersistentDataContainer();
      pdc.set(this.enabledKey, PersistentDataType.BYTE, (byte)1);
   }

   public void setTemperatureUnit(@NotNull Player player, @NotNull String unit) {
      PersistentDataContainer pdc = player.getPersistentDataContainer();

      try {
         Temperature temperature = Temperature.valueOf(unit);
         pdc.set(this.unitKey, PersistentDataType.STRING, unit);
      } catch (IllegalArgumentException var5) {
         this.plugin.getLogger().warning("WARNING! Passed illegal unit value as " + unit);
      }

   }

   @Nullable
   public Temperature getPlayerTemperature(@NotNull Player player) {
      PersistentDataContainer pdc = player.getPersistentDataContainer();
      String string = (String)pdc.get(this.unitKey, PersistentDataType.STRING);
      return string == null ? null : Temperature.valueOf(string);
   }

   public boolean isEnableTemperature(@NotNull Player player) {
      PersistentDataContainer pdc = player.getPersistentDataContainer();
      Byte value = (Byte)pdc.get(this.enabledKey, PersistentDataType.BYTE);
      return value == null || value == 0;
   }
}
