package com.uzm.hylex.bedwars.arena.team;

import com.uzm.hylex.bedwars.arena.Arena;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;

import java.util.Arrays;
import java.util.List;

public enum Teams {

  RED("Vermelho", "§cVermelho", Color.fromRGB(255, 0, 0), DyeColor.RED, "a"),
  BLUE("Azul", "§9Azul", Color.fromRGB(0, 0, 255), DyeColor.BLUE, "b"),
  GREEN("Verde", "§aVerde", Color.fromRGB(0, 255, 0), DyeColor.LIME, "c"),
  YELLOW("Amarelo", "§eAmarelo", Color.fromRGB(255, 255, 0), DyeColor.YELLOW, "d"),
  AQUA("Ciano", "§bCiano", Color.fromRGB(173, 216, 230), DyeColor.CYAN, "e"),
  WHITE("Branco", "§fBranco", Color.fromRGB(255, 255, 255), DyeColor.WHITE, "f"),
  PINK("Rosa", "§dRosa", Color.fromRGB(255, 192, 203), DyeColor.PINK, "g"),
  GREY("Cinza", "§8Cinza", Color.fromRGB(105, 105, 105), DyeColor.GRAY, "h");

  private String name;
  private String displayName;
  private Color colorRGB;
  private DyeColor color;
  private String order;

  Teams(String name, String displayName, Color colorRGB, DyeColor color, String order) {
    this.name = name;
    this.displayName = displayName;
    this.colorRGB = colorRGB;
    this.color = color;
    this.order = order;
  }

  public String getOrder() {
    return order;
  }

  public String getName() {
    return this.name;
  }

  public DyeColor getColor() {
    return this.color;
  }

  public Color getColorRGB() {
    return colorRGB;
  }

  public String getTagColor() {
    return ChatColor.getLastColors(this.displayName);
  }

  public String getDisplayName() {
    return this.displayName;
  }

  public String getScoreboardName() {
    return this.getTagColor() + "§l" + this.displayName.substring(2, 3) + this.getTagColor();
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

  public Teams next(Arena arena) {
    return (TEAMS.indexOf(this) + 1) == arena.getConfiguration().getIslands() ? RED : TEAMS.get((TEAMS.indexOf(this) + 1));
  }
}
