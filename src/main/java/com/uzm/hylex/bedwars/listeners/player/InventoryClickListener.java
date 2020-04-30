package com.uzm.hylex.bedwars.listeners.player;

import com.uzm.hylex.bedwars.arena.Arena;
import com.uzm.hylex.bedwars.arena.player.ArenaPlayer;
import com.uzm.hylex.bedwars.controllers.HylexPlayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import static com.uzm.hylex.bedwars.arena.enums.ArenaEnums.ArenaState.IN_GAME;

public class InventoryClickListener implements Listener {
 @EventHandler
  public void onInventoryClick(InventoryClickEvent evt) {
    if (evt.getWhoClicked() instanceof Player) {

       ItemStack item = evt.getCurrentItem();
      Player player = (Player) evt.getWhoClicked();

      HylexPlayer hp = HylexPlayer.get(player);
      if (hp != null) {
        ArenaPlayer ap = hp.getArenaPlayer();
        if (ap != null) {
          Arena arena = ap.getArena();
          if (arena != null) {
            if (arena.getState() != IN_GAME || ap.getCurrentState() != ArenaPlayer.CurrentState.IN_GAME) {
              evt.setCancelled(true);
              // TODO: Click nos items do inventário
            } else if (arena.getState() == IN_GAME) {
              Material material = Material.AIR;
              if (item != null) {
                material = item.getType();
              }

              boolean cantDrop = material.name().contains("WOOD_SWORD") || material.name().contains("BOW") || material.name().contains("_PICKAXE") || material.name().contains("_AXE")
                      || material.name().contains("SHEARS") || material.name().contains("COMPASS");
              if (material.name().contains("_CHESTPLATE") || material.name().contains("_BOOTS") || material.name().contains("_LEGGINGS") || material.name().contains("_HELMET")) {
                evt.setCancelled(true);
              } else {
                if (player.getOpenInventory().getTopInventory().getType().name().contains("CHEST")
                        || (evt.getClickedInventory() != null && evt.getClickedInventory().getType().name().contains("CHEST"))) {
                  evt.setCancelled(cantDrop);
                  if (evt.getHotbarButton() != -1) {
                    material = Material.AIR;
                    ItemStack itemMoved = player.getInventory().getItem(evt.getHotbarButton());
                    if (itemMoved != null) {
                      material = itemMoved.getType();
                    }

                    evt.setCancelled(material.name().contains("_SWORD") || material.name().contains("BOW") || material.name().contains("_PICKAXE") || material.name().contains("_AXE")
                            || material.name().contains("SHEARS") || material.name().contains("COMPASS") || material.name().contains("_CHESTPLATE") || material.name().contains("_BOOTS")
                            || material.name().contains("_LEGGINGS") || material.name().contains("_LEGGINGS"));
                  }
                } else if (evt.getClickedInventory() != null
                        && (evt.getClickedInventory().getType().name().contains("CRAFTING") || evt.getClickedInventory().getType().name().contains("WORKBENCH"))) {
                  evt.setCancelled(true);
                }
              }
            }
          }
        }
      }
        }
      }
}
