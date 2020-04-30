package com.uzm.hylex.bedwars.listeners.player;

import com.uzm.hylex.bedwars.Core;
import com.uzm.hylex.bedwars.arena.Arena;
import com.uzm.hylex.bedwars.arena.creator.inventory.MainPainel;
import com.uzm.hylex.bedwars.arena.generators.Generator;
import com.uzm.hylex.bedwars.arena.player.ArenaPlayer;
import com.uzm.hylex.bedwars.arena.team.Teams;
import com.uzm.hylex.bedwars.controllers.HylexPlayer;
import com.uzm.hylex.bedwars.utils.BukkitUtils;
import com.uzm.hylex.bedwars.utils.CubeId;
import com.uzm.hylex.core.spigot.items.ItemBuilder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import static com.uzm.hylex.bedwars.arena.enums.ArenaEnums.ArenaState.IN_GAME;

public class PlayerInteractListener implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent evt) {
        Player player = evt.getPlayer();

        HylexPlayer hp = HylexPlayer.get(player);
        if (hp != null) {
            ArenaPlayer ap = hp.getArenaPlayer();
            if (ap != null) {
                Arena arena = ap.getArena();
                if (arena != null) {
                    if (arena.getState() != IN_GAME || ap.getCurrentState() != ArenaPlayer.CurrentState.IN_GAME) {
                        evt.setCancelled(true);
                        // TODO: click dos items de sair etc
                    } else {
                        ItemStack item = evt.getItem();
                        if (item != null && evt.getAction().name().contains("RIGHT")) {
                            if (item.getType().name().contains("WATER_BUCKET")) {
                                if (evt.getClickedBlock() != null) {
                                    Block block = evt.getClickedBlock();
                                    if (!arena.getBorders().contains(block.getLocation()) || !arena.getBorders().contains(block.getRelative(BlockFace.UP).getLocation())) {
                                        evt.setCancelled(true);
                                        return;
                                    }

                                    // TODO: Verificar se a área não é prtegida para continuar a por a água
                              /*if (!nearZone) {
                                  Bukkit.getScheduler().runTaskLaterAsynchronously(Core.getInstance(), () -> {
                                      BukkitUtils.removeItem(player.getInventory(), Material.matchMaterial("BUCKET"), 1);
                                  }, 1);
                              }*/
                                }

                                return;
                            }

                            if (evt.getAction().name().contains("AIR") && item.getType().name().contains("FIREBALL")) {
                                player.launchProjectile(Fireball.class).setMetadata("BEDWARS_FIREBALL", new FixedMetadataValue(Core.getInstance(), true));
                                player.playSound(player.getLocation(), Sound.GHAST_CHARGE, 1.0F, 1.0F);
                                BukkitUtils.removeItem(player.getInventory(), item.getType(), 1);
                                return;
                            }

                            if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                                if (item.getItemMeta().getDisplayName().equals("§aRastreador")) {
                                    // TODO: Menu de track da compass
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
