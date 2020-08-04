package com.uzm.hylex.bedwars.arena.player;

import com.google.common.collect.Lists;
import com.uzm.hylex.bedwars.Core;
import com.uzm.hylex.bedwars.arena.improvements.UpgradeType;
import com.uzm.hylex.bedwars.arena.shop.ShopItem;
import com.uzm.hylex.bedwars.arena.team.Teams;
import com.uzm.hylex.bedwars.utils.PlayerUtils;
import com.uzm.hylex.core.api.HylexPlayer;
import com.uzm.hylex.core.spigot.items.ItemBuilder;
import com.uzm.hylex.core.spigot.utils.BukkitUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.bukkit.Material.STONE;
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
  private Teams tracking;
  private short colorId;

  private List<ShopItem> shopItems = new ArrayList<>();
  private Map<ShopItem, Integer> tiers = new HashMap<>();

  public ArenaEquipment(ArenaPlayer ap) {
    this.player = ap.getPlayer();
    this.colorId = ap.getTeam().getTeamType().getColor().getWoolData();
    this.sword = new ItemStack(WOOD_SWORD);
    this.helmet = new ItemBuilder("LEATHER_HELMET : 1").color(ap.getTeam().getTeamType().getColorRGB()).build();
    this.chestplate = new ItemBuilder("LEATHER_CHESTPLATE : 1").color(ap.getTeam().getTeamType().getColorRGB()).build();
    this.leggings = new ItemBuilder("LEATHER_LEGGINGS : 1").color(ap.getTeam().getTeamType().getColorRGB()).build();
    this.boots = new ItemBuilder("LEATHER_BOOTS : 1").color(ap.getTeam().getTeamType().getColorRGB()).build();
    this.compass = new ItemBuilder("COMPASS : 1 : display=§aRastreador").build();
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
    if (this.shopItems !=null)
    this.shopItems.clear();
    this.shopItems = null;
    if (this.tiers !=null)
    this.tiers.clear();
    this.tiers = null;
  }

  public void setDisableInvisibility( boolean b) {
    this.disableInvisibility=b;
  }


  private boolean disableInvisibility;

  public boolean update() {
    if (player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
      this.disableInvisibility = true;
      return true;
    }

    return false;
  }

  public boolean isDisableInvisibility() {
    boolean sendArmor = this.disableInvisibility;
    this.disableInvisibility = false;
    return sendArmor;
  }

  public void setTracking(Teams tracking) {
    this.tracking = tracking;
  }

  public void removeTier(ShopItem item) {
    if (this.tiers.containsKey(item) && this.getTier(item) != 1) {
      this.tiers.put(item, this.getTier(item) - 2);

      this.addItem(item);
    }
  }

  public void refresh() {
    this.player.getInventory().clear();
    this.player.getInventory().setArmorContents(new ItemStack[4]);

    List<ShopItem> toRemove = this.shopItems.stream().filter(ShopItem::lostOnDie).collect(Collectors.toList());
    this.shopItems.removeAll(toRemove);
    toRemove.clear();

    this.tracking = null;
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
        if (!is.getType().name().contains("SWORD") && !is.getType().name().contains("HELMET") && !is.getType().name().contains("CHESTPLATE") && !is.getType().name()
          .contains("LEGGINGS") && !is.getType().name().contains("BOOTS") && !is.getType().name().contains("BOW") && !is.getType().name().contains("AXE")) {
          if (is.getType().name().contains("WOOL") || is.getType().name().contains("STAINED_CLAY") || is.getType().name().contains("STAINED_GLASS")) {
            ItemStack clone = is.clone();
            clone.setDurability(colorId);
            if (clone.getType() != STONE)
            this.player.getInventory().addItem(clone);
            return;
          }
          if (is.clone().getType() != STONE)
          this.player.getInventory().addItem(is.clone());
        }
      });
    }

    this.player.updateInventory();
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
        if (PlayerUtils.containsWoodSword(Lists.newArrayList(player.getInventory().getContents()))) {
          BukkitUtils.replaceItem(player.getInventory(), "SWORD", is);
        } else {
          player.getInventory().addItem(is);
        }


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

      player.getInventory().addItem(is.clone());
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

  public Teams getTracking() {
    return this.tracking;
  }

  public int getDeaths() {
    return this.deaths;
  }


  public static void woodSword() {
    Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(Core.getInstance(), () -> {
      for (Player pls : Bukkit.getOnlinePlayers()) {
        HylexPlayer hp = HylexPlayer.getByPlayer(pls);
        ArenaPlayer ap = (ArenaPlayer) hp.getArenaPlayer();
        if (ap != null) {
          if (ap.getCurrentState() != null) {
            if (ap.getCurrentState().isInGame()) {
              if (PlayerUtils.containsSword(Lists.newArrayList(pls.getInventory().getContents())) && pls.getItemOnCursor().getType() != WOOD_SWORD) {
                if (pls.getInventory().contains(WOOD_SWORD))
                  pls.getInventory().remove(WOOD_SWORD);
              } else {
                if (!pls.getInventory().contains(WOOD_SWORD) && pls.getItemOnCursor().getType() != WOOD_SWORD) {
                  if (ap.getTeam() != null) {
                    if (ap.getTeam().hasUpgrade(UpgradeType.SHARPENED_SWORDS)) {
                      pls.getInventory().addItem(new ItemBuilder(Material.WOOD_SWORD).enchant(Enchantment.DAMAGE_ALL, ap.getTeam().getTier(UpgradeType.SHARPENED_SWORDS)).build());
                      pls.updateInventory();
                    } else {
                      pls.getInventory().addItem(new ItemBuilder(Material.WOOD_SWORD).build());

                    }
                  }
                }
              }
            }
          }
        }
      }
    }, 0, 20L);
  }
}
