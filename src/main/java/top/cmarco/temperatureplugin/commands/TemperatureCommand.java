package top.cmarco.temperatureplugin.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import top.cmarco.temperatureplugin.TemperaturePlugin;
import top.cmarco.temperatureplugin.manager.NamespaceManager;
import top.cmarco.temperatureplugin.utilities.ChatUtils;

public final class TemperatureCommand implements CommandExecutor {
   private final TemperaturePlugin plugin;

   public TemperatureCommand(@NotNull TemperaturePlugin plugin) {
      this.plugin = plugin;
   }

   private static void help(@NotNull Player player) {
      player.sendMessage(ChatUtils.colorStd("&7[&cTemperature&7]&f: Help Page"));
      player.sendMessage(ChatUtils.colorStd("&7/&etemperature help"));
      player.sendMessage(ChatUtils.colorStd("&7/&etemperature toggle"));
      player.sendMessage(ChatUtils.colorStd("&7/&etemperature reload"));
   }

   private static void noPerm(@NotNull Player player, @NotNull String perm) {
      player.sendMessage(ChatUtils.colorStd("&7[&cTemperature&7]&f: Missing Permission &c" + perm));
   }

   public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
      if (sender instanceof Player) {
         Player player = (Player)sender;
         if (args.length == 0) {
            help(player);
            return true;
         } else {
            if (args.length == 1) {
               if (args[0].equalsIgnoreCase("help")) {
                  help(player);
                  return true;
               }

               if (args[0].equalsIgnoreCase("toggle")) {
                  if (!player.hasPermission("temperature.toggle")) {
                     noPerm(player, "temperature.toggle");
                     return true;
                  }

                  NamespaceManager namespaceManager = this.plugin.getNamespaceManager();
                  boolean isEnabled = namespaceManager.isEnableTemperature(player);
                  if (isEnabled) {
                     player.sendMessage(ChatUtils.colorStd("&7[&cTemperature&7]&f: &cDisabled Temperature"));
                     namespaceManager.disableTemperature(player);
                  } else {
                     player.sendMessage(ChatUtils.colorStd("&7[&cTemperature&7]&f: &aEnabled Temperature"));
                     namespaceManager.enableTemperature(player);
                  }

                  return true;
               }

               if (args[0].equalsIgnoreCase("reload")) {
                  if (!player.hasPermission("temperature.reload")) {
                     noPerm(player, "temperature.reload");
                     return true;
                  }

                  long ms = System.currentTimeMillis();
                  this.plugin.reload();
                  player.sendMessage(ChatUtils.colorStd("&7[&cTemperature&7]&f: &aReloaded in " + (System.currentTimeMillis() - ms) + "ms"));
               } else {
                  player.sendMessage(ChatUtils.colorStd("&cInvalid command syntax."));
               }
            } else {
               player.sendMessage(ChatUtils.colorStd("&cInvalid command syntax."));
            }

            return true;
         }
      } else {
         sender.sendMessage(ChatUtils.colorStd("&cThis command cannot be used from console."));
         return true;
      }
   }
}
