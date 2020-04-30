package com.uzm.hylex.bedwars.listeners.server;

import com.uzm.hylex.bedwars.arena.Arena;
import com.uzm.hylex.bedwars.controllers.ArenaController;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

import java.util.ArrayList;

import static com.uzm.hylex.bedwars.arena.enums.ArenaEnums.ArenaState.IN_GAME;

public class ServerListener implements Listener {

  @EventHandler
  public void onBlockIgnite(BlockIgniteEvent evt) {
    Arena arena = ArenaController.getArena(evt.getPlayer().getWorld().getName());
    if (arena == null || arena.getState() != IN_GAME || !arena.getBlocks().isPlacedBlock(evt.getBlock())) {
      evt.setCancelled(true);
    }
  }

  @EventHandler
  public void onBlockBurn(BlockBurnEvent evt) {
    Arena arena = ArenaController.getArena(evt.getBlock().getWorld().getName());
    if (arena == null || arena.getState() != IN_GAME || !arena.getBlocks().isPlacedBlock(evt.getBlock())) {
      evt.setCancelled(true);
    }
  }

  @EventHandler
  public void onBlockExplode(BlockExplodeEvent evt) {
    Arena arena = ArenaController.getArena(evt.getBlock().getWorld().getName());
    if (arena == null || arena.getState() != IN_GAME) {
      evt.setCancelled(true);
    } else {
      for (Block block : new ArrayList<>(evt.blockList())) {
        int protectionBlock = 0;
        for (BlockFace blockface : new BlockFace[] {BlockFace.UP, BlockFace.SOUTH, BlockFace.WEST, BlockFace.EAST, BlockFace.NORTH}) {
          if (block.getRelative(blockface).getType().name().contains("GLASS")) {
            protectionBlock++;
          }
        }

        if (!arena.getBlocks().isPlacedBlock(block) || block.getType().name().contains("GLASS") || protectionBlock > 1) {
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
  public void onEntityExplode(EntityExplodeEvent evt) {
    Arena arena = ArenaController.getArena(evt.getEntity().getWorld().getName());
    if (arena == null || arena.getState() != IN_GAME) {
      evt.setCancelled(true);
    } else {
      for (Block block : new ArrayList<>(evt.blockList())) {
        int protectionBlock = 0;
        for (BlockFace blockface : new BlockFace[] {BlockFace.UP, BlockFace.SOUTH, BlockFace.WEST, BlockFace.EAST, BlockFace.NORTH}) {
          if (block.getRelative(blockface).getType().name().contains("GLASS")) {
            protectionBlock++;
          }
        }

        if (!arena.getBlocks().isPlacedBlock(block) || block.getType().name().contains("GLASS") || protectionBlock > 1) {
          evt.blockList().remove(block);
        }
      }
    }
  }

  @EventHandler
  public void onWeatherChange(WeatherChangeEvent evt) {
    evt.setCancelled(evt.toWeatherState());
  }
}
