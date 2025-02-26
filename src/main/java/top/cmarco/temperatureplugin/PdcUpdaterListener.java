package top.cmarco.temperatureplugin;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;
import top.cmarco.temperatureplugin.manager.NamespaceManager;

public class PdcUpdaterListener implements Listener {
   private final TemperaturePlugin plugin;

   public PdcUpdaterListener(@NotNull TemperaturePlugin plugin) {
      this.plugin = plugin;
   }

   @EventHandler
   public void onEvent(@NotNull PlayerJoinEvent event) {
      NamespaceManager namespaceManager = this.plugin.getNamespaceManager();
      Player player = event.getPlayer();
   }
}
