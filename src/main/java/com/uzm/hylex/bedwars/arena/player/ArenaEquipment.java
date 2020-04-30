package com.uzm.hylex.bedwars.arena.player;

import com.uzm.hylex.bedwars.arena.shop.ShopItem;
import com.uzm.hylex.bedwars.utils.BukkitUtils;
import com.uzm.hylex.core.spigot.items.ItemBuilder;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.bukkit.Material.WOOD_SWORD;

public class ArenaEquipment {

  private int deaths = -1;
  private Player player;
  private ItemStack sword;
  private ItemStack bow;
  private ItemStack pickaxe;
  private ItemStack axe;
  private ItemStack helmet;
  private ItemStack chestplate;
  private ItemStack leggings;
  private ItemStack boots;
  private ItemStack compass;
  private String tracking;
  private short colorId;

  private List<ShopItem> shopItems = new ArrayList<>();
  private Map<ShopItem, Integer> tiers = new HashMap<>();

  public ArenaEquipment(ArenaPlayer ap) {
    this.player = ap.getPlayer();
    this.colorId = ap.getTeam().getTeamType().getColor().getWoolData();
    this.sword = new ItemStack(WOOD_SWORD);
    this.helmet = new ItemBuilder("LEATHER_HELMET : 1").color(ap.getTeam().getTeamType().getColor().getColor()).build();
    this.chestplate = new ItemBuilder("LEATHER_CHESTPLATE : 1").color(ap.getTeam().getTeamType().getColor().getColor()).build();
    this.leggings = new ItemBuilder("LEATHER_LEGGINGS : 1").color(ap.getTeam().getTeamType().getColor().getColor()).build();
    this.boots = new ItemBuilder("LEATHER_BOOTS : 1").color(ap.getTeam().getTeamType().getColor().getColor()).build();
    this.compass = new ItemBuilder("COMPASS : 1 : display=§aRastreador").build();
    this.tracking = "";
  }

  public void destroy() {
    this.player = null;
    this.sword = null;
    this.bow = null;
    this.pickaxe = null;
    this.axe = null;
    this.helmet = null;
    this.chestplate = null;
    this.leggings = null;
    this.boots = null;
    this.compass = null;
    this.tracking = null;
    this.colorId = 0;
    this.shopItems.clear();
    this.shopItems = null;
    this.tiers.clear();
    this.tiers = null;
  }

  public boolean update() {
    boolean refreshUpgrades = false;
    if (player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
      for (ItemStack stack : player.getInventory().getArmorContents()) {
        if (stack != null && !stack.getType().name().contains("AIR")) {
          refreshUpgrades = true;
          break;
        }
      }

      if (refreshUpgrades) {
        this.player.getInventory().setArmorContents(null);
        this.player.updateInventory();
      }
    } else {
      for (ItemStack stack : player.getInventory().getArmorContents()) {
        if (stack == null || stack.getType().name().contains("AIR")) {
          refreshUpgrades = true;
          break;
        }
      }

      if (refreshUpgrades) {
        this.player.getInventory().setItem(8, this.compass);
        this.player.getInventory().setHelmet(this.helmet);
        this.player.getInventory().setChestplate(this.chestplate);
        this.player.getInventory().setLeggings(this.leggings);
        this.player.getInventory().setBoots(this.boots);
        this.player.updateInventory();
      }
    }

    return refreshUpgrades;
  }

  public void setTracking(String tracking) {
    this.tracking = tracking;
  }

  public void removeTier(ShopItem item) {
    if (this.tiers.containsKey(item) && this.getTier(item) != 1) {
      this.tiers.put(item, this.getTier(item) - 2);

      this.addItem(item);
    }
  }

