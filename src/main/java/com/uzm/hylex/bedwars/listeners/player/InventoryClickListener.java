package com.uzm.hylex.bedwars.listeners.player;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.uzm.hylex.bedwars.arena.Arena;
import com.uzm.hylex.bedwars.arena.player.ArenaPlayer;
import com.uzm.hylex.bedwars.utils.PlayerUtils;
import com.uzm.hylex.core.api.HylexPlayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.uzm.hylex.core.api.interfaces.Enums.ArenaState.IN_GAME;

public class InventoryClickListener implements Listener {


  @EventHandler
  public void onInventoryClick(InventoryClickEvent evt) {
    if (evt.getWhoClicked() instanceof Player) {

      ItemStack item = evt.getCurrentItem();
      Player player = (Player) evt.getWhoClicked();

      HylexPlayer hp = HylexPlayer.getByPlayer(player);
      if (hp != null) {
        ArenaPlayer ap = (ArenaPlayer) hp.getArenaPlayer();
        if (ap != null) {
          Arena arena = ap.getArena();
          if (arena != null) {
            if (arena.getState() != IN_GAME || ap.getCurrentState() != ArenaPlayer.CurrentState.IN_GAME) {
              evt.setCancelled(true);
            } else if (arena.getState() == IN_GAME) {
              if (evt.getSlotType().equals(InventoryType.SlotType.OUTSIDE))
                return;
              else if (evt.getInventory().getType().equals(InventoryType.CHEST)) {
                if (evt.getCurrentItem().getType() == Material.WOOD_SWORD || evt.getCurrentItem().getType().toString().contains("AXE")) {
                  evt.setCancelled(true);
                }
              }
              Material material = item != null ? item.getType() : Material.AIR;

              boolean cantDrop =
                material.name().contains("WOOD_SWORD") || material.name().contains("BOW") || material.name().contains("_PICKAXE") || material.name().contains("_AXE") || material
                  .name().contains("SHEARS") || material.name().contains("COMPASS");
              if (material.name().contains("_CHESTPLATE") || material.name().contains("_BOOTS") || material.name().contains("_LEGGINGS") || material.name().contains("_HELMET")) {
                evt.setCancelled(true);
              } else {
                if (player.getOpenInventory().getTopInventory().getType().name().contains("CHEST") || (evt.getClickedInventory() != null && evt.getClickedInventory().getType()
                  .name().contains("CHEST"))) {
                  evt.setCancelled(cantDrop);
                  if (evt.getHotbarButton() != -1) {
                    material = Material.AIR;
                    ItemStack itemMoved = player.getInventory().getItem(evt.getHotbarButton());
                    if (itemMoved != null) {
                      material = itemMoved.getType();
                    }

                    evt.setCancelled(
                      material.name().contains("_SWORD") || material.name().contains("BOW") || material.name().contains("_PICKAXE") || material.name().contains("_AXE") || material
                        .name().contains("SHEARS") || material.name().contains("COMPASS") || material.name().contains("_CHESTPLATE") || material.name()
                        .contains("_BOOTS") || material.name().contains("_LEGGINGS") || material.name().contains("_LEGGINGS"));
                  }
                } else if (evt.getClickedInventory() != null && (evt.getClickedInventory().getType().name().contains("CRAFTING") || evt.getClickedInventory().getType().name()
                  .contains("WORKBENCH"))) {
                  evt.setCancelled(true);
                }
              }
            }
          }
        }
      }
    }
  }

/*
  @EventHandler
  public void onSwordMove(InventoryClickEvent evt) {
    Inventory top = evt.getView().getTopInventory();
    Inventory bottom = evt.getView().getBottomInventory();
    Player player = (Player) evt.getWhoClicked();


    if (evt.getAction() == InventoryAction.PICKUP_ALL) {
      if (evt.getCurrentItem() != null && evt.getCurrentItem().getTypeId() != 0) {
        if (evt.getCurrentItem().getType().name().contains("SWORD")) {
          if (evt.getCurrentItem().getType() != Material.WOOD_SWORD) {
            List<ItemStack> items = new ArrayList<ItemStack>(Lists.newArrayList(player.getInventory().getContents()));
            if (!evt.getClickedInventory().getType().name().contains("CHEST")) {
              items.remove(evt.getCurrentItem());
            }
            if (!PlayerUtils.containsSword(items) && evt.getCurrentItem().getType().name().contains("SWORD")) {
              player.sendMessage(items.toString());
              if (!player.getInventory().contains(new ItemStack(Material.WOOD_SWORD))) {
                player.getInventory().addItem(new ItemStack(Material.WOOD_SWORD));
              }

            }
          }

        }

      }

    } else if (evt.getAction() == InventoryAction.PLACE_ALL) {
      if (evt.getCurrentItem() != null && evt.getCurrentItem().getTypeId() != 0) {
        if (evt.getCurrentItem().getType().name().contains("SWORD")) {
          if (evt.getCurrentItem().getType() != Material.WOOD_SWORD) {
            List<ItemStack> items = new ArrayList<ItemStack>(Lists.newArrayList(player.getInventory().getContents()));
            player.sendMessage(items.toString());
            if (!PlayerUtils.containsSword(items) && evt.getCurrentItem().getType().name().contains("SWORD")) {

              if (player.getInventory().contains(new ItemStack(Material.WOOD_SWORD))) {
                player.getInventory().removeItem(new ItemStack(Material.WOOD_SWORD));

              }



            }
          }

        }

      }


    } else if (evt.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
      if (top.getType().name().contains("CHEST") && bottom.getType() == InventoryType.PLAYER) {
        if (evt.getCurrentItem() != null && evt.getCurrentItem().getTypeId() != 0) {
          if (evt.getCurrentItem().getType() != Material.WOOD_SWORD) {
            List<ItemStack> items = new ArrayList<ItemStack>(Lists.newArrayList(player.getInventory().getContents()));
            if (!evt.getClickedInventory().getType().name().contains("CHEST")) {
              items.remove(evt.getCurrentItem());
            }

            if (!PlayerUtils.containsSword(items) && evt.getCurrentItem().getType().name().contains("SWORD")) {
              player.sendMessage(items.toString());
              if (!player.getInventory().contains(new ItemStack(Material.WOOD_SWORD))) {
                player.getInventory().addItem(new ItemStack(Material.WOOD_SWORD));
              } else {
                player.getInventory().removeItem(new ItemStack(Material.WOOD_SWORD));
              }



            }
          }

        }

      }
    }

  }
*/

  @EventHandler
  public void onInventoryDrag(InventoryMoveItemEvent evt) {
    if (!(evt.getSource().getHolder() instanceof Player))
      return;
    Player player = (Player) evt.getSource().getHolder();
    HylexPlayer hp = HylexPlayer.getByPlayer(player);
    if (hp != null && hp.getArenaPlayer() != null && hp.getArenaPlayer().getArena() != null) {
      if (evt.getDestination().getType() != InventoryType.PLAYER) {
        if (evt.getItem().getType() == Material.WOOD_SWORD) {
          evt.setCancelled(true);
        }

      }
    }
  }

}
