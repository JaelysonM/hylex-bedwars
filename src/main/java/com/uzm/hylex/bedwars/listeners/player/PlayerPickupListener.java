package com.uzm.hylex.bedwars.listeners.player;

import com.google.common.collect.Lists;
import com.uzm.hylex.bedwars.arena.Arena;
import com.uzm.hylex.bedwars.arena.improvements.UpgradeType;
import com.uzm.hylex.bedwars.arena.player.ArenaPlayer;
import com.uzm.hylex.core.api.HylexPlayer;
import com.uzm.hylex.core.spigot.items.ItemBuilder;
import com.uzm.hylex.core.spigot.utils.BukkitUtils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.uzm.hylex.core.api.interfaces.Enums.ArenaState.IN_GAME;



public class PlayerPickupListener implements Listener {
  private static final Map<String, Long> PROTECTION_PICKUP = new HashMap<>();


  @EventHandler(priority = EventPriority.MONITOR)
  public void onSplitDropMonitor(PlayerPickupItemEvent evt) {
    Player player = evt.getPlayer();
    Item i = evt.getItem();
    if (i.getItemStack().getType() == Material.GOLD_INGOT || i.getItemStack().getType() == Material.IRON_INGOT || i.getItemStack().getType() == Material.EMERALD) {
      int nearby = evt.getPlayer().getNearbyEntities(1, 1, 1).stream().filter(e -> e.getType() == EntityType.PLAYER).collect(Collectors.toList()).size();
      if (nearby != 0) {
        long last = PROTECTION_PICKUP.getOrDefault(player.getName().toLowerCase(), 0L);
        if (last > System.currentTimeMillis()) {
          PROTECTION_PICKUP.remove(player.getName().toLowerCase());
          return;
        }
        evt.setCancelled(true);
        PROTECTION_PICKUP.put(player.getName().toLowerCase(), System.currentTimeMillis() + 10);
        evt.getItem().setItemStack(new ItemBuilder(i.getItemStack().clone()).amount(i.getItemStack().getAmount() / (nearby)).build());

      }
    }
  }



  @EventHandler
  public void onPlayerPickup(PlayerPickupItemEvent evt) {
    Player player = evt.getPlayer();
    HylexPlayer hp = HylexPlayer.getByPlayer(player);
    if (hp != null) {
      if (hp.getArenaPlayer() != null) {
        ArenaPlayer ap = (ArenaPlayer) hp.getArenaPlayer();
        Arena arena = ap.getArena();
        if (arena.getState() != IN_GAME || ap.getCurrentState() != ArenaPlayer.CurrentState.IN_GAME) {
          evt.setCancelled(true);
        } else {
          ItemStack item = evt.getItem().getItemStack();
          Material material = item.getType();
          int amount = material == Material.IRON_INGOT ? (64 * 10) : material == Material.GOLD_INGOT ? (64 * 4) : 0;
          if (amount > 0) {
            if (BukkitUtils.getCountFromMaterial(player.getInventory(), material) >= amount) {
              evt.setCancelled(true);
              return;
            }
          }

          if (item.getType() == Material.RED_ROSE) {
            evt.setCancelled(true);
          } else if (item.getType().name().contains("_SWORD")) {
            if (player.getInventory().contains(Material.WOOD_SWORD)) {
              player.getInventory().removeItem(new ItemStack(Material.WOOD_SWORD));
            }
          }

          if (!evt.isCancelled()) {
            if (ap.getTeam() != null) {
              if (ap.getTeam().hasUpgrade(UpgradeType.SHARPENED_SWORDS)) {
                Lists.newArrayList(player.getInventory().getContents()).forEach(i -> {
                  if (i != null && i.getType().name().contains("SWORD")) {
                    i.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, ap.getTeam().getTier(UpgradeType.SHARPENED_SWORDS));
                  }
                });
                player.updateInventory();
              }
            }
          }
        }
      }
    }
  }
}
