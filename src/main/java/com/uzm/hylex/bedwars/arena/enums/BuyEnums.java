package com.uzm.hylex.bedwars.arena.enums;

import org.bukkit.Material;

public enum BuyEnums {
  IRON("Ferro", "§fFerro", Material.IRON_INGOT),
  DIAMOND("Diamante", "§bDiamante", Material.DIAMOND),
  EMERALD("Esmeralda", "§aEsmeralda", Material.EMERALD),
  GOLD("Ouro", "§eOuro", Material.GOLD_INGOT);

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
}
