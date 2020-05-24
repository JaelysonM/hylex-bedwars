package com.uzm.hylex.bedwars.controllers;

import com.uzm.hylex.core.api.HylexPlayer;
import com.uzm.hylex.core.spigot.items.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class HylexPlayerController {

  public static void setupHotbar(HylexPlayer hp) {
    Player player = hp.getPlayer();
    player.setLevel((0));
    player.setExp(0);
    player.getInventory().clear();
    player.getInventory().setItem(8, new ItemBuilder(Material.BED).name("§cVoltar ao Lobby").lore("§7Clique para voltar ao Lobby.").build());
  }
  public static long getLevel(HylexPlayer hp) {
    long total = hp.getBedWarsStatistics().getLong("exp", "global")/5000;
    return total == 0?1 : total;
  }

  public static long getTotalExp(HylexPlayer hp) {
    return hp.getBedWarsStatistics().getLong("exp", "global");
  }

  public static long getExp(HylexPlayer hp) {
    return hp.getBedWarsStatistics().getLong("exp", "global")%5000;
  }

}
