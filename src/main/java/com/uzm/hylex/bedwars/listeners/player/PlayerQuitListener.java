package com.uzm.hylex.bedwars.listeners.player;

import com.uzm.hylex.bedwars.arena.player.ArenaPlayer;
import com.uzm.hylex.core.api.HylexPlayer;
import com.uzm.hylex.core.controllers.TagController;
import com.uzm.hylex.core.spigot.features.Titles;
import com.uzm.hylex.core.spigot.scoreboards.AsyncScoreboard;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener  {

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onPlayerQuit(PlayerQuitEvent evt) {
    evt.setQuitMessage(null);

    Player player = evt.getPlayer();
    HylexPlayer hp = HylexPlayer.remove(player);
    if (hp != null) {
      ArenaPlayer ap = (ArenaPlayer) hp.getArenaPlayer();
      if (ap != null) {
        ap.getArena().leave(hp);

      }
      hp.save();
      hp.destroy();
    }
    TagController.remove(player);
    player.getInventory().clear();
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onPlayerQuit(PlayerKickEvent evt) {
    Player player = evt.getPlayer();
    HylexPlayer hp = HylexPlayer.remove(player);
    if (hp != null) {
      ArenaPlayer ap = (ArenaPlayer) hp.getArenaPlayer();
      if (ap != null) {
        ap.getArena().leave(hp);

      }
      hp.save();
      hp.destroy();
    }
    TagController.remove(player);
    player.getInventory().clear();
  }

}
