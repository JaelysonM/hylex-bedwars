package com.uzm.hylex.bedwars.listeners.server;

import com.uzm.hylex.bedwars.arena.Arena;
import com.uzm.hylex.bedwars.arena.player.ArenaPlayer;
import com.uzm.hylex.bedwars.controllers.ArenaController;
import com.uzm.hylex.bedwars.controllers.HylexPlayer;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
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

import static com.uzm.hylex.bedwars.arena.enums.ArenaEnums.ArenaState.IN_GAME;

public class ServerListener implements Listener {

  @EventHandler
  public void onBlockIgnite(BlockIgniteEvent evt) {
    if (evt.getIgnitingEntity() != null) {
      Arena arena = ArenaController.getArena(evt.getIgnitingEntity().getWorld().getName());
      if (arena == null || arena.getState() != IN_GAME || !arena.getBlocks().isPlacedBlock(evt.getBlock())) {
        evt.setCancelled(true);
      }
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
        /*
          Aqui tu já checa se o item está coberto por vidro então se ele tiver todas essas condições ele vai remover
          esse bloco de da lista  de itens a serem explodidos
         */
        if (!arena.getBlocks().isPlacedBlock(block)) {
          evt.blockList().remove(block);
        }
        for (BlockFace blockface : new BlockFace[] {BlockFace.UP, BlockFace.SOUTH, BlockFace.WEST, BlockFace.EAST, BlockFace.NORTH}) {
          if (block.getRelative(blockface).getType().name().contains("GLASS")) {
            evt.blockList().remove(block);
          }
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

  @EventHandler
  public void onItemMerge(ItemMergeEvent evt) {
    if ((evt.getEntity() != null)) {
      Item i = evt.getEntity();
      if (i.getItemStack().getType() == Material.GOLD_INGOT || i.getItemStack().getType() == Material.IRON_INGOT)
        evt.setCancelled(true);

    }
  }

  @EventHandler
  public void onCraftItem(PrepareItemCraftEvent evt) {
    for (HumanEntity h : evt.getViewers()) {
      if (h instanceof Player) {
        Player player = (Player) h;
        HylexPlayer hp = HylexPlayer.get(player);

        if (hp != null) {
          ArenaPlayer ap = hp.getArenaPlayer();
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
