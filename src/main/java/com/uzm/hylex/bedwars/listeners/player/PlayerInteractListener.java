package com.uzm.hylex.bedwars.listeners.player;

import com.uzm.hylex.bedwars.Core;
import com.uzm.hylex.bedwars.arena.Arena;
import com.uzm.hylex.bedwars.arena.player.ArenaPlayer;
import com.uzm.hylex.bedwars.arena.team.Team;
import com.uzm.hylex.bedwars.controllers.MatchmakingController;
import com.uzm.hylex.bedwars.menus.ItemShopMenu;
import com.uzm.hylex.bedwars.menus.TrackerShopMenu;
import com.uzm.hylex.bedwars.menus.UpgradeShopMenu;
import com.uzm.hylex.bedwars.nms.NMS;
import com.uzm.hylex.bedwars.proxy.ServerItem;
import com.uzm.hylex.core.api.HylexPlayer;
import com.uzm.hylex.core.libraries.npclib.api.NPC;
import com.uzm.hylex.core.libraries.npclib.api.event.NPCRightClickEvent;
import com.uzm.hylex.core.spigot.utils.BukkitUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import static com.uzm.hylex.core.api.interfaces.Enums.ArenaState.IN_GAME;

public class PlayerInteractListener implements Listener {

  @EventHandler
  public void onNPCRightClick(NPCRightClickEvent evt) {
    HylexPlayer hp = HylexPlayer.getByPlayer(evt.getPlayer());
    if (hp != null) {
      ArenaPlayer ap = (ArenaPlayer) hp.getArenaPlayer();
      if (ap != null && ap.getTeam() != null && ap.getCurrentState() == ArenaPlayer.CurrentState.IN_GAME) {
        NPC npc = evt.getNPC();
        if (npc.data().has("SHOP")) {
          String shop = npc.data().get("SHOP");
          if (shop.equalsIgnoreCase("item")) {
            new ItemShopMenu(ap, null);
          } else {
            new UpgradeShopMenu(ap);
          }
        }
      }
    }
  }

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent evt) {

    ItemStack item = evt.getItem();

    Player player = evt.getPlayer();

    HylexPlayer hp = HylexPlayer.getByPlayer(player);
    if (hp != null) {
      if (item != null && evt.getAction().name().contains("RIGHT")) {
        if (hp.getArenaPlayer() == null) {
          if (item.getType() == Material.BED) {
            ServerItem.getServerItem("lobby").connect(hp);
          }
        }
      }
      ArenaPlayer ap = (ArenaPlayer) hp.getArenaPlayer();
      if (ap != null) {
        Arena arena = ap.getArena();
        if (evt.getClickedBlock() != null && !player.isSneaking() && evt.getClickedBlock().getType().equals(Material.BED_BLOCK) && evt.getAction().name().contains("RIGHT")) {
          evt.setCancelled(true);
          return;
        }


        if (arena != null) {
          if (arena.getState() != IN_GAME || ap.getCurrentState() != ArenaPlayer.CurrentState.IN_GAME) {
            evt.setCancelled(true);
            if (item != null && evt.getAction().name().contains("RIGHT")) {
              if (item.getType() == Material.PAPER) {
                MatchmakingController.findMatch(hp, arena.getConfiguration().getMode().toUpperCase());
              } else if (item.getType() == Material.BED) {
                ServerItem.getServerItem("lobby").connect(hp);
              }
            }
          } else {
            if (evt.getClickedBlock() != null && evt.getClickedBlock().getType() == Material.CHEST) {
              Team team = arena.listTeams().stream().filter(t -> t.getBorder().contains(evt.getClickedBlock().getLocation())).findFirst().orElse(null);
              if (team != null && team.getSitation() != Team.Sitation.ELIMINATED) {
                if (!team.getMembers().contains(ap)) {
                  evt.setCancelled(true);
                  player.sendMessage("§cVocê não pode abrir este baú enquanto o time não estiver eliminado.");
                }
              }
            } else if (item != null && evt.getAction().name().contains("RIGHT")) {
              if (item.getType().name().contains("WATER_BUCKET")) {
                if (evt.getClickedBlock() != null) {
                  Block block = evt.getClickedBlock();
                  if (!arena.getBorders().contains(block.getLocation()) || !arena.getBorders().contains(block.getRelative(BlockFace.UP).getLocation())) {
                    evt.setCancelled(true);
                    player.sendMessage("§cVocê não pode quebrar por água aqui.");
                    return;
                  }

                  if (!arena.isProtected(block.getLocation())) {
                    Bukkit.getScheduler()
                      .runTaskLaterAsynchronously(Core.getInstance(), () -> BukkitUtils.removeItem(player.getInventory(), Material.matchMaterial("BUCKET"), 1), 1);
                  }
                }
              } else if (evt.getAction().name().contains("AIR") && item.getType().name().contains("FIREBALL")) {
                //  Fireball fire = NMS.createFireball(player);
                // fire.setMetadata("BEDWARS_FIREBALL", new FixedMetadataValue(Core.getInstance(), true));
                Location eye = player.getEyeLocation();
                Location loc = eye.add(eye.getDirection().multiply(1.2));
                Fireball f = (Fireball) loc.getWorld().spawnEntity(loc, EntityType.FIREBALL);
                f.setShooter(player);
                f.setMetadata("BEDWARS_FIREBALL", new FixedMetadataValue(Core.getInstance(), player));
                BukkitUtils.removeItem(player.getInventory(), item.getType(), 1);
              } else if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                if (item.getItemMeta().getDisplayName().equals("§aRastreador")) {
                  new TrackerShopMenu(ap);
                }
              }
            }
          }
        }
      }
    }
  }
}
