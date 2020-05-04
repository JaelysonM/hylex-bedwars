package com.uzm.hylex.bedwars.listeners.player;

import com.uzm.hylex.bedwars.arena.Arena;
import com.uzm.hylex.bedwars.arena.player.ArenaPlayer;
import com.uzm.hylex.core.api.HylexPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.List;

public class PlayerDeathListener implements Listener {

  @EventHandler
  public void onPlayerDeath(PlayerDeathEvent evt) {
    Player player = evt.getEntity();
    evt.setDeathMessage(null);

    HylexPlayer hp = HylexPlayer.getByPlayer(player);
    if (hp != null) {
      evt.getDrops().clear();
      ArenaPlayer ap = (ArenaPlayer) hp.getArenaPlayer();
      if (ap != null) {
        Arena arena = ap.getArena();
        if (arena != null) {
          player.setHealth(20.0);
          List<HylexPlayer> hitters = hp.getLastHitters();
          arena.kill(hp, hitters.size() > 0 ? hitters.get(0) : null);
        }
      }
    }
  }
}
