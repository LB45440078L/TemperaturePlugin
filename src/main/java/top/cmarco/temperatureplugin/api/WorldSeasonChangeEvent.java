package top.cmarco.temperatureplugin.api;

import org.bukkit.World;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.world.WorldEvent;
import org.jetbrains.annotations.NotNull;
import top.cmarco.temperatureplugin.season.Season;

public class WorldSeasonChangeEvent extends WorldEvent implements Cancellable {
   private static final HandlerList HANDLERS = new HandlerList();
   private final Season previousSeason;
   private Season upcomingSeason;
   private boolean cancel = false;

   public WorldSeasonChangeEvent(@NotNull World world, @NotNull Season previous, @NotNull Season upcoming) {
      super(world);
      this.previousSeason = previous;
      this.upcomingSeason = upcoming;
   }

   public static HandlerList getHandlerList() {
      return HANDLERS;
   }

   @NotNull
   public Season getPreviousSeason() {
      return this.previousSeason;
   }

   @NotNull
   public Season getUpcomingSeason() {
      return this.upcomingSeason;
   }

   public void setUpcomingSeason(@NotNull Season upcomingSeason) {
      this.upcomingSeason = upcomingSeason;
   }

   public boolean isCancelled() {
      return this.cancel;
   }

   public void setCancelled(boolean cancel) {
      this.cancel = cancel;
   }

   @NotNull
   public HandlerList getHandlers() {
      return HANDLERS;
   }
}
