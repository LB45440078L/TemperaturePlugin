package top.cmarco.temperatureplugin.season;

import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

public enum Season {
   WINTER("&bWinter"),
   SPRING("&6Spring"),
   SUMMER("&eSummer"),
   AUTUMN("&3Autumn");

   private final String name;

   private Season(@NotNull final String param3) {
      this.name = param3;
   }

   @NotNull
   public String getName() {
      return this.name;
   }

   @NotNull
   public static Season getCurrentSeason(long totalTicks) {
      int monthsElapsed = (int)(totalTicks / 744000L);
      Season var10000;
      switch(monthsElapsed & 3) {
      case 0:
         var10000 = SUMMER;
         break;
      case 1:
         var10000 = AUTUMN;
         break;
      case 2:
         var10000 = WINTER;
         break;
      case 3:
         var10000 = SPRING;
         break;
      default:
         throw new IllegalStateException("Invalid season calculation");
      }

      return var10000;
   }

   @NotNull
   public static Season getCurrentSeason(@NotNull World world) {
      return getCurrentSeason(world.getGameTime());
   }

   // $FF: synthetic method
   private static Season[] $values() {
      return new Season[]{WINTER, SPRING, SUMMER, AUTUMN};
   }
}
