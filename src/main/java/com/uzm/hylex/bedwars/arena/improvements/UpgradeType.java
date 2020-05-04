package com.uzm.hylex.bedwars.arena.improvements;

public enum UpgradeType {
  SHARPENED_SWORDS(10, "Sharpened Swords", "Espadas Afiadas", "DIAMOND_SWORD : 1 : flags=all : display={color}Espadas Afiadas"),
  REINFORCED_ARMOR(11,"Reinforced Armor", "Armadura Reforçada", "DIAMOND_CHESTPLATE : 1 : display={color}Armadura Reforçada {tier}"),
  IRON_FORGE(12, "Iron Forge", "Forja de Ferro", "FURNACE : 1 : display={color}Forja de Ferro {tier}"),
  MANIAC_MINER(13, "Maniac Miner", "Minerador Louco", "GOLD_PICKAXE : 1 : flags=all : display={color}Minerador Louco {tier}"),
  HEAL_POOL(14, "Heal Pool", "Campo de Regeneração", "BEACON : 1 : display={color}Campo de Regeneração");

  private int slot;
  private String key;
  private String name;
  private String icon;

  UpgradeType(int slot, String key, String name, String icon) {
    this.slot = slot;
    this.key = key;
    this.name = name;
    this.icon = icon;
  }

  public int getSlot() {
    return this.slot;
  }

  public String getName() {
    return this.name;
  }

  public String getIcon() {
    return this.icon;
  }

  @Override
  public String toString() {
    return this.key;
  }
}
