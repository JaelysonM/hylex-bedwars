package com.uzm.hylex.bedwars.arena.shop;

import com.uzm.hylex.bedwars.Core;
import com.uzm.hylex.core.java.util.configuration.ConfigurationCreator;
import com.uzm.hylex.core.spigot.items.ItemBuilder;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ShopCategory {

  private String name;
  private String key;
  private ItemStack icon;
  private List<ShopItem> items;

  public ShopCategory(String key) {
    FileConfiguration config = ConfigurationCreator.find("itemshop", Core.getInstance()).get();
    this.key=key;
    this.name = config.getString("categories." + key + ".name");
    this.icon = new ItemBuilder(config.getString("categories." + key + ".icon")).build();
    this.items = new ArrayList<>();

    ConfigurationSection section = config.getConfigurationSection("categories." + key + ".items");
    for (String item : section.getKeys(false)) {
      boolean lostOnDie = section.getBoolean(item + ".lostOnDie", true);
      String icon2 = section.getString(item + ".icon");
      List<String> blocks = new ArrayList<>();
      if (section.getStringList(item + ".block") != null) {
        blocks.addAll(section.getStringList(item + ".block"));
      }

      if (!section.contains(item + ".price")) {
        if (!section.contains(item + ".tiers")) {
          continue;
        }

        List<ShopItem.ShopItemTier> tiers = new ArrayList<>();
        for (String tier : section.getConfigurationSection(item + ".tiers").getKeys(false)) {
          ItemStack price = new ItemBuilder(section.getString(item + ".tiers." + tier + ".price")).build();
          String name = section.getString(item + ".tiers." + tier + ".name");
          List<ItemStack> content = new ArrayList<>();
          for (String stack : section.getStringList(item + ".tiers." + tier + ".content")) {
            content.add(new ItemBuilder(stack).build());
          }

          tiers.add(new ShopItem.ShopItemTier(price, content, name));
        }

        this.items.add(new ShopItem(this, item, lostOnDie, icon2, null, null, blocks, tiers));
        continue;
      }

      ItemStack price = new ItemBuilder(section.getString(item + ".price")).build();
 

      this.items.add(new ShopItem(this, item, lostOnDie, icon2, price, section.getStringList(item + ".content"), blocks, null));
    }
  }


  public String getKey() {
    return key;
  }

  public String getName() {
    return this.name;
  }

  public ItemStack getIcon() {
    return this.icon;
  }

  public List<ShopItem> listItems() {
    return this.items;
  }

  public ShopItem getItem(String item) {
    return listItems().stream().filter(si -> si.getName().equals(item)).findFirst().orElse(null);
  }
}
