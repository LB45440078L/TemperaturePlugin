package top.cmarco.temperatureplugin.utilities;

import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

public final class ChatUtils {
   @NotNull
   public static String colorStd(@NotNull String text) {
      return ChatColor.translateAlternateColorCodes('&', text);
   }
}
