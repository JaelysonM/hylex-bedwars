package com.uzm.hylex.bedwars.arena.improvements;

public enum UpgradeType {
  SHARPENED_SWORDS("", "Espadas Afiadas"),
  REINFORCED_ARMOR("", "Armadura Reforçada"),
  IRON_FORGE("", "Forja de Ferro"),
  MANIAC_MINER("", "Minerador Louco"),
  HEAL_POOL("", "Campo de Regeneração");

  private String key;
  private String name;


  UpgradeType(String key, String name) {
    this.key = key;
    this.name = name;
  }

  public String getName() {
    return this.name;
  }

  @Override
  public String toString() {
    return this.key;
  }
}
