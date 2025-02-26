package top.cmarco.temperatureplugin.manager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Server;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import top.cmarco.temperatureplugin.TemperaturePlugin;
import top.cmarco.temperatureplugin.api.WorldSeasonChangeEvent;
import top.cmarco.temperatureplugin.config.StandardConfig;
import top.cmarco.temperatureplugin.season.Season;

public final class WorldManager {
   private final TemperaturePlugin plugin;
   private final HashSet<World> worlds = new HashSet(3);
   private final HashMap<UUID, Season> worldsSeasons = new HashMap(3);
   private long lastChecked = 0L;

   public WorldManager(@NotNull TemperaturePlugin plugin) {
      this.plugin = plugin;
      StandardConfig stdConf = plugin.getStandardConfig();
      Server server = plugin.getServer();
      Iterator var4 = stdConf.getWorlds().iterator();

      while(var4.hasNext()) {
         String worldName = (String)var4.next();
         World world = server.getWorld(worldName);
         if (world == null) {
            plugin.getLogger().warning("config.yml lists unknown world with name: `" + worldName + "`.");
         } else {
            this.worlds.add(world);
         }
      }

   }

   public Set<World> getWorlds() {
      return this.worlds;
   }

   @NotNull
   public Season getWorldSeason(@NotNull World world) {
      return (Season)this.worldsSeasons.get(world.getUID());
   }

   public void setWorldSeason(@NotNull World world) {
      Season previous = this.getWorldSeason(world);
      Season $new = Season.getCurrentSeason(world);
      if (previous != null && previous != $new) {
         WorldSeasonChangeEvent event = new WorldSeasonChangeEvent(world, previous, $new);
         this.plugin.getServer().getPluginManager().callEvent(event);
         if (event.isCancelled()) {
            return;
         }

         $new = event.getUpcomingSeason();
      }

      this.worldsSeasons.put(world.getUID(), $new);
      this.lastChecked = System.currentTimeMillis();
   }

   public TemperaturePlugin getPlugin() {
      return this.plugin;
   }
}
