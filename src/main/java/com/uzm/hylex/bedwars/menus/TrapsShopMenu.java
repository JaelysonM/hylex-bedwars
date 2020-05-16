package com.uzm.hylex.bedwars.menus;

import com.uzm.hylex.bedwars.arena.improvements.Trap;
import com.uzm.hylex.bedwars.arena.player.ArenaPlayer;
import com.uzm.hylex.bedwars.arena.team.Team;
import com.uzm.hylex.core.Core;
import com.uzm.hylex.core.java.util.StringUtils;
import com.uzm.hylex.core.spigot.inventories.PlayerMenu;
import com.uzm.hylex.core.spigot.items.ItemBuilder;
import com.uzm.hylex.core.spigot.utils.BukkitUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class TrapsShopMenu extends PlayerMenu {

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
          if (evt.getSlot() == 31) {
            new UpgradeShopMenu(this.ap);
          } else {
            Trap trap = (Trap) getAttached(evt.getSlot());
            if (trap != null) {
              if (team.getTraps().size() > 2) {
                player.sendMessage("§cVocê já possui o máximo de armadilhas na fila!");
                return;
              }

              if (BukkitUtils.getCountFromMaterial(player.getInventory(), trap.getCoinTrade().getMaterial()) < (team.getTraps().size() + 1)) {
                player.sendMessage("§cVocê não possui recursos suficientes para adquirir esta Armadilha!");
                return;
              }

              BukkitUtils.removeItem(player.getInventory(), trap.getCoinTrade().getMaterial(), team.getTraps().size() + 1);
              team.addTrap(trap);

              ap.getTeam().getAlive().stream().map(ArenaPlayer::getPlayer)
                .forEach(players -> players.sendMessage(player.getDisplayName() + " §ecomprou §6" + StringUtils.stripColors(item.getItemMeta().getDisplayName())));

              new TrapsShopMenu(ap);
            }
          }
        }
      }
    }
  }

  private ArenaPlayer ap;

  public TrapsShopMenu(ArenaPlayer ap) {
    super(ap.getPlayer(), "Comprar armadilha", 4);

    this.ap = ap;
    Team team = ap.getTeam();
    int slot = 10;
    for (Trap trap : Trap.listTraps()) {
      String color = BukkitUtils.getCountFromMaterial(player.getInventory(), trap.getCoinTrade().getMaterial()) < (team.getTraps().size() + 1) ? "&c" : "&a";
      ItemStack icon = new ItemBuilder(trap.getIcon().replace("{color}", color)).build();
      ItemMeta meta = icon.getItemMeta();
      List<String> lore = meta.getLore();
      lore.add("");
      lore.add("§7Custo: §b" + (team.getTraps().size() + 1) + " Diamante" + (team.getTraps().size() + 1 > 1 ? "s" : ""));
      lore.add("");
      if ("&c".equals(color)) {
        lore.add("§cVocê não possui Diamantes suficientes!");
      } else {
        lore.add("§eClique para comprar!");
      }
      meta.setLore(lore);
      icon.setItemMeta(meta);

      this.setItem(slot, icon);
      this.attachObject(slot++, trap);
    }

    this.setItem(31, new ItemBuilder("ARROW : 1 : display=&aVoltar").build());

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
