package com.uzm.hylex.bedwars.menus;

import com.uzm.hylex.bedwars.arena.player.ArenaPlayer;
import com.uzm.hylex.bedwars.arena.shop.Shop;
import com.uzm.hylex.bedwars.arena.shop.ShopCategory;
import com.uzm.hylex.bedwars.arena.shop.ShopItem;
import com.uzm.hylex.core.Core;
import com.uzm.hylex.core.api.HylexPlayer;
import com.uzm.hylex.core.api.container.BedWarsPreferencesContainer;
import com.uzm.hylex.core.spigot.inventories.PlayerMenu;
import com.uzm.hylex.core.spigot.items.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.Sound;
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

public class SelectionMenu extends PlayerMenu {

  @EventHandler
  public void onInventoryClick(InventoryClickEvent evt) {
    if (evt.getInventory().equals(getInventory())) {
      evt.setCancelled(true);

      if (evt.getWhoClicked() instanceof Player && evt.getWhoClicked().equals(player)) {
        ItemStack item = evt.getCurrentItem();
        HylexPlayer hp = HylexPlayer.getByPlayer(player);

        if (hp == null) {
          player.closeInventory();
          return;
        }

        if (evt.getClickedInventory() != null && evt.getClickedInventory().equals(evt.getInventory()) && item != null && item.getType() != Material.AIR) {
          if (SLOTS.contains(evt.getSlot())) {
            if (item.getDurability() == 13) {
              hp.getBedWarsPreferences().setQuickBuy(evt.getSlot(), Shop.getCategoryId(this.item.getCategory()) + ":" + this.item.getName());
              player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1.0F, 1.0F);
              new ItemShopMenu(this.ap, null);
            }
          }
        }
      }
    }
  }

  private static final List<Integer> SLOTS = Arrays.asList(19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43);

  private ArenaPlayer ap;
  private ShopItem item;

  public SelectionMenu(ArenaPlayer ap, ShopItem item, ItemStack stack) {
    super(ap.getPlayer(), "Selecione um slot...", 6);
    this.ap = ap;
    this.item = item;

    HylexPlayer hp = HylexPlayer.getByPlayer(player);

    this.setItem(4, stack);

    for (int i = 0; i < 9; i++) {
      this.setItem(9 + i, new ItemBuilder("STAINED_GLASS_PANE:7 : 1 : display=&8↑ &7Item : lore=&8↓ &7Slots").build());
    }

    BedWarsPreferencesContainer preferences = hp.getBedWarsPreferences();
    for (int slot : SLOTS) {
      if (preferences.hasQuickBuy(slot)) {
        String fav = preferences.getQuickBuy(slot);
        ShopItem favItem = Shop.getCategoryById(Integer.parseInt(fav.split(":")[0])).getItem(fav.split(":")[1]);
        ItemStack icon = new ItemBuilder(favItem.getIcon().replace("{color}", "§a").replace("{price}", String.valueOf(item.getPrice().getAmount())).replace("{tier}", "I")).build();
        ItemMeta meta = icon.getItemMeta();
        List<String> lore = meta.getLore();
        lore.clear();
        lore.add("§7Este slot já está sendo");
        lore.add("§7utilizado por algum item!");
        lore.add("");
        lore.add("§7Tente clicar apenas no");
        lore.add("§7slots em §2verde§7.");
        meta.setLore(lore);
        icon.setItemMeta(meta);
        this.setItem(slot, icon);
      } else {
        this.setItem(slot, new ItemBuilder(
          "STAINED_GLASS_PANE:13 : 1 : display=&aUtilizar este slot : lore=§7Ao clicar neste vidro você\n§7irá colocar o item neste slot.\n \n§eClique para utilizar este slot!")
          .build());
      }
    }

    this.open();
    this.register(Core.getInstance());
  }


  public void cancel() {
    this.ap = null;
    this.item = null;
    HandlerList.unregisterAll(this);
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent evt) {
    if (evt.getPlayer().equals(this.player)) {
      this.cancel();
    }
  }

  @EventHandler
  public void onInventoryClose(InventoryCloseEvent evt) {
    if (evt.getPlayer().equals(this.player) && evt.getInventory().equals(this.getInventory())) {
      this.cancel();
    }
  }
}

