package top.cmarco.temperatureplugin.task;

import java.util.Iterator;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import top.cmarco.temperatureplugin.TemperaturePlugin;
import top.cmarco.temperatureplugin.config.StandardConfig;
import top.cmarco.temperatureplugin.manager.NamespaceManager;
import top.cmarco.temperatureplugin.manager.PlayerTemperatureManager;
import top.cmarco.temperatureplugin.manager.WorldManager;
import top.cmarco.temperatureplugin.season.Season;
import top.cmarco.temperatureplugin.temperature.Temperature;
import top.cmarco.temperatureplugin.utilities.ChatUtils;

public final class TemperatureDisplayRunnable implements Runnable {
   private final WorldManager worldManager;
   private final NamespaceManager namespaceManager;
   private final PlayerTemperatureManager temperatureManager;
   private final TemperaturePlugin plugin;

   public TemperatureDisplayRunnable(@NotNull WorldManager worldManager, @NotNull NamespaceManager namespaceManager, @NotNull PlayerTemperatureManager playerTemperatureManager, @NotNull TemperaturePlugin plugin) {
      this.worldManager = worldManager;
      this.namespaceManager = namespaceManager;
      this.temperatureManager = playerTemperatureManager;
      this.plugin = plugin;
   }

   @NotNull
   private String getFormatTemperature(@NotNull Player player, double temp) {
      StandardConfig config = this.worldManager.getPlugin().getStandardConfig();
      Season currentSeason = this.worldManager.getWorldSeason(player.getWorld());
      Temperature playerTemp = this.temperatureManager.getPlayerUnitManager().getPlayerTemperature(player);
      double minConverted = playerTemp.convertToUnit(-40.0D);
      double maxConverted = playerTemp.convertToUnit(40.0D);
      double step = (maxConverted - minConverted) / 10.0D;
      StringBuilder bar = new StringBuilder();
      double celsiusTemp = playerTemp.convertUnitToCelsius(temp);
      char relativeTempColour = (char)(celsiusTemp < 5.0D ? 98 : (celsiusTemp < 29.5D ? 101 : 99));

      for(int i = 1; i <= 10; ++i) {
         if ((double)i * step + minConverted >= temp && i != 1) {
            bar.append("&7").append(config.getBarProgressUnreached());
         } else {
            bar.append("&").append(relativeTempColour).append(config.getBarProgressReached());
         }
      }

      return ChatUtils.colorStd(config.getActionBarFormat().replace("{PROGRESS}", bar.toString()).replace("{TEMP}", String.format("&%c%.1f%s", relativeTempColour, temp, playerTemp.getName())).replace("{SEASON}", currentSeason.getName()));
   }

   private void display(@NotNull Player player) {
      double temp = this.temperatureManager.getTemperature(player);
      player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(this.getFormatTemperature(player, temp)));
   }

   public void run() {
      if (this.plugin.getStandardConfig().getActionBarEnabled()) {
         Iterator var1 = this.worldManager.getWorlds().iterator();

         while(var1.hasNext()) {
            World world = (World)var1.next();
            Iterator var3 = world.getPlayers().iterator();

            while(var3.hasNext()) {
               Player player = (Player)var3.next();
               if (this.namespaceManager.isEnableTemperature(player)) {
                  this.display(player);
               }
            }
         }

      }
   }
}
