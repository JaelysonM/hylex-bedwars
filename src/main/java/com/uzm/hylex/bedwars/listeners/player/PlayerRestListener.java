package com.uzm.hylex.bedwars.listeners.player;

import com.google.common.collect.Lists;
import com.uzm.hylex.bedwars.Core;
import com.uzm.hylex.bedwars.arena.Arena;
import com.uzm.hylex.bedwars.arena.improvements.UpgradeType;
import com.uzm.hylex.bedwars.arena.player.ArenaPlayer;
import com.uzm.hylex.bedwars.arena.team.Team;
import com.uzm.hylex.bedwars.utils.PlayerUtils;
import com.uzm.hylex.core.api.HylexPlayer;
import com.uzm.hylex.core.spigot.items.ItemBuilder;
import com.uzm.hylex.core.spigot.utils.BukkitUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static com.uzm.hylex.core.api.interfaces.Enums.ArenaState.IN_GAME;

public class PlayerRestListener implements Listener {

  @EventHandler
  public void onPlayerItemDamage(PlayerItemDamageEvent evt) {
    evt.setCancelled(true);
    evt.setDamage(0);
    evt.getPlayer().updateInventory();
  }

  @EventHandler
  public void onPlayerItemConsume(PlayerItemConsumeEvent evt) {
    Player player = evt.getPlayer();
    HylexPlayer hp = HylexPlayer.getByPlayer(player);
    if (hp != null) {
      ArenaPlayer ap = (ArenaPlayer) hp.getArenaPlayer();
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
    Player player = evt.getPlayer();
    HylexPlayer hp = HylexPlayer.getByPlayer(player);
    if (hp != null) {
      evt.setCancelled(hp.getAbstractArena() == null);
      ArenaPlayer ap = (ArenaPlayer) hp.getArenaPlayer();
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
            } else {
              evt.setCancelled(false);
              if (player.getFallDistance() > 0.0F) {
                evt.setCancelled(true);
                return;
              }
            }
            if (item.getType().name().contains("_SWORD") && !item.getType().name().contains("WOOD_SWORD")) {
              List<ItemStack> items = new ArrayList<ItemStack>(Lists.newArrayList(player.getInventory().getArmorContents()));
              if (!PlayerUtils.containsSword(items) && !player.getInventory().contains(Material.WOOD_SWORD))
                if (ap.getTeam() !=null) {
                  if (ap.getTeam().hasUpgrade(UpgradeType.SHARPENED_SWORDS)) {
                    player.getInventory().addItem(new ItemBuilder(Material.WOOD_SWORD).enchant(Enchantment.DAMAGE_ALL, ap.getTeam().getTier(UpgradeType.SHARPENED_SWORDS)).build());
                    player.updateInventory();
                  }else {
                    player.getInventory().addItem(new ItemBuilder(Material.WOOD_SWORD).build());

                  }
                }
            }
          }
        }
      }
    }
  }

  @EventHandler
  public void onCanBuild(BlockCanBuildEvent evt) {
    if (!evt.isBuildable()) {
      for (Entity nearby : evt.getBlock().getWorld().getNearbyEntities(evt.getBlock().getLocation(), 5, 5, 5)) {
        if (nearby.getType() == EntityType.PLAYER) {
          Player player = (Player) nearby;
          HylexPlayer hp = HylexPlayer.getByPlayer(player);
          if (hp != null) {
            ArenaPlayer ap = (ArenaPlayer) hp.getArenaPlayer();
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

  @EventHandler
  public void onBlockBreak(BlockBreakEvent evt) {
    Player player = evt.getPlayer();
    HylexPlayer hp = HylexPlayer.getByPlayer(player);
    if (hp != null) {
      ArenaPlayer ap = (ArenaPlayer) hp.getArenaPlayer();
      if (ap != null) {
        Arena arena = ap.getArena();
        if (arena != null) {
          if (arena.getState() != IN_GAME || ap.getCurrentState() != ArenaPlayer.CurrentState.IN_GAME) {
            evt.setCancelled(true);
          } else {
            Block block = evt.getBlock();
            if (block.getType().name().contains("BED_BLOCK")) {
              for (Team team : arena.listTeams()) {
                if (team.isBed(block)) {
                  if (!team.equals(ap.getTeam())) {
                    arena.destroyBed(team, hp);
                    return;
                  }

                  evt.setCancelled(true);
                  player.sendMessage("§cVocê não pode destruir a sua própria cama.");
                }
              }
            } else if (!arena.getBorders().contains(evt.getBlock().getLocation())) {
              evt.setCancelled(true);
              player.sendMessage("§cVocê não pode quebrar blocos aqui.");
            } else if (!arena.getBlocks().isPlacedBlock(block)) {
              evt.setCancelled(true);
              player.sendMessage("§cVocê só pode quebrar blocos colocados por jogadores.");
            }
          }
        }
      }
    }
  }

  @EventHandler
  public void onBlockPlace(BlockPlaceEvent evt) {
    Player player = evt.getPlayer();
    HylexPlayer hp = HylexPlayer.getByPlayer(evt.getPlayer());
    if (hp != null) {
      ArenaPlayer ap = (ArenaPlayer) hp.getArenaPlayer();
      if (ap != null) {
        Arena arena = ap.getArena();
        if (arena != null) {
          if (arena.getState() != IN_GAME || ap.getCurrentState() != ArenaPlayer.CurrentState.IN_GAME) {
            evt.setCancelled(true);
            player.sendMessage("§cVocê não tem permissão para quebrar blocos.");
          } else if (!arena.getBorders().contains(evt.getBlock().getLocation())) {
            evt.setCancelled(true);
            player.sendMessage("§cVocê chegou no limite de construção.");
          } else {
            Block block = evt.getBlock();
            evt.setCancelled(arena.isProtected(block.getLocation()));
            if (evt.isCancelled()) {
              player.sendMessage("§cVocê não pode colocar blocos aqui.");
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

  @EventHandler
  public void onPlayerBucketEmpty(PlayerBucketEmptyEvent evt) {
    Player player = evt.getPlayer();
    HylexPlayer hp = HylexPlayer.getByPlayer(player);
    if (hp != null) {
      ArenaPlayer ap = (ArenaPlayer) hp.getArenaPlayer();
      if (ap != null) {
        if (ap.getArena() != null) {
          if (evt.getBucket() == Material.LAVA_BUCKET) {
            evt.setCancelled(true);
          }
        }
      }
    }
  }
}
