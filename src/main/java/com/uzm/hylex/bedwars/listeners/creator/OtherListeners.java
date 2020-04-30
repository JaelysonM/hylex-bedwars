package com.uzm.hylex.bedwars.listeners.creator;

import com.uzm.hylex.bedwars.arena.creator.inventory.MainPainel;
import com.uzm.hylex.bedwars.controllers.HylexPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

public class OtherListeners implements Listener {

  @EventHandler
  public void onPlayerDropItem(PlayerDropItemEvent evt) {
    Player player = evt.getPlayer();
    ItemStack item = evt.getItemDrop().getItemStack();
    if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
      if (item.hasItemMeta()) {
        if (item.getItemMeta().hasDisplayName()) {
          this.cancelIfCreating(item, evt);
          if (evt.isCancelled()) {
            player.updateInventory();
          }
        }
      }
    }
  }

  @EventHandler
  public void onInventoryClick(InventoryClickEvent evt) {
    if (evt.getWhoClicked() instanceof Player) {
      Player player = (Player) evt.getWhoClicked();
      ItemStack item = evt.getCurrentItem();
      if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
        this.cancelIfCreating(item, evt);
        if (evt.isCancelled()) {
          player.updateInventory();
        }
      }
    }
  }

  @EventHandler
  public void onPlayerChangedWorld(PlayerChangedWorldEvent e) {
    HylexPlayer hp = HylexPlayer.get(e.getPlayer());

    if (hp != null && hp.getAux()) {
      new MainPainel(e.getPlayer(), hp.getAbstractArena());
      hp.setAux(false);
    }
  }

  private void cancelIfCreating(ItemStack item, Cancellable evt) {
    String displayName = item.getItemMeta().getDisplayName();
    if (displayName.equalsIgnoreCase("§aSetar posições §7(Esquerdo: Localização #1/Direito: Localização #2)")) {
      evt.setCancelled(true);
    } else if (displayName.equalsIgnoreCase("§bBordas da arena §7(Clique para alterar)")) {
      evt.setCancelled(true);
    } else if (displayName.equalsIgnoreCase("§aLocais protegidos §7(Clique para alterar)")) {
      evt.setCancelled(true);
    } else if (displayName.equalsIgnoreCase("§cAbrir configurações")) {
      evt.setCancelled(true);
    } else if (displayName.equalsIgnoreCase("§aSalvar alterações")) {
      evt.setCancelled(true);
    }
    else if (displayName.equalsIgnoreCase("§aSetar > §8Local de espera  §7(Clique para alterar)")) {
      evt.setCancelled(true);
    }
    else if (displayName.equalsIgnoreCase("§aCriar um gerador §8(Tipo definido de acordo com o bloco clicado)")) {
      evt.setCancelled(true);
    }
    else if (displayName.equalsIgnoreCase("§aSetar posições da borda da ilha §7(Esquerdo: Localização #1/Direito: Localização #2)")) {
      evt.setCancelled(true);
    }
    else if (displayName.equalsIgnoreCase("§eAdicionar gerador do time")) {
      evt.setCancelled(true);
    }
    else if (displayName.equalsIgnoreCase("§aSetar NPCs §7(Esquerdo: Altere o tipo/Direito: Loja/Direito+Shift: Melhorias)")) {
      evt.setCancelled(true);
    }
    else if (displayName.startsWith("§aConfigurando o time: ")) {
      evt.setCancelled(true);
    }
  }
}