  public void refresh() {
    List<ShopItem> toRemove = this.shopItems.stream().filter(ShopItem::lostOnDie).collect(Collectors.toList());
    this.shopItems.removeAll(toRemove);
    toRemove.clear();

    this.deaths++;
    this.player.getInventory().setItem(0, this.sword);
    if (this.bow != null) {
      this.player.getInventory().setItem(1, this.bow);
    }
    if (this.pickaxe != null) {
      this.player.getInventory().setItem(2, this.pickaxe);
    }
    if (this.axe != null) {
      this.player.getInventory().setItem(3, this.axe);
    }
    this.player.getInventory().setItem(8, this.compass);
    this.player.getInventory().setHelmet(this.helmet);
    this.player.getInventory().setChestplate(this.chestplate);
    this.player.getInventory().setLeggings(this.leggings);
    this.player.getInventory().setBoots(this.boots);

    for (ShopItem item : shopItems) {
      removeTier(item);

      item.getContent(getTier(item)).forEach(is -> {
        if (is.getType().name().contains("WOOL") || is.getType().name().contains("STAINED_CLAY") || is.getType().name().contains("STAINED_GLASS")) {
          ItemStack clone = is.clone();
          clone.setDurability(colorId);
          player.getInventory().addItem(clone);
          return;
        }

        player.getInventory().addItem(is);
      });
    }
  }

  public void addItem(ShopItem item) {
    if (item.isTieable()) {
      this.tiers.put(item, this.getTier(item) + 1);
    }

    List<ItemStack> give = new ArrayList<>();
    for (ItemStack is : item.getContent(this.getTier(item))) {
      if (is.getType().name().contains("SWORD")) {
        if (!item.lostOnDie()) {
          this.sword = is;
        }
        BukkitUtils.replaceItem(player.getInventory(), "SWORD", is);
      } else if (is.getType().name().contains("HELMET")) {
        if (!item.lostOnDie()) {
          this.helmet = is;
        }
        player.getInventory().setHelmet(is);
      } else if (is.getType().name().contains("CHESTPLATE")) {
        if (!item.lostOnDie()) {
          this.chestplate = is;
        }
        player.getInventory().setChestplate(is);
      } else if (is.getType().name().contains("LEGGINGS")) {
        if (!item.lostOnDie()) {
          this.leggings = is;
        }
        player.getInventory().setLeggings(is);
      } else if (is.getType().name().contains("BOOTS")) {
        if (!item.lostOnDie()) {
          this.boots = is;
        }
        player.getInventory().setBoots(is);
      } else if (is.getType().name().contains("BOW")) {
        if (!item.lostOnDie()) {
          this.bow = is;
        }
        boolean bol = BukkitUtils.replaceItem(player.getInventory(), "BOW", is);
        if (!bol) {
          player.getInventory().addItem(is);
        }
      } else if (is.getType().name().contains("_PICKAXE")) {
        if (!item.lostOnDie()) {
          this.pickaxe = is;
        }
        boolean bol = BukkitUtils.replaceItem(player.getInventory(), "_PICKAXE", is);
        if (!bol) {
          player.getInventory().addItem(is);
        }
      } else if (is.getType().name().contains("_AXE")) {
        if (!item.lostOnDie()) {
          this.axe = is;
        }
        boolean bol = BukkitUtils.replaceItem(player.getInventory(), "_AXE", is);
        if (!bol) {
          player.getInventory().addItem(is);
        }
      } else {
        give.add(is);
      }
    }

    if (!this.shopItems.contains(item)) {
      this.shopItems.add(item);
    }
    item.getContent().stream().filter(give::contains).forEach(is -> {
      if (is.getType().name().contains("WOOL") || is.getType().name().contains("STAINED_CLAY") || is.getType().name().contains("STAINED_GLASS")) {
        ItemStack clone = is.clone();
        clone.setDurability(colorId);
        player.getInventory().addItem(clone);
        return;
      }

      player.getInventory().addItem(is);
    });
  }

  public boolean cantBuy(ShopItem item) {
    boolean anyBlockThat = this.shopItems.stream().anyMatch(si -> si.getCategory().equals(item.getCategory()) && si.isBlocked(item));
    if (anyBlockThat) {
      return true;
    }

    boolean alreadyHaves = this.shopItems.contains(item);
    if (alreadyHaves && item.isTieable()) {
      return item.getMaxTier() == this.getTier(item);
    }

    return alreadyHaves && !item.lostOnDie();
  }

  public boolean contains(ShopItem item) {
    return this.shopItems.contains(item) && (!item.isTieable() || item.getMaxTier() == this.getTier(item));
  }

  public int getTier(ShopItem item) {
    return this.tiers.getOrDefault(item, 0);
  }

  public String getTracking() {
    return this.tracking;
  }

  public int getDeaths() {
    return this.deaths;
  }
}
