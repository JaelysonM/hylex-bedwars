package com.uzm.hylex.bedwars.listeners.player;

import com.google.common.collect.Lists;
import com.uzm.hylex.bedwars.arena.Arena;
import com.uzm.hylex.bedwars.arena.improvements.UpgradeType;
import com.uzm.hylex.bedwars.arena.player.ArenaPlayer;
import com.uzm.hylex.core.api.HylexPlayer;
import com.uzm.hylex.core.utils.BukkitUtils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

import static com.uzm.hylex.core.api.interfaces.Enums.ArenaState.IN_GAME;

public class PlayerPickupListener implements Listener {

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
            if (player.getInventory().containsAtLeast(new ItemStack(Material.WOOD_SWORD), 1)) {
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
