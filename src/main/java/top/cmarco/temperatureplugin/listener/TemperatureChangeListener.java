package top.cmarco.temperatureplugin.listener;

import java.util.HashMap;
import java.util.UUID;

import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.NBTType;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;
import top.cmarco.temperatureplugin.TemperaturePlugin;
import top.cmarco.temperatureplugin.api.PlayerTemperatureUpdateEvent;
import top.cmarco.temperatureplugin.data.nbt.NbtTemperatureModifier;
import top.cmarco.temperatureplugin.manager.NamespaceManager;
import top.cmarco.temperatureplugin.manager.PlayerTemperatureManager;
import top.cmarco.temperatureplugin.temperature.Temperature;

public class TemperatureChangeListener implements Listener {
   private final TemperaturePlugin plugin;
   private final HashMap<UUID, Long> lastFrozen = new HashMap();
   private final HashMap<UUID, Integer> lastFrozenTicks = new HashMap();
   private final HashMap<UUID, Long> lastBurn = new HashMap();
   private final HashMap<UUID, Long> lastDrinkWater = new HashMap();
   private final HashMap<UUID, NbtTemperatureModifier> nbtTemperatureModifiers = new HashMap<>();
   private static final double HOT_THRESHOLD = 40.5D;
   private static final double COLD_THRESHOLD = -0.5D;

   public TemperatureChangeListener(@NotNull TemperaturePlugin plugin) {
      this.plugin = plugin;
   }

   @EventHandler
   public void playerConsumeNBT(PlayerItemConsumeEvent event) {
      Player player = event.getPlayer();
      UUID uuid = player.getUniqueId();

      NBT.get(event.getItem(), nbt -> {
         if (!nbt.hasNBTData()) return;
         if (!nbt.hasTag("temperature_modifier_type")) return;
         if (!nbt.hasTag("temperature_modifier_amount")) return;
         if (!nbt.hasTag("temperature_modifier_time")) return;
         String modifierType = nbt.getString("temperature_modifier_type");
         int amount = Math.abs(nbt.getInteger("temperature_modifier_amount"));
         long time = Math.abs(nbt.getLong("temperature_modifier_time"));

         NbtTemperatureModifier nbtTemperatureModifier = new NbtTemperatureModifier(modifierType.equalsIgnoreCase("increase"), amount, System.currentTimeMillis() + (time*1000));

         nbtTemperatureModifiers.put(uuid, nbtTemperatureModifier);

      });
   }

   @EventHandler
   public void onPlayerDrinkWater(PlayerItemConsumeEvent event) {
      Player player = event.getPlayer();
      ItemStack item = event.getItem();

      if (item.getType() != Material.POTION) {
         return;
      }

      if (!(item.getItemMeta() instanceof PotionMeta potionMeta)) {
         return;
      }

      if (potionMeta.getBasePotionType() != PotionType.WATER) {
         return;
      }

      PlayerTemperatureManager temperatureManager = plugin.getTemperatureDisplay().getPlayerTemperatureManager();
      double temp = temperatureManager.getTemperature(player);

      lastDrinkWater.put(player.getUniqueId(), System.currentTimeMillis());

   }

   @EventHandler(
      priority = EventPriority.HIGH
   )
   public void onEvent(@NotNull PlayerTemperatureUpdateEvent event) {
      Player player = event.getPlayer();
      NamespaceManager namespaceManager = this.plugin.getNamespaceManager();
      if (namespaceManager.isEnableTemperature(player)) {
         UUID uuid = player.getUniqueId();
         Temperature playerTemperature = event.getPlayerTemperatureUnit();
         double playerTemperatureValue = event.getTemperatureAsUnit();
         double celsius = playerTemperature.convertUnitToCelsius(playerTemperatureValue);
         long now = System.currentTimeMillis();
         byte leather;
         ItemStack[] var13;
         int var14;
         int inc;
         ItemStack armorContent;
         ItemMeta itemMeta;
         if (!(celsius >= 40.5D)) {
            if (celsius <= -0.5D) {
               leather = 0;
               var13 = player.getInventory().getArmorContents();
               var14 = var13.length;

               for(inc = 0; inc < var14; ++inc) {
                  armorContent = var13[inc];
                  if (armorContent != null) {
                     itemMeta = armorContent.getItemMeta();
                     if (itemMeta instanceof LeatherArmorMeta) {
                        ++leather;
                     }
                  }
               }

               if (leather >= 3) {
                  return;
               }

               long lastFrozenValue = (Long)this.lastFrozen.getOrDefault(uuid, now);
               if (now - lastFrozenValue >= 50L || lastFrozenValue == now) {
                  inc = this.plugin.getStandardConfig().getFreezingAnimationSpeed();
                  if (this.lastFrozenTicks.containsKey(uuid)) {
                     int ticks = (Integer)this.lastFrozenTicks.get(uuid);
                     player.setFreezeTicks(ticks + inc);
                     this.lastFrozenTicks.put(uuid, ticks + inc);
                  } else {
                     int ticks = 25;
                     player.setFreezeTicks(ticks + inc);
                     this.lastFrozenTicks.put(uuid, Integer.valueOf(ticks));
                  }

                  this.lastFrozen.put(uuid, now);
               }
            } else {
               this.lastBurn.remove(uuid);
               this.lastFrozen.remove(uuid);
               this.lastFrozenTicks.remove(uuid);
            }
         } else {
            leather = 0;
            var13 = player.getInventory().getArmorContents();
            var14 = var13.length;

            for(inc = 0; inc < var14; ++inc) {
               armorContent = var13[inc];
               if (armorContent != null) {
                  itemMeta = armorContent.getItemMeta();
                  if (itemMeta != null && itemMeta.hasEnchant(Enchantment.FIRE_PROTECTION)) {
                     ++leather;
                  }
               }
            }

            boolean fireProt = player.getActivePotionEffects().stream().anyMatch((p) -> {
               return p.getType() == PotionEffectType.FIRE_RESISTANCE;
            });
            if (leather >= 3 || fireProt) {
               return;
            }

            boolean hasWaterEffect = false;
            

            long lastBurnValue = (Long) this.lastBurn.getOrDefault(uuid, now);
            if (now - lastBurnValue >= 1000L || lastBurnValue == now) {
               player.setFireTicks(20);
               this.lastBurn.put(uuid, now);
            }
         }

      }
   }

   public HashMap<UUID, Long> getLastDrinkWater() {
      return lastDrinkWater;
   }

   public HashMap<UUID, NbtTemperatureModifier> getNbtTemperatureModifiers() {
      return nbtTemperatureModifiers;
   }
}
