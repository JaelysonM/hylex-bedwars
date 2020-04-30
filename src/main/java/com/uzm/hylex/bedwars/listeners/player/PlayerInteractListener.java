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
            } else if (evt.getItem() != null) {
                if (evt.getItem().hasItemMeta()) {
                    if (evt.getItem().getItemMeta().hasDisplayName()) {
                        if (evt.getItem().getItemMeta().getDisplayName().startsWith("§aSetar posições")) {
                            evt.setCancelled(true);
                            if (evt.getAction().toString().contains("LEFT")) {
                                hp.getTemporaryLocation().put(1, player.getLocation());
                                player.sendMessage("§6[ArenaCreator] §aPosição #1 setada com sucesso.");
                            } else if (evt.getAction().toString().contains("RIGHT")) {
                                hp.getTemporaryLocation().put(2, player.getLocation());
                                player.sendMessage("§6[ArenaCreator] §aPosição #2 setada com sucesso.");
                            }
                        } else if (evt.getItem().getItemMeta().getDisplayName().equalsIgnoreCase("§eAdicionar gerador do time")) {
                            evt.setCancelled(true);
                            if (hp.getAuxiler() != null) {
                                Teams team = (Teams) hp.getAuxiler();
                                hp.getAbstractArena().getTeams().get(team).getTeamGenerators().add(player.getLocation());
                                player.sendMessage(
                                        "§6[ArenaCreator] §a+1 §7Gerador do time " + team.getDisplayName() + " §8(" + hp.getAbstractArena().getTeams().get(team).getTeamGenerators().size() + ")");
                            }
                        } else if (evt.getItem().getItemMeta().getDisplayName().startsWith("§aSetar NPCs §7(Esquerdo: Altere o tipo/Direito: Loja/Direito+Shift: Melhorias)")) {
                            evt.setCancelled(true);
                            if (hp.getAuxiler() != null) {
                                Teams team = (Teams) hp.getAuxiler();
                                if (evt.getAction().name().contains("LEFT")) {
                                    player.getInventory().setItem(3, new ItemBuilder(Material.ENDER_PORTAL_FRAME).name("§aSetar localizações importantes §7(Esquerdo: Altere o tipo/Direito: Cama/Direito+Shift: Spawn)").build());

                                } else {
                                    if (player.isSneaking()) {
                                        hp.getAbstractArena().getTeams().get(team).setUpgradeLocation(player.getLocation());
                                        player.sendMessage("§6[ArenaCreator] §7Você setou o §bposição do NPC-Melhorias§7 do time " + team.getDisplayName() + " §7na sua localização.");

                                    } else {
                                        hp.getAbstractArena().getTeams().get(team).setShopLocation(player.getLocation());
                                        player.sendMessage("§6[ArenaCreator] §7Você setou o §bposição do NPC-Loja§7 do time " + team.getDisplayName() + " §7a sua localização.");

                                    }
                                }

                            }


                        } else if (evt.getItem().getItemMeta().getDisplayName().startsWith("§aSetar localizações importantes §7(Esquerdo: Altere o tipo/Direito: Cama/Direito+Shift: Spawn)")) {
                            evt.setCancelled(true);
                            if (hp.getAuxiler() != null) {
                                Teams team = (Teams) hp.getAuxiler();
                                if (evt.getAction().name().contains("LEFT")) {
                                    player.getInventory().setItem(3, new ItemBuilder(Material.COMPASS).name("§aSetar NPCs §7(Esquerdo: Altere o tipo/Direito: Loja/Direito+Shift: Melhorias)").build());

                                } else {
                                    if (player.isSneaking()) {
                                        hp.getAbstractArena().getTeams().get(team).setSpawnLocation(player.getLocation());
                                        player.sendMessage("§6[ArenaCreator] §7Você setou o §blocal spawns§7 do time " + team.getDisplayName() + " §7na sua localização.");

                                    } else {
                                        hp.getAbstractArena().getTeams().get(team).setBedLocation(player.getLocation());
                                        player.sendMessage("§6[ArenaCreator] §7Você setou o §bposição da cama§7 do time " + team.getDisplayName() + " §7na sua localização.");

                                    }
                                }

                            }


                        } else if (evt.getItem().getItemMeta().getDisplayName().startsWith("§aConfigurando o time: ")) {
                            evt.setCancelled(true);
                            if (hp.getAuxiler() != null) {
                                Teams team = (Teams) hp.getAuxiler();
                                if (evt.getAction().name().contains("LEFT")) {
                                    Teams newTeam = team.next();
                                    player.getInventory().setItem(4, new ItemBuilder(Material.STAINED_GLASS_PANE).durability(newTeam.getColor().getData()).name("§aConfigurando o time: " + newTeam.getDisplayName() + " §7(Clique para alterar)").build());
                                    hp.getTemporaryLocation().clear();
                                    hp.setAuxiler(newTeam);
                                }
                            }

                        } else if (evt.getItem().getItemMeta().getDisplayName().equalsIgnoreCase("§aCriar um gerador §8(Tipo definido de acordo com o bloco clicado)")) {
                            evt.setCancelled(true);
                            if (evt.getAction().toString().contains("BLOCK")) {
                                Block clicked = evt.getClickedBlock();
                                if (clicked.getType() != null && (clicked.getType() == Material.EMERALD_BLOCK || clicked.getType() == Material.DIAMOND_BLOCK)) {
                                    Generator.Type type = Generator.Type.valueOf(clicked.getType().toString().replace("_BLOCK", ""));
                                    Generator gen = new Generator(hp.getAbstractArena(), type, clicked.getLocation().add(0, 1, 0));
                                    hp.getAbstractArena().getGenerators().add(gen);
                                    player.sendMessage("§6[ArenaCreator] §7Você criou um gerador do tipo " + type.getName() + " §7na localização que você clicou.");
                                } else {
                                    player.sendMessage("§cNão pode criar um gerador a partir deste bloco, o mesmo deve ser um §bBloco de esmeralda §cou §aBloco de esmeralda§c.");
                                }
                            }
                        } else if (evt.getItem().getItemMeta().getDisplayName().equalsIgnoreCase("§bBordas da arena §7(Clique para alterar)")) {
                            evt.setCancelled(true);
                            if (evt.getAction().toString().contains("LEFT")) {
                                player.getInventory().setItem(4, new ItemBuilder(Material.TRIPWIRE_HOOK).name("§aLocais protegidos §7(Clique para alterar)").build());
                            }
                        } else if (evt.getItem().getItemMeta().getDisplayName().equalsIgnoreCase("§aLocais protegidos §7(Clique para alterar)")) {
                            if (evt.getAction().toString().contains("LEFT")) {
                                player.getInventory().setItem(4, new ItemBuilder(Material.GLASS).name("§bBordas da arena §7(Clique para alterar)").build());
                            }
                        } else if (evt.getItem().getItemMeta().getDisplayName().equalsIgnoreCase("§aSetar > §8Local de espera  §7(Clique para alterar)")) {
                            evt.setCancelled(true);
                            if (evt.getAction().toString().contains("LEFT")) {
                                player.getInventory().setItem(4, new ItemBuilder(Material.EYE_OF_ENDER).name("§aSetar > §8Spawn do espectador  §7(Clique para alterar)").build());
                            } else {
                                player.sendMessage("§6[ArenaCreator] §7Você setou o §bLocal de espera§7 da arena na sua localização.");
                                hp.getAbstractArena().setWaitingLocation(player.getLocation());
                            }
                        } else if (evt.getItem().getItemMeta().getDisplayName().equalsIgnoreCase("§aSetar > §8Spawn do espectador  §7(Clique para alterar)")) {
                            evt.setCancelled(true);
                            if (evt.getAction().toString().contains("LEFT")) {
                                player.getInventory().setItem(4, new ItemBuilder(Material.NETHER_STAR).name("§aSetar > §8Local de espera  §7(Clique para alterar)").build());
                            } else {
                                player.sendMessage("§6[ArenaCreator] §7Você setou o §bSpawn do espectador§7 da arena na sua localização.");
                                hp.getAbstractArena().setSpectatorLocation(player.getLocation());
                            }
                        } else if (evt.getItem().getItemMeta().getDisplayName().equalsIgnoreCase("§cAbrir configurações")) {
                            evt.setCancelled(true);
                            new MainPainel(player, hp.getAbstractArena());
                        } else if (evt.getItem().getItemMeta().getDisplayName().equalsIgnoreCase("§aSalvar alterações")) {
                            evt.setCancelled(true);
                            ItemStack item = player.getInventory().getItem(4);
                            if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                                Material material = item.getType();
                                String displayName = item.getItemMeta().getDisplayName();

                                if (material == Material.NETHER_STAR || material == Material.EYE_OF_ENDER) {
                                    player
                                            .sendMessage("§a* Alterações salvadas com sucesso: §fLocalizações da arena." + (hp.getAbstractArena().getSpectatorLocation() != null ? " §e(Alterado)" : ""));
                                    player.getInventory().clear();
                                    new MainPainel(player, hp.getAbstractArena());
                                    hp.getTemporaryLocation().clear();
                                } else if (material == Material.DIAMOND_AXE) {
                                    player.sendMessage("§a* Alterações salvadas com sucesso: §fGeradores globais §8(" + hp.getAbstractArena().getGenerators().size() + ")");
                                    player.getInventory().clear();
                                    new MainPainel(player, hp.getAbstractArena());
                                    hp.getTemporaryLocation().clear();
                                } else if (material == Material.STAINED_GLASS_PANE) {
                                    if (hp.getAuxiler() != null) {
                                        Teams team = (Teams) hp.getAuxiler();
                                        if (hp.getTemporaryLocation().size() == 2) {
                                            player.sendMessage("§a* Você salvou as §fbordas §ado time " + team.getDisplayName() + " §acom sucesso.");
                                            hp.getAbstractArena().getTeams().get(team).setBorder(new CubeId(hp.getTemporaryLocation().get(1), hp.getTemporaryLocation().get(2)));
                                            hp.getTemporaryLocation().clear();
                                        } else {
                                            player.sendMessage("§6[ArenaCreator] §cVocê não setou todas a localizações ainda faltam §f" + (2 - hp.getTemporaryLocation().size()) + "§c.");

                                        }

                                    }


                                } else if (displayName.equalsIgnoreCase("§bBordas da arena §7(Clique para alterar)")) {
                                    if (hp.getTemporaryLocation().size() == 2) {
                                        player.sendMessage("§a* Alterações salvadas com sucesso: §fBordas da arena." + (hp.getAbstractArena().getBorders() != null ? " §e(Alterado)" : ""));
                                        hp.getAbstractArena().setBorders(new CubeId(hp.getTemporaryLocation().get(1), hp.getTemporaryLocation().get(2)));
                                        hp.getTemporaryLocation().clear();
                                    } else {
                                        player.sendMessage("§6[ArenaCreator] §cVocê não setou todas a localizações ainda faltam §f" + (2 - hp.getTemporaryLocation().size()) + "§c.");
                                    }
                                } else if (displayName.equalsIgnoreCase("§aLocais protegidos §7(Clique para alterar)")) {
                                    if (hp.getTemporaryLocation().size() == 2) {
                                        hp.getAbstractArena().getCantConstruct().add(new CubeId(hp.getTemporaryLocation().get(1), hp.getTemporaryLocation().get(2)));
                                        player.sendMessage("§a* Alterações salvadas com sucesso: §fÁrea protegida §8(" + hp.getAbstractArena().getCantConstruct().size() + ")");
                                        hp.getTemporaryLocation().clear();
                                    } else {
                                        player.sendMessage("§6[ArenaCreator] §cVocê não setou todas a localizações ainda faltam §f" + (2 - hp.getTemporaryLocation().size()) + "§c.");
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
