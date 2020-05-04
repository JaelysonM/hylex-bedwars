package com.uzm.hylex.bedwars.menus;

import com.uzm.hylex.bedwars.arena.improvements.Trap;
import com.uzm.hylex.bedwars.arena.improvements.Upgrades;
import com.uzm.hylex.bedwars.arena.player.ArenaPlayer;
import com.uzm.hylex.bedwars.arena.team.Team;
import com.uzm.hylex.bedwars.loaders.UpgradesLoader;
import com.uzm.hylex.core.Core;
import com.uzm.hylex.core.spigot.inventories.PlayerMenu;
import com.uzm.hylex.core.spigot.items.ItemBuilder;
import com.uzm.hylex.core.utils.BukkitUtils;
import com.uzm.hylex.core.utils.StringUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class UpgradeShopMenu extends PlayerMenu {

  @EventHandler
  public void onInventoryClick(InventoryClickEvent evt) {
    if (evt.getInventory().equals(getInventory())) {
      evt.setCancelled(true);

      if (evt.getWhoClicked() instanceof Player && evt.getWhoClicked().equals(player)) {
        ItemStack item = evt.getCurrentItem();
        Team team;
        if (ap.getArena() == null || (team = ap.getTeam()) == null) {
          player.closeInventory();
          return;
        }

        if (evt.getClickedInventory() != null && evt.getClickedInventory().equals(evt.getInventory()) && item != null && item.getType() != Material.AIR) {
          if (evt.getSlot() == 16) {
            new TrapsShopMenu(ap);
          } else {
            Upgrades upgrade = (Upgrades) getAttached(evt.getSlot());
            if (upgrade != null) {
              boolean maxTier = team.getTier(upgrade.getType()) >= upgrade.getMaxTier();
              int nextTier = maxTier ? upgrade.getMaxTier() : team.getTier(upgrade.getType()) + 1;
              if (maxTier) {
                return;
              }

              if (BukkitUtils.getCountFromMaterial(player.getInventory(), upgrade.getCoinTrade().getMaterial()) < upgrade.getPrice(nextTier)) {
                player.sendMessage("§cVocê não possui recursos suficientes para adquirir esta melhoria!");
                return;
              }

              BukkitUtils.removeItem(player.getInventory(), upgrade.getCoinTrade().getMaterial(), upgrade.getPrice(nextTier));
              team.evolve(upgrade.getType());
              team.getAlive().stream().filter(ap -> ap.getCurrentState() == ArenaPlayer.CurrentState.IN_GAME).forEach(ArenaPlayer::refresh);

              ap.getTeam().getAlive().stream().map(ArenaPlayer::getPlayer)
                .forEach(players -> players.sendMessage(player.getDisplayName() + " §ecomprou §6" + StringUtils.stripColors(item.getItemMeta().getDisplayName())));

              new UpgradeShopMenu(ap);
            }
          }
        }
      }
    }
  }

  private ArenaPlayer ap;

  public UpgradeShopMenu(ArenaPlayer ap) {
    super(ap.getPlayer(), "Melhorias", 5);

    this.ap = ap;
    Team team = ap.getTeam();
    for (Upgrades upgrade : UpgradesLoader.listUpgrades()) {
      boolean maxTier = team.getTier(upgrade.getType()) >= upgrade.getMaxTier();
      int nextTier = maxTier ? upgrade.getMaxTier() : team.getTier(upgrade.getType()) + 1;
      String color = BukkitUtils.getCountFromMaterial(player.getInventory(), upgrade.getCoinTrade().getMaterial()) < upgrade.getPrice(nextTier) ? "&c" : "&a";
      ItemStack icon = new ItemBuilder(
        upgrade.getType().getIcon().replace("{color}", maxTier ? "&e" : color).replace("{tier}", nextTier > 3 ? nextTier == 4 ? "IV" : "V" : StringUtils.repeat("I", nextTier)))
        .build();
      ItemMeta meta = icon.getItemMeta();
      List<String> lore = new ArrayList<>(upgrade.getUpdatableDescription(team.getTier(upgrade.getType()), !"&c".equals(color)));
      lore.add("");
      if (maxTier) {
        lore.add("§cMelhoria já está maximizada!");
      } else if (color.equals("&c")) {
        lore.add("§cVocê não possui Diamantes suficientes!");
      } else {
        lore.add("§eClique para comprar!");
      }
      meta.setLore(lore);
      icon.setItemMeta(meta);

      this.setItem(upgrade.getType().getSlot(), icon);
      this.attachObject(upgrade.getType().getSlot(), upgrade);
    }

    this.setItem(16,
      new ItemBuilder("LEATHER : 1 : display=&aComprar Armadilhas : lore=&7Armadilhas compradas serão colocadas\n&7na fila abaixo.\n \n&eClique para comprar!").build());

    for (int i = 0; i < 9; i++) {
      if (i == 0 || i == 8) {
        this.setItem(18 + i, new ItemBuilder("STAINED_GLASS_PANE:7 : 1 : display=&f").build());
      } else {
        this.setItem(18 + i, new ItemBuilder("STAINED_GLASS_PANE:7 : 1 : display=&8↑ &7Melhorias : lore=&8↓ &7Fila de Armadilhas").build());
      }
    }

    for (int slot = 30; slot < 33; slot++) {
      int index = slot - 30;
      Trap trap = index < team.getTraps().size() ? team.getTraps().get(index) : null;
      if (trap == null) {
        int trapIndex = (index + 1);
        this.setItem(slot, new ItemBuilder(
          "STAINED_GLASS:7 : 1 : display=&cArmadilha #" + trapIndex + ": Nenhuma! : lore=&7O "+ (trapIndex == 1 ?"primeiro":(trapIndex== 2? "segundo":"terceiro"))+ " inimigo a entrar\n&7em sua base irá ativar\n&7a armadilha!\n \n&7Comprar uma armadilha irá\n&7adicioná-la aqui. O custo\n&7varia de acordo com a\n&7quantia de armadilhas na fila.\n \n&7Próxima armadilha: §b" + (team
            .getTraps().size() + 1) + " Diamante" + (team.getTraps().size() + 1 > 1 ? "s" : "")).build());
      } else {
        this.setItem(slot, new ItemBuilder(trap.getIcon().replace("{color}", "&a") + "\n \n&7Esta armadilha será ativada\n&7quando o " + (index == 0 ?
          "primeiro" :
          index == 1 ? "segundo" : "terceiro") + " oponente\n&7entrar em sua base.").build());
      }
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
