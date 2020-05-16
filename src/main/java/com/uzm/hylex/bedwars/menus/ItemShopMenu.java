package com.uzm.hylex.bedwars.menus;

import com.uzm.hylex.bedwars.arena.player.ArenaEquipment;
import com.uzm.hylex.bedwars.arena.player.ArenaPlayer;
import com.uzm.hylex.bedwars.arena.shop.Shop;
import com.uzm.hylex.bedwars.arena.shop.ShopCategory;
import com.uzm.hylex.bedwars.arena.shop.ShopItem;
import com.uzm.hylex.bedwars.arena.team.Team;
import com.uzm.hylex.core.Core;
import com.uzm.hylex.core.api.HylexPlayer;
import com.uzm.hylex.core.api.container.BedWarsPreferencesContainer;
import com.uzm.hylex.core.java.util.StringUtils;
import com.uzm.hylex.core.spigot.inventories.PlayerMenu;
import com.uzm.hylex.core.spigot.items.ItemBuilder;
import com.uzm.hylex.core.spigot.utils.BukkitUtils;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemShopMenu extends PlayerMenu {

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

        Team team = this.ap.getTeam();
        if (evt.getClickedInventory() != null && evt.getClickedInventory().equals(evt.getInventory()) && item != null && item.getType() != Material.AIR) {
          ShopItem si;
          ShopCategory category = categories.get(evt.getSlot());
          if (category != null) {
            if (category != this.category) {
              new ItemShopMenu(this.ap, category);
            }
          } else if ((si = items.get(item)) != null) {
            ArenaEquipment equipment = ap.getEquipment();
            boolean have = equipment.cantBuy(si);
            boolean maxTier = si.isTieable() && equipment.getTier(si) >= si.getMaxTier();
            int nextTier = maxTier ? si.getMaxTier() : equipment.getTier(si) + 1;
            if (evt.getClick().name().contains("SHIFT")) {
              BedWarsPreferencesContainer preferences = hp.getBedWarsPreferences();
              if (this.category == null && preferences.hasQuickBuy(evt.getSlot())) {
                preferences.setQuickBuy(evt.getSlot(), null);
                player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1.0F, 1.0F);
                new ItemShopMenu(this.ap, this.category);
              } else if (this.category != null) {
                int categoryId = Shop.getCategoryId(this.category);
                if (preferences.hasQuickBuy(categoryId + ":" + si.getName())) {
                  preferences.setQuickBuy(preferences.getQuickBuy(categoryId + ":" + si.getName()), null);
                  player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1.0F, 1.0F);
                  new ItemShopMenu(this.ap, this.category);
                } else {
                  new SelectionMenu(ap, si, item);
                }
              }
              return;
            }

            if (!have) {
              if (BukkitUtils.getCountFromMaterial(player.getInventory(), si.getPrice(nextTier).getType()) < si.getPrice(nextTier).getAmount()) {
                player.sendMessage("§cVocê não possui recursos suficientes.");
                return;
              }

              BukkitUtils.removeItem(player.getInventory(), si.getPrice(nextTier).getType(), si.getPrice(nextTier).getAmount());

              if (team != null) {
                equipment.addItem(si);
                this.ap.refresh();
              }

              player.sendMessage("§aVocê comprou §6" + StringUtils.stripColors(item.getItemMeta().getDisplayName()));
              new ItemShopMenu(this.ap, this.category);
            }
          } else if (evt.getSlot() == 0) {
            new ItemShopMenu(this.ap, null);
          }
        }
      }
    }
  }

  private static final List<Integer> SLOTS = Arrays.asList(19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43);

  private ArenaPlayer ap;
  private ShopCategory category;
  private Map<ItemStack, ShopItem> items = new HashMap<>();
  private Map<Integer, ShopCategory> categories = new HashMap<>();

  public ItemShopMenu(ArenaPlayer ap, ShopCategory category) {
    super(ap.getPlayer(), category == null ? "Compra fazt" : category.getName(), 6);
    this.ap = ap;
    this.category = category;

    HylexPlayer hp = HylexPlayer.getByPlayer(player);

    int id = 1;
    this.setItem(0, new ItemBuilder(Material.NETHER_STAR).name("§bCompra fazt").build());
    for (ShopCategory sc : Shop.listCategories()) {
      this.setItem(id, sc.getIcon());
      this.categories.put(id++, sc);
    }

    int categoryId = Shop.getCategoryId(category);
    for (int i = 0; i < 9; i++) {
      this.setItem(9 + i, new ItemBuilder("STAINED_GLASS_PANE:" + (i == categoryId ? "13" : "7") + " : 1 : display=&8↑ &7Categorias : lore=&8↓ &7Itens").build());
    }

    BedWarsPreferencesContainer preferences = hp.getBedWarsPreferences();
    List<ShopItem> items = category == null ? null : category.listItems();
    if (category == null) {
      SLOTS.forEach(slot -> {
        if (!preferences.hasQuickBuy(slot)) {
          this.setItem(slot, new ItemBuilder("STAINED_GLASS_PANE:14 : 1 : display=&cSlot vazio! : lore=&7Esse é um slot de Compra fazt!\n&bShift Clique &7em qualquer item na\n&7loja para adicioná-lo aqui.").build());
          return;
        }

        String fav = preferences.getQuickBuy(slot);
        ShopCategory favCategory = Shop.getCategoryById(Integer.parseInt(fav.split(":")[0]));
        if (favCategory != null) {
          ShopItem item = favCategory.getItem(fav.split(":")[1]);
          if (item != null) {
            ArenaEquipment equipment = ap.getEquipment();
            boolean maxTier = item.isTieable() && equipment.getTier(item) >= item.getMaxTier();
            int nextTier = maxTier ? item.getMaxTier() : equipment.getTier(item) + 1;
            String color = BukkitUtils.getCountFromMaterial(player.getInventory(), item.getPrice(nextTier).getType()) < item.getPrice(nextTier).getAmount() ? "&c" : "&a";

            ItemStack icon = new ItemBuilder(item.getIcon().replace("{color}", color).replace("{price}", String.valueOf(item.getPrice(nextTier).getAmount()))
              .replace("{tier}", nextTier > 3 ? nextTier == 4 ? "IV" : "V" : StringUtils.repeat("I", nextTier))).build();
            if (item.isTieable()) {
              icon.setType(item.getTier(nextTier).getContent().get(0).getType());
            }
            ItemMeta meta = icon.getItemMeta();
            List<String> lore = meta.getLore();
            lore.add("");
            lore.add("§bShift clique para remover");
            lore.add("§bna compra fazt!");
            lore.add("");
            if (equipment.cantBuy(item)) {
              lore.add("§cVocê já possui este item!");
            } else if ("&c".equals(color)) {
              lore.add("§cVocê não possui recursos suficientes!");
            } else {
              lore.add("§eClique para comprar!");
            }
            meta.setLore(lore);
            icon.setItemMeta(meta);
            this.setItem(slot, icon);
            this.items.put(icon, item);
            return;
          }
        }

        preferences.setQuickBuy(slot, null);
      });
    } else {
      for (int index = 0; index < SLOTS.size(); index++) {
        if (items.size() == index) {
          break;
        }

        ShopItem item = items.get(index);
        ArenaEquipment equipment = ap.getEquipment();
        boolean maxTier = item.isTieable() && equipment.getTier(item) >= item.getMaxTier();
        int nextTier = maxTier ? item.getMaxTier() : equipment.getTier(item) + 1;
        String color = BukkitUtils.getCountFromMaterial(player.getInventory(), item.getPrice(nextTier).getType()) < item.getPrice(nextTier).getAmount() ? "&c" : "&a";

        ItemStack icon = new ItemBuilder(item.getIcon().replace("{color}", color).replace("{price}", String.valueOf(item.getPrice(nextTier).getAmount()))
          .replace("{tier}", nextTier > 3 ? nextTier == 4 ? "IV" : "V" : StringUtils.repeat("I", nextTier))).build();
        if (item.isTieable()) {
          icon.setType(item.getTier(nextTier).getContent().get(0).getType());
        }
        ItemMeta meta = icon.getItemMeta();
        List<String> lore = meta.getLore();
        lore.add("");
        if (preferences.hasQuickBuy(categoryId + ":" + item.getName())) {
          lore.add("§bShift clique para remover");
        } else {
          lore.add("§bShift clique para adicionar");
        }
        lore.add("§bna compra fazt!");
        lore.add("");
        if (equipment.cantBuy(item)) {
          lore.add("§cVocê já possui este item!");
        } else if ("&c".equals(color)) {
          lore.add("§cVocê não possui recursos suficientes!");
        } else {
          lore.add("§eClique para comprar!");
        }
        meta.setLore(lore);
        icon.setItemMeta(meta);
        this.setItem(SLOTS.get(index), icon);
        this.items.put(icon, item);
      }
    }

    this.open();
    this.register(Core.getInstance());
  }

  public void cancel() {
    this.ap = null;
    this.category = null;
    this.items.clear();
    this.items = null;
    this.categories.clear();
    this.categories = null;
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
