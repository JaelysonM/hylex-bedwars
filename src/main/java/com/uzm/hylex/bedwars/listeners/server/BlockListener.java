package com.uzm.hylex.bedwars.listeners.server;

import com.uzm.hylex.bedwars.arena.Arena;
import com.uzm.hylex.bedwars.arena.player.ArenaPlayer;
import com.uzm.hylex.bedwars.controllers.ArenaController;
import com.uzm.hylex.bedwars.controllers.HylexPlayer;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPhysicsEvent;


public class BlockListener implements Listener {

  @EventHandler
  public void onCanBuild(BlockCanBuildEvent evt) {
    if (!evt.isBuildable()) {
      for (Entity nearby : evt.getBlock().getWorld().getNearbyEntities(evt.getBlock().getLocation(), 5, 5, 5)) {
        if (nearby.getType() == EntityType.PLAYER) {
          Player player = (Player) nearby;
          HylexPlayer hp = HylexPlayer.get(player);
          if (hp != null) {
            ArenaPlayer ap = hp.getArenaPlayer();
            if (ap != null) {
              Arena arena = ap.getArena();
              if (arena != null) {
                if (ap.getCurrentState() == ArenaPlayer.CurrentState.SPECTATING) {
                  player.teleport(arena.getWaitingLocation());
                  evt.getBlock().getLocation().getBlock().setType(evt.getMaterial());
                }
              }

            }
          }
        }
      }
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onPshic(BlockPhysicsEvent e) {
    if (e.getBlock().getType() == Material.BED_BLOCK) {
      e.setCancelled(true);
    }
    if (e.getBlock().getType() == Material.GRASS) {
      e.setCancelled(true);
    }
  }

  @EventHandler
  public void onBlockUpdate(BlockFromToEvent evt) {
    if (evt.getBlock().getType() == Material.DRAGON_EGG) {
      evt.setCancelled(true);
    }else if (evt.getBlock().getType() == Material.WATER|| evt.getBlock().getType() == Material.STATIONARY_WATER ) {
      ArenaController.getArenas().values().forEach(arenas ->{
        if (evt.getBlock().getLocation().getWorld() == arenas.getWaitingLocation().getWorld()) {
            arenas.getBlocks().addBlock(evt.getBlock());
        }
      });
    }
  }


}
