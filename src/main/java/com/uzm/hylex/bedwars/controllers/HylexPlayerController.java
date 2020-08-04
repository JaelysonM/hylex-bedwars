package com.uzm.hylex.bedwars.controllers;

import com.uzm.hylex.core.api.Group;
import com.uzm.hylex.core.api.HylexPlayer;
import com.uzm.hylex.core.spigot.features.ActionBar;
import com.uzm.hylex.core.spigot.features.SpigotFeatures;
import com.uzm.hylex.core.spigot.items.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class HylexPlayerController {

  public static void setupHotbar(HylexPlayer hp) {
    Player player = hp.getPlayer();
    if (player != null) {
      player.setLevel((0));
      player.setExp(0);
      player.getInventory().clear();
      player.getInventory().setItem(8, new ItemBuilder(Material.BED).name("§cVoltar ao Lobby").lore("§7Clique para voltar ao Lobby.").build());

    }
  }

  public static int getLevel(HylexPlayer hp) {
    if (hp != null) {
      int total = (int) ((hp.getBedWarsStatistics().getLong("exp", "global") / 5000) + 1);
      return total <= 0 ? 1 : total;
    } else {
      return 1;
    }


  }

  public static int giveCoin(Player player, HylexPlayer hp, int baseAmount, String baseMessage) {

    float multiplier = 1;

    if (hp.getGroup().ordinal() < Group.EMERALD.ordinal()) {
      multiplier = 3.5F;
    } else if (hp.getGroup() == Group.EMERALD) {
      multiplier = 2.5F;
    } else if (hp.getGroup() == Group.DIAMOND) {
      multiplier = 2.0F;
    } else if (hp.getGroup() == Group.GOLD) {
      multiplier = 1.5F;
    }
    int finalCoins = (int) (baseAmount * multiplier);

    SpigotFeatures.sendActionBar(player, "§6+" + finalCoins + " coins");
    player.sendMessage(baseMessage.replace("%s", String.valueOf(finalCoins)) + (multiplier > 1 ? " (Multiplicador do cargo " + multiplier + "x)" : ""));


    return finalCoins;
  }

  public static int giveExp(Player player, HylexPlayer hp, int baseAmount, String baseMessage) {

    float multiplier = 1;

    if (hp.getGroup().ordinal() < Group.EMERALD.ordinal()) {
      multiplier = 3.5F;
    } else if (hp.getGroup() == Group.EMERALD) {
      multiplier = 2.5F;
    } else if (hp.getGroup() == Group.DIAMOND) {
      multiplier = 2.0F;
    } else if (hp.getGroup() == Group.GOLD) {
      multiplier = 1.5F;
    }
    int finalCoins = (int) (baseAmount * multiplier);


     player.sendMessage(baseMessage.replace("%s", String.valueOf(finalCoins)) + (multiplier > 1 ? " (Multiplicador do cargo " + multiplier + "x)" : ""));


    return finalCoins;
  }


  public static long getTotalExp(HylexPlayer hp) {
    return hp.getBedWarsStatistics().getLong("exp", "global");
  }

  public static long getExp(HylexPlayer hp) {
    return hp.getBedWarsStatistics().getLong("exp", "global") % 5000;
  }

}
