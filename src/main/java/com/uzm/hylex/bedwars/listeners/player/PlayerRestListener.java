package com.uzm.hylex.bedwars.listeners.player;

import com.uzm.hylex.bedwars.Core;
import com.uzm.hylex.bedwars.arena.Arena;
import com.uzm.hylex.bedwars.arena.player.ArenaPlayer;
import com.uzm.hylex.bedwars.arena.team.Team;
import com.uzm.hylex.bedwars.controllers.HylexPlayer;
import com.uzm.hylex.bedwars.utils.BukkitUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import static com.uzm.hylex.bedwars.arena.enums.ArenaEnums.ArenaState.IN_GAME;

public class PlayerRestListener implements Listener {

  @EventHandler
  public void onPlayerItemConsume(PlayerItemConsumeEvent evt) {
    Player player = evt.getPlayer();
    HylexPlayer hp = HylexPlayer.get(player);
    if (hp != null) {
      ArenaPlayer ap = hp.getArenaPlayer();
      if (ap != null) {
        Arena arena = ap.getArena();
        if (arena != null && evt.getItem().getType().name().contains("POTION")) {
          Bukkit.getScheduler().runTaskLaterAsynchronously(Core.getInstance(), () -> BukkitUtils.removeItem(player.getInventory(), Material.matchMaterial("GLASS_BOTTLE"), 1), 1);
        }
      }
    }
  }

  @EventHandler
  public void onPlayerDropItem(PlayerDropItemEvent evt) {
    HylexPlayer hp = HylexPlayer.get(evt.getPlayer());
    if (hp != null) {
      ArenaPlayer ap = hp.getArenaPlayer();
      if (ap != null) {
        Arena arena = ap.getArena();
        if (arena != null) {
          if (arena.getState() != IN_GAME || ap.getCurrentState() != ArenaPlayer.CurrentState.IN_GAME) {
            evt.setCancelled(true);
          } else {
            ItemStack item = evt.getItemDrop().getItemStack();
            if (item.getType().name().contains("WOOD_SWORD") || item.getType().name().contains("_HELMET") || item.getType().name().contains("_CHESTPLATE") || item.getType().name()
              .contains("_LEGGINGS") || item.getType().name().contains("_BOOTS") || item.getType().name().contains("BOW") || item.getType().name().contains("_PICKAXE") || item
              .getType().name().contains("_AXE") || item.getType().name().contains("SHEARS") || item.getType().name().contains("COMPASS")) {
              evt.setCancelled(true);
            }
          }
        }
      }
    }
  }

  @EventHandler
  public void onPlayerPickupItem(PlayerPickupItemEvent evt) {
    HylexPlayer hp = HylexPlayer.get(evt.getPlayer());
    if (hp != null) {
      ArenaPlayer ap = hp.getArenaPlayer();
      if (ap != null) {
        Arena arena = ap.getArena();
        if (arena != null) {
          if (arena.getState() != IN_GAME || ap.getCurrentState() != ArenaPlayer.CurrentState.IN_GAME) {
            evt.setCancelled(true);
          }
        }
      }
    }
  }

  @EventHandler
  public void onBlockBreak(BlockBreakEvent evt) {
    Player player = evt.getPlayer();
    HylexPlayer hp = HylexPlayer.get(player);
    if (hp != null) {
      ArenaPlayer ap = hp.getArenaPlayer();
      if (ap != null) {
        Arena arena = ap.getArena();
        if (arena != null) {
          if (arena.getState() != IN_GAME || ap.getCurrentState() != ArenaPlayer.CurrentState.IN_GAME) {
            evt.setCancelled(true);
          } else {
            Block block = evt.getBlock();
            if (block.getType().name().contains("BED_BLOCK")) {
              for (Team team : arena.getTeams().values()) {
                if (team.isBed(block)) {
                  if (!team.equals(ap.getTeam())) {
                    // TODO: quebrar cama inimiga
                    // arena.destroyBed(team, ap);
                    return;
                  }

                  evt.setCancelled(true);
                  player.sendMessage("§cVocê não pode destruir a sua própria cama.");
                }
              }
            } else if (!arena.getBorders().contains(evt.getBlock().getLocation())) {
              evt.setCancelled(true);
            } else if (!arena.getBlocks().isPlacedBlock(block)) {
              evt.setCancelled(true);
            }
          }
        }
      }
    }
  }

  @EventHandler
  public void onBlockPlace(BlockPlaceEvent evt) {
    HylexPlayer hp = HylexPlayer.get(evt.getPlayer());
    if (hp != null) {
      ArenaPlayer ap = hp.getArenaPlayer();
      if (ap != null) {
        Arena arena = ap.getArena();
        if (arena != null) {
          if (arena.getState() != IN_GAME || ap.getCurrentState() != ArenaPlayer.CurrentState.IN_GAME) {
            evt.setCancelled(true);
          } else if (!arena.getBorders().contains(evt.getBlock().getLocation())) {
            evt.setCancelled(true);
          } else {
            Block block = evt.getBlock();

            // TODO: Bloquear se a zona tiver protegida
            //evt.setCancelled(nearZone);
            if (evt.isCancelled()) {
              return;
            }

            if (block.getType().name().contains("TNT")) {
              block.setType(Material.AIR);
              block.getWorld().spawn(block.getLocation(), TNTPrimed.class).setFuseTicks(60);
              return;
            }

            arena.getBlocks().addBlock(block);
          }
        }
      }
    }
  }
}
