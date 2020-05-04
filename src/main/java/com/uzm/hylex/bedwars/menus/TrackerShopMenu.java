package com.uzm.hylex.bedwars.menus;

import com.uzm.hylex.bedwars.arena.player.ArenaEquipment;
import com.uzm.hylex.bedwars.arena.player.ArenaPlayer;
import com.uzm.hylex.bedwars.arena.team.Team;
import com.uzm.hylex.bedwars.arena.team.Teams;
import com.uzm.hylex.core.Core;
import com.uzm.hylex.core.spigot.inventories.PlayerMenu;
import com.uzm.hylex.core.spigot.items.ItemBuilder;
import com.uzm.hylex.core.utils.BukkitUtils;
import com.uzm.hylex.core.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class TrackerShopMenu extends PlayerMenu {

  @EventHandler
  public void onInventoryClick(InventoryClickEvent evt) {
    if (evt.getInventory().equals(getInventory())) {
      evt.setCancelled(true);

      if (evt.getWhoClicked() instanceof Player && evt.getWhoClicked().equals(player)) {
        ItemStack item = evt.getCurrentItem();
        Team team = ap.getTeam();
        if (ap.getArena() == null || team == null) {
          player.closeInventory();
          return;
        }

        if (evt.getClickedInventory() != null && evt.getClickedInventory().equals(evt.getInventory()) && item != null && item.getType() != Material.AIR) {
          boolean bedsBroken = ap.getArena().listTeams().stream().noneMatch(bt -> !bt.getMembers().contains(ap) && bt.getSitation() == Team.Sitation.STANDING);
          Team target = (Team) getAttached(evt.getSlot());
          if (target != null) {
            if (target.getSitation() == Team.Sitation.ELIMINATED) {
              player.sendMessage("§cEste time já foi eliminado.");
              return;
            }

            if (!bedsBroken) {
              player.sendMessage("§cAs camas inimigas ainda não foram destruídas por completo.");
              return;
            }

            if (BukkitUtils.getCountFromMaterial(player.getInventory(), Material.EMERALD) < 2) {
              player.sendMessage("§cVocê não possui Esmeraldas suficientes!");
              return;
            }

            if (target.getTeamType().equals(ap.getEquipment().getTracking())) {
              player.sendMessage("§cVocê já está rastreando este time!");
              return;
            }

            BukkitUtils.removeItem(player.getInventory(), Material.EMERALD, 2);
            ap.getEquipment().setTracking(target.getTeamType());
            player.sendMessage("§aVocê comprou §6" + StringUtils.stripColors(item.getItemMeta().getDisplayName()));
            player.closeInventory();
          }
        }
      }
    }
  }

  private static final List<Integer> SLOTS = Arrays.asList(10, 11, 12, 13, 14, 15, 16, 17);
  private ArenaPlayer ap;

  public TrackerShopMenu(ArenaPlayer ap) {
    super(ap.getPlayer(), "Rastreador de oponente", 3);
    this.ap = ap;

    Team selfTeam = ap.getTeam();
    ArenaEquipment equipment = ap.getEquipment();

    int index = 0;
    boolean bedsBroken = ap.getArena().listTeams().stream().noneMatch(bt -> !bt.getMembers().contains(ap) && bt.getSitation() == Team.Sitation.STANDING);
    for (Teams teams : Teams.values()) {
      Team team = ap.getArena().getTeams().get(teams);
      if (team == null || selfTeam.equals(team)) {
        continue;
      }

      String color = !bedsBroken ? "&e" : BukkitUtils.getCountFromMaterial(player.getInventory(), Material.EMERALD) < 2 ? "&c" : "&a";
      ItemStack icon = new ItemBuilder("WOOL:" + teams.getColor().getData() + " : 1 : display=" + color + "Rastreador do Time " + teams
        .getName() + " : lore=&7Compre a melhoria para a sua\n&7bússola de rastreio de jogadores\n&7próximos do time " + teams
        .getDisplayName() + " &7até você\n&7morrer.\n \n&7Custo: &22 Esmeraldas\n ").build();
      ItemMeta meta = icon.getItemMeta();
      List<String> lore = meta.getLore();
      if (teams.equals(equipment.getTracking())) {
        lore.add("§cVocê já está rastreando este time!");
      } else if (team.getSitation() == Team.Sitation.ELIMINATED) {
        lore.add("§cEste time já foi eliminado.");
      } else if ("&c".equals(color)) {
        lore.add("§cVocê não possui Esmeraldas suficientes!");
      } else if ("&e".equals(color)) {
        lore.add("§cAs camas inimigas ainda não foram");
        lore.add("§cdestruídas por completo.");
      } else {
        lore.add("§eClique para comprar!");
      }
      meta.setLore(lore);
      icon.setItemMeta(meta);

      this.setItem(SLOTS.get(index), icon);
      this.attachObject(SLOTS.get(index++), team);
    }

    this.open();
    this.register(Core.getInstance());
  }

  public void cancel() {
    this.ap = null;
    HandlerList.unregisterAll(this);
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent evt) {
    if (evt.getPlayer().equals(player)) {
      this.cancel();
    }
  }

  @EventHandler
  public void onInventoryClose(InventoryCloseEvent evt) {
    if (evt.getPlayer().equals(player) && evt.getInventory().equals(this.getInventory())) {
      this.cancel();
    }
  }
}
