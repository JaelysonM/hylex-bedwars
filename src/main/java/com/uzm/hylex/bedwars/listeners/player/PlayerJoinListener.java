package com.uzm.hylex.bedwars.listeners.player;

import com.uzm.hylex.bedwars.Core;
import com.uzm.hylex.bedwars.controllers.ArenaController;
import com.uzm.hylex.bedwars.controllers.HylexPlayer;
import com.uzm.hylex.core.controllers.TagController;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerJoinListener implements Listener {

  @EventHandler(priority =  EventPriority.LOWEST)
  public void onRegister(PlayerJoinEvent evt) {
    evt.setJoinMessage(null);

    Player player = evt.getPlayer();
    HylexPlayer hp = HylexPlayer.create(player);
    hp.setupPlayer();

    if (player.hasPermission("hylex.staff") && hp.getGroup() == HylexPlayer.Group.HYLEX && ArenaController.getArenas().size() == 0) {
      player.sendMessage("");
      player.sendMessage("§e§l⚠ §aPercebemos que você é um §bHylex§a e não há nenhum 'Mini' criado nesse servidor.");
      player.sendMessage("§7Digite /arena create <mininame> para criar um arena.");
    }

    new BukkitRunnable() {

      public void run() {
        hp.requestLoad();
      }
    }.runTaskLaterAsynchronously(Core.getInstance(), 5L);
  }
}
