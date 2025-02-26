package top.cmarco.temperatureplugin.api;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;
import top.cmarco.temperatureplugin.temperature.Temperature;

public class PlayerTemperatureUpdateEvent extends PlayerEvent implements Cancellable {
   private static final HandlerList HANDLERS = new HandlerList();
   private final Temperature playerTemperatureUnit;
   private double temperatureAsUnit;
   private boolean cancelled = false;

   public PlayerTemperatureUpdateEvent(@NotNull Player who, @NotNull Temperature playerTemperatureUnit, double temperatureAsUnit) {
      super(who);
      this.playerTemperatureUnit = playerTemperatureUnit;
      this.temperatureAsUnit = temperatureAsUnit;
   }

   public static HandlerList getHandlerList() {
      return HANDLERS;
   }

   @NotNull
   public HandlerList getHandlers() {
      return HANDLERS;
   }

   public double getTemperatureAsUnit() {
      return this.temperatureAsUnit;
   }

   @NotNull
   public Temperature getPlayerTemperatureUnit() {
      return this.playerTemperatureUnit;
   }

   public void setTemperatureAsUnit(double temperatureAsUnit) {
      this.temperatureAsUnit = temperatureAsUnit;
   }

   public boolean isCancelled() {
      return this.cancelled;
   }

   public void setCancelled(boolean cancel) {
      this.cancelled = cancel;
   }
}
