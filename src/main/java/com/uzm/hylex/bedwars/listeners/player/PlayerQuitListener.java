package com.uzm.hylex.bedwars.listeners.player;

import com.uzm.hylex.bedwars.controllers.HylexPlayer;
import com.uzm.hylex.core.controllers.TagController;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener  {

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent evt) {
    evt.setQuitMessage(null);

    Player player = evt.getPlayer();
    HylexPlayer hp = HylexPlayer.remove(player);
    if (hp != null) {
      hp.save();
      hp.destroy();
    }
    TagController.delete(player);
    player.getInventory().clear();
  }
}
