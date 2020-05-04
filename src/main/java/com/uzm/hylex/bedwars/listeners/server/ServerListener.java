package com.uzm.hylex.bedwars.listeners.server;

import com.uzm.hylex.bedwars.arena.Arena;
import com.uzm.hylex.bedwars.arena.player.ArenaPlayer;
import com.uzm.hylex.bedwars.controllers.ArenaController;
import com.uzm.hylex.core.api.HylexPlayer;
import com.uzm.hylex.core.api.interfaces.Enums;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class ServerListener implements Listener {

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onBlockPhysics(BlockPhysicsEvent evt) {
    if (evt.getBlock().getType() == Material.GRASS || evt.getBlock().getType() == Material.BED_BLOCK) {
      evt.setCancelled(true);
    }
  }

  @EventHandler
  public void onBlockFromTo(BlockFromToEvent evt) {
    if (evt.getBlock().getType() == Material.DRAGON_EGG) {
      evt.setCancelled(true);
    }
  }

  @EventHandler
  public void onBlockIgnite(BlockIgniteEvent evt) {
    if (evt.getIgnitingEntity() != null) {
      Arena arena = ArenaController.getArena(evt.getIgnitingEntity().getWorld().getName());
      if (arena == null || arena.getState() != Enums.ArenaState.IN_GAME || !arena.getBlocks().isPlacedBlock(evt.getBlock())) {
        evt.setCancelled(true);
      }
    }
  }

  @EventHandler
  public void onBlockBurn(BlockBurnEvent evt) {
    Arena arena = ArenaController.getArena(evt.getBlock().getWorld().getName());
    if (arena == null || arena.getState() != Enums.ArenaState.IN_GAME || !arena.getBlocks().isPlacedBlock(evt.getBlock())) {
      evt.setCancelled(true);
    }
  }

  @EventHandler
  public void onBlockExplode(BlockExplodeEvent evt) {
    Arena arena = ArenaController.getArena(evt.getBlock().getWorld().getName());
    if (arena == null || arena.getState() != Enums.ArenaState.IN_GAME) {
      evt.setCancelled(true);
    } else {
      for (Block block : new ArrayList<>(evt.blockList())) {
        int protectionBlock = 0;
        for (BlockFace blockface : new BlockFace[] {BlockFace.UP, BlockFace.SOUTH, BlockFace.WEST, BlockFace.EAST, BlockFace.NORTH}) {
          if (block.getRelative(blockface).getType().name().contains("GLASS")) {
            protectionBlock++;
          }
        }

        if (!arena.getBlocks().isPlacedBlock(block) || block.getType().name().contains("GLASS") || protectionBlock > 2) {
          evt.blockList().remove(block);
        }
      }
    }
  }

  @EventHandler
  public void onEntityExplode(EntityExplodeEvent evt) {
    Arena arena = ArenaController.getArena(evt.getEntity().getWorld().getName());
    if (arena == null || arena.getState() != Enums.ArenaState.IN_GAME) {
      evt.setCancelled(true);
    } else {
      for (Block block : new ArrayList<>(evt.blockList())) {
        int protectionBlock = 0;
        for (BlockFace blockface : new BlockFace[] {BlockFace.UP, BlockFace.SOUTH, BlockFace.WEST, BlockFace.EAST, BlockFace.NORTH}) {
          if (block.getRelative(blockface).getType().name().contains("GLASS")) {
            protectionBlock++;
          }
        }

        if (!arena.getBlocks().isPlacedBlock(block) || block.getType().name().contains("GLASS") || protectionBlock > 2) {
          evt.blockList().remove(block);
        }
      }
    }
  }

  @EventHandler
  public void onLeavesDecay(LeavesDecayEvent evt) {
    evt.setCancelled(true);
  }

  @EventHandler
  public void onWeatherChange(WeatherChangeEvent evt) {
    evt.setCancelled(evt.toWeatherState());
  }

  @EventHandler
  public void onItemMerge(ItemMergeEvent evt) {
    if (evt.getEntity() != null) {
      Item i = evt.getEntity();
      if (i.getItemStack().getType() == Material.GOLD_INGOT || i.getItemStack().getType() == Material.IRON_INGOT) {
        evt.setCancelled(true);
      }
    }
  }

  @EventHandler
  public void onCraftItem(PrepareItemCraftEvent evt) {
    for (HumanEntity h : evt.getViewers()) {
      if (h instanceof Player) {
        Player player = (Player) h;
        HylexPlayer hp = HylexPlayer.getByPlayer(player);
        if (hp != null) {
          ArenaPlayer ap = (ArenaPlayer) hp.getArenaPlayer();
          if (ap.getArena() != null) {
            evt.getInventory().setResult(new ItemStack(Material.AIR));
          }
        }
      }
    }
  }


  @EventHandler(priority = EventPriority.HIGHEST)
  public void onFood(FoodLevelChangeEvent evt) {
    evt.setCancelled(true);
  }
}
