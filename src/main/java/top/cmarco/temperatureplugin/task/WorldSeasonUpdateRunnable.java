package top.cmarco.temperatureplugin.task;

import java.util.Objects;
import java.util.Set;

import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import top.cmarco.temperatureplugin.manager.WorldManager;

public final class WorldSeasonUpdateRunnable implements Runnable {
   private final WorldManager worldManager;

   public WorldSeasonUpdateRunnable(@NotNull WorldManager worldManager) {
      this.worldManager = worldManager;
   }

   public void run() {
      Set<World> var10000 = this.worldManager.getWorlds();
      WorldManager var10001 = this.worldManager;
      Objects.requireNonNull(var10001);
      var10000.forEach(var10001::setWorldSeason);
   }
}
