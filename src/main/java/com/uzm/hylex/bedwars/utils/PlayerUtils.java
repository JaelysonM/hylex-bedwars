package com.uzm.hylex.bedwars.utils;

import com.google.common.collect.Lists;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Objects;

public class PlayerUtils {

  public static void giveResources(Player player, Player killer) {
    int iron = 0, gold = 0, diamond = 0, emerald = 0;
    for (ItemStack i : player.getInventory().getContents()) {
      if (i != null) {
        if (i.getType() == Material.EMERALD) {
          emerald += i.getAmount();
          killer.getInventory().addItem(i);
        } else if (i.getType() == Material.DIAMOND) {
          diamond += i.getAmount();
          killer.getInventory().addItem(i);
        } else if (i.getType() == Material.GOLD_INGOT) {
          gold += i.getAmount();
          killer.getInventory().addItem(i);
        } else if (i.getType() == Material.IRON_INGOT) {
          iron += i.getAmount();
          killer.getInventory().addItem(i);
        } else if (i.getType() == Material.FIREBALL || i.getType() == Material.TNT || i.getType() == Material.GOLDEN_APPLE || i.getType() == Material.ENDER_PEARL) {
          killer.getInventory().addItem(i);
        }
      }
    }

    if (iron > 0 || gold > 0 || diamond > 0 || emerald > 0) {
      killer.sendMessage("§aItens recebidos de " + player.getDisplayName() + "§a:");
    }
    if (iron > 0) {
      killer.sendMessage("§f+" + iron + " Ferro" + (iron > 1 ? "s" : ""));
    }
    if (gold > 0) {
      killer.sendMessage("§6+" + gold + " Ouro" + (gold > 1 ? "s" : ""));
    }
    if (diamond > 0) {
      killer.sendMessage("§b+" + diamond + " Diamante" + (diamond > 1 ? "s" : ""));
    }
    if (emerald > 0) {
      killer.sendMessage("§2+" + emerald + " Esmeralda" + (emerald > 1 ? "s" : ""));
    }
  }

  public static boolean containsSword(List<ItemStack> items) {
    return items.stream().filter(Objects::nonNull).map(ItemStack::getType).anyMatch(i -> i == Material.STONE_SWORD || i == Material.IRON_SWORD || i == Material.DIAMOND_SWORD);
  }
  public static boolean containsWoodSword(List<ItemStack> items) {
    return items.stream().filter(Objects::nonNull).map(ItemStack::getType).anyMatch(i -> i == Material.WOOD_SWORD);
  }
}
