package top.cmarco.temperatureplugin.api;

import java.util.Locale;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.cmarco.temperatureplugin.TemperaturePlugin;
import top.cmarco.temperatureplugin.manager.PlayerTemperatureManager;

public final class TemperatureExpansion extends PlaceholderExpansion {
   private final TemperaturePlugin plugin;

   public TemperatureExpansion(@NotNull TemperaturePlugin plugin) {
      this.plugin = plugin;
   }

   @NotNull
   public String getIdentifier() {
      return "temperature";
   }

   @NotNull
   public String getAuthor() {
      return "CMarco";
   }

   @NotNull
   public String getVersion() {
      return this.plugin.getDescription().getVersion();
   }

   public String onPlaceholderRequest(@Nullable Player player, @NotNull String identifier) {
      if (player == null) {
         return "";
      } else {
         PlayerTemperatureManager temperatureManager = this.plugin.getTemperatureDisplay().getPlayerTemperatureManager();
         String var4 = identifier.toLowerCase(Locale.ROOT);
         byte var5 = -1;
         switch(var4.hashCode()) {
         case 321701236:
            if (var4.equals("temperature")) {
               var5 = 0;
            }
            break;
         case 548027571:
            if (var4.equals("humidity")) {
               var5 = 1;
            }
         }

         String var10000;
         switch(var5) {
         case 0:
            var10000 = String.format("%.1f", temperatureManager.getTemperature(player));
            break;
         case 1:
            var10000 = String.format("%.1f", this.plugin.getHumidityManager().getHumidity(player.getLocation().getBlock().getBiome()));
            break;
         default:
            var10000 = null;
         }

         return var10000;
      }
   }
}
