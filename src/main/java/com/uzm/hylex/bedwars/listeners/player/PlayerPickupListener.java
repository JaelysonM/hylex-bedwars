package com.uzm.hylex.bedwars.listeners.player;

import com.google.common.collect.Lists;
import com.uzm.hylex.bedwars.arena.Arena;
import com.uzm.hylex.bedwars.arena.improvements.UpgradeType;
import com.uzm.hylex.bedwars.arena.player.ArenaPlayer;
import com.uzm.hylex.bedwars.controllers.HylexPlayer;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

import static com.uzm.hylex.bedwars.arena.enums.ArenaEnums.ArenaState.IN_GAME;

public class PlayerPickupListener implements Listener {
  @EventHandler
  public void onPlayerPickup(PlayerPickupItemEvent evt) {
    Player player = evt.getPlayer();
    HylexPlayer hp = HylexPlayer.get(player);
    ArenaPlayer ap = hp.getArenaPlayer();


    if (hp.getArenaPlayer() != null) {

      //TODO Máximo de minérios por inventário de um jogador Ex: Max de Iron: 10x64; Max de ouro: 2x64

      Arena arena = ap.getArena();
      if (arena.getState() != IN_GAME || ap.getCurrentState() != ArenaPlayer.CurrentState.IN_GAME)
        evt.setCancelled(true);
      ItemStack item = evt.getItem().getItemStack();
      if (ap.getTeam() != null) {
        if (ap.getTeam().hasUpgrade(UpgradeType.SHARPENED_SWORDS)) {
          Lists.newArrayList(player.getInventory().getContents()).forEach(i -> {
            if (i != null && i.getType().name().contains("SWORD") || Objects.requireNonNull(i).getType().toString().contains("_AXE"))
              i.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, ap.getTeam().getTier(UpgradeType.SHARPENED_SWORDS));
          });
        }
      }
      if (item.getType() == Material.RED_ROSE) {
        evt.setCancelled(true);
      } else if (item.getType().name().contains("_SWORD")) {
        if (player.getInventory().containsAtLeast(new ItemStack(Material.WOOD_SWORD), 1))
          player.getInventory().removeItem(new ItemStack(Material.WOOD_SWORD));
      }
    }
  }

}
