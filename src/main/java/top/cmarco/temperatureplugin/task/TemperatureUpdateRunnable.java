package top.cmarco.temperatureplugin.task;

import java.util.Objects;
import java.util.stream.Stream;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import top.cmarco.temperatureplugin.manager.PlayerTemperatureManager;
import top.cmarco.temperatureplugin.manager.WorldManager;

public class TemperatureUpdateRunnable implements Runnable {
   private final PlayerTemperatureManager playerTemperatureManager;
   private final WorldManager worldManager;

   public TemperatureUpdateRunnable(@NotNull PlayerTemperatureManager playerTemperatureManager, @NotNull WorldManager worldManager) {
      this.playerTemperatureManager = playerTemperatureManager;
      this.worldManager = worldManager;
   }

   public void run() {
      Stream<Player> var10000 = this.worldManager.getWorlds().stream().flatMap((w) -> {
         return w.getPlayers().stream();
      });
      PlayerTemperatureManager var10001 = this.playerTemperatureManager;
      Objects.requireNonNull(var10001);
      var10000.forEach(var10001::setTemperature);
   }
}
