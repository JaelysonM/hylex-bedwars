package com.uzm.hylex.bedwars.listeners.entity;

import com.uzm.hylex.bedwars.arena.Arena;
import com.uzm.hylex.bedwars.arena.player.ArenaPlayer;
import com.uzm.hylex.bedwars.arena.team.Team;
import com.uzm.hylex.bedwars.controllers.ArenaController;
import com.uzm.hylex.core.api.HylexPlayer;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import static com.uzm.hylex.core.api.interfaces.Enums.ArenaState.IN_GAME;

public class EntityListener implements Listener {

  @EventHandler(priority = EventPriority.HIGH)
  public void onEntityDamageByEntity(EntityDamageByEntityEvent evt) {
    if (evt.isCancelled()) {
      return;
    }
    if (evt.getEntity() instanceof Player) {
      Player player = (Player) evt.getEntity();

      Arena arena = null;
      HylexPlayer hp = HylexPlayer.getByPlayer(player);
      if (hp == null || hp.getArenaPlayer() == null || (arena = (Arena) hp.getArenaPlayer().getArena()) == null || arena.getState() != IN_GAME || ((ArenaPlayer) hp
        .getArenaPlayer()).getCurrentState() != ArenaPlayer.CurrentState.IN_GAME) {
        evt.setCancelled(true);
        return;
      }

      Team team = ((ArenaPlayer) hp.getArenaPlayer()).getTeam();

      Player damager = null;
      HylexPlayer hp2 = null;
      if (evt.getDamager() instanceof Player) {
        damager = (Player) evt.getDamager();
        hp2 = HylexPlayer.getByPlayer(damager);
        if (hp2 == null || hp2.getArenaPlayer() == null || !hp2.getArenaPlayer().getArena().equals(arena) || ((ArenaPlayer) hp2.getArenaPlayer())
          .getCurrentState() != ArenaPlayer.CurrentState.IN_GAME || (!damager.equals(player) && team != null && team.equals(((ArenaPlayer) hp2.getArenaPlayer()).getTeam()))) {
          evt.setCancelled(true);
          return;
        }
      }

      if (evt.getDamager() instanceof Projectile) {
        Projectile proj = (Projectile) evt.getDamager();
      /*  if (proj.hasMetadata("BEDWARS_FIREBALL")) {
        }
*/
        if (proj.getShooter() instanceof Player) {
          damager = (Player) proj.getShooter();
          hp2 = HylexPlayer.getByPlayer(damager);
          if (hp2 == null || hp2.getArenaPlayer() == null || !hp2.getArenaPlayer().getArena().equals(arena) || ((ArenaPlayer) hp2.getArenaPlayer())
            .getCurrentState() != ArenaPlayer.CurrentState.IN_GAME || (!damager.equals(player) && team != null && team.equals(((ArenaPlayer) hp2.getArenaPlayer()).getTeam()))) {
            evt.setCancelled(true);
            return;
          }
        }
      }

      if (damager != null) {
        hp.setLastHit(damager);
      }
    } else if (evt.getEntity() instanceof Fireball && evt.getDamager() instanceof Player) {
      Player damager = (Player) evt.getDamager();
      HylexPlayer hp = HylexPlayer.getByPlayer(damager);
      if (hp == null || hp.getArenaPlayer() == null || ((ArenaPlayer) hp.getArenaPlayer()).getCurrentState() != ArenaPlayer.CurrentState.IN_GAME) {
        evt.setCancelled(true);
      }
    }
  }

  @EventHandler
  public void onEntityDamage(EntityDamageEvent evt) {
    if (evt.getEntity() instanceof Player) {
      Player player = (Player) evt.getEntity();

      HylexPlayer hp = HylexPlayer.getByPlayer(player);
      if (hp != null) {
        ArenaPlayer ap = (ArenaPlayer) hp.getArenaPlayer();
        if (ap != null) {
          Arena arena = ap.getArena();
          if (arena == null) {
            evt.setCancelled(true);
          } else {
            if (arena.getState() != IN_GAME) {
              evt.setCancelled(true);
              if (evt.getCause() == DamageCause.VOID) {
                player.teleport(arena.getWaitingLocation());
              }
            } else if (ap.getCurrentState() != ArenaPlayer.CurrentState.IN_GAME) {
              evt.setCancelled(true);
              if (evt.getCause() == DamageCause.VOID) {
                player.teleport(arena.getSpectatorLocation());
              }
            } else if (evt.getCause() == DamageCause.VOID) {
              evt.setDamage(player.getMaxHealth());
            }
          }
        }
      }
    }
  }

  @EventHandler
  public void onProjectileHit(ProjectileHitEvent evt) {
    Projectile proj = evt.getEntity();
    Location explosionLocation = proj.getLocation();

    if (proj.hasMetadata("BEDWARS_FIREBALL")) {
      if (explosionLocation != null) {
        boolean flag = ((CraftWorld) explosionLocation.getWorld()).getHandle().getGameRules().getBoolean("mobGriefing");
        ((CraftWorld) explosionLocation.getWorld()).getHandle().createExplosion(((CraftEntity) evt.getEntity()).getHandle(), explosionLocation.getX(), explosionLocation.getY() + 0.98 / 2.0F, explosionLocation.getZ() ,1.0F, true, true);
      }

    }

  }

  @EventHandler
  public void onCreatureSpawn(CreatureSpawnEvent evt) {
    evt.setCancelled(evt.getSpawnReason() != SpawnReason.CUSTOM);
  }

  @EventHandler
  public void onItemSpawn(ItemSpawnEvent evt) {
    Arena arena = ArenaController.getArena(evt.getEntity().getWorld().getName());
    if (arena == null || arena.getState() != IN_GAME || evt.getEntity().getItemStack().getType().name().contains("BED")) {
      evt.setCancelled(true);
    }
  }

  @EventHandler
  public void onFoodLevelChange(FoodLevelChangeEvent evt) {
    evt.setCancelled(true);
  }
}
