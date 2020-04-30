package com.uzm.hylex.bedwars.arena.team;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;

import java.util.Arrays;
import java.util.List;

public enum Teams {

  RED("Vermelho", "§cVermelho", DyeColor.RED),
  BLUE("Azul", "§9Azul", DyeColor.BLUE),
  GREEN("Verde", "§aVerde", DyeColor.GREEN),
  YELLOW("Amarelo", "§eAmarelo", DyeColor.YELLOW),
  AQUA("Azul claro", "§bAzul claro", DyeColor.LIGHT_BLUE),
  WHITE("Branco", "§fBranco", DyeColor.WHITE),
  PINK("Rosa", "§dRosa", DyeColor.PINK),
  BLACK("Preto", "§8Preto", DyeColor.BLACK);

  private String name;
  private String displayName;
  private DyeColor color;

  Teams(String name, String displayName, DyeColor color) {
    this.name = name;
    this.displayName = displayName;
    this.color = color;
  }

  public String getName() {
    return this.name;
  }

  public DyeColor getColor() {
    return this.color;
  }

  public String getDisplayName() {
    return this.displayName;
  }

  public String getScoreboardName() {
    return ChatColor.getLastColors(this.displayName) + "§l" + this.displayName.substring(2, 3);
  }

  private static final List<Teams> TEAMS;

  static {
    TEAMS = Arrays.asList(values());
  }

  public static Teams getByData(short data) {
    for (Teams team : TEAMS) {
      if (team.getColor().getData() == data) {
        return team;
      }
    }

    return null;
  }

  public Teams next() {
    return (TEAMS.indexOf(this) + 1) == TEAMS.size() ? RED : TEAMS.get((TEAMS.indexOf(this) + 1));
  }
}
