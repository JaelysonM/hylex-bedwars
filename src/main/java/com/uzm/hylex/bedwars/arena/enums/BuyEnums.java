package com.uzm.hylex.bedwars.arena.enums;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public enum BuyEnums {
  IRON("Ferro", "§7Ferro", Material.IRON_INGOT),
  DIAMOND("Diamante", "§bDiamante", Material.DIAMOND),
  EMERALD("Esmeralda", "§aEsmeralda", Material.DIAMOND),
  GOLD("Ouro", "§eOuro", Material.DIAMOND);

  private String name;
  private String displayName;
  private Material material;

  BuyEnums(String name, String displayName, Material material) {
    this.name = name;
    this.displayName = displayName;
    this.material = material;
  }

  public Material getMaterial() {
    return material;
  }

  public String getName() {
    return name;
  }

  public String getDisplayName() {
    return displayName;
  }

  public boolean buy(Player player, int price) {
    if (player.getInventory().contains(getMaterial(), price)) {
      player.getInventory().removeItem(new ItemStack(getMaterial(), price));
      return true;
    } else {
      return false;
    }
  }
}
