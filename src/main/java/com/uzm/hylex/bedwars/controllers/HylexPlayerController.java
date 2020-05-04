package com.uzm.hylex.bedwars.controllers;

import com.uzm.hylex.core.api.HylexPlayer;
import com.uzm.hylex.core.spigot.items.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class HylexPlayerController {

  public static void setupHotbar(HylexPlayer hp) {
    Player player = hp.getPlayer();
    player.getInventory().setItem(8, new ItemBuilder(Material.BED).name("§cVoltar ao Lobby").lore("§7Clique para voltar ao Lobby.").build());
  }
}
