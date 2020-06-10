package com.uzm.hylex.bedwars.arena.shop;

import com.uzm.hylex.bedwars.arena.enums.BuyEnums;
import com.uzm.hylex.core.spigot.items.ItemBuilder;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.stream.Collectors;

public class ShopItem {

  private ShopCategory category;
  private String name;
  private boolean lostOnDie;
  private String icon;
  private ItemStack price;
  private List<String> content;
  private List<String> block;
  private List<ShopItemTier> tiers;
  private BuyEnums coinTrade;

  public ShopItem(ShopCategory category, String name, boolean lostOnDie, String icon, ItemStack price, List<String> content, List<String> block, List<ShopItemTier> tiers) {
    this.category = category;
    this.name = name;
    this.lostOnDie = lostOnDie;
    this.icon = icon;
    this.price = price;
    this.content = content;
    this.block = block;
    this.tiers = tiers;
    if (this.price != null) {
      for (BuyEnums b : BuyEnums.values()) {
        if (b.getMaterial() == price.getType()) {
          this.coinTrade = b;
          break;
        }
      }
    }

  }

  public ShopCategory getCategory() {
    return category;
  }

  public String getName() {
    return name;
  }

  public boolean lostOnDie() {
    return lostOnDie;
  }

  public String getIcon() {
    return icon;
  }

  public ItemStack getPrice() {
    return isTieable() ? getTier(1).getPrice() : price;
  }

  public BuyEnums getCoinTrade() {
    return coinTrade;
  }

  public ItemStack getPrice(int tier) {
    return isTieable() ? getTier(tier).getPrice() : price;
  }

  public List<ItemStack> getContent() {
    return this.getContent(1);
  }

  public boolean isBlocked(ShopItem item) {
    return this.block.contains(item.getName());
  }

  public List<ItemStack> getContent(int tier) {
    return isTieable() ? getTier(tier).getContent() : content.stream().map(s -> new ItemBuilder(s).build()).collect(Collectors.toList());
  }

  public boolean isTieable() {
    return tiers != null && tiers.size() > 0;
  }

  public ShopItemTier getTier(int tier) {
    return tiers.size() == tier ? tiers.get(tiers.size() - 1) : tiers.get(tier - 1);
  }

  public int getMaxTier() {
    return tiers.size();
  }

  public static class ShopItemTier {

    private ItemStack price;
    private List<ItemStack> content;
    private String name;
    private BuyEnums coinTrade;

    public ShopItemTier(ItemStack price, List<ItemStack> content, String name) {
      this.price = price;
      this.content = content;
      this.name = name;
      for (BuyEnums b : BuyEnums.values()) {
        if (b.getMaterial() == price.getType()) {
          this.coinTrade = b;
          break;
        }
      }
    }

    public BuyEnums getCoinTrade() {
      return coinTrade;
    }

    public String getName() {
      return name;
    }

    public ItemStack getPrice() {
      return price;
    }

    public List<ItemStack> getContent() {
      return content;
    }
  }
}
