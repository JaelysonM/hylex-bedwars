package com.uzm.hylex.bedwars.listeners.player;

import com.uzm.hylex.bedwars.arena.Arena;
import com.uzm.hylex.bedwars.arena.player.ArenaPlayer;
import com.uzm.hylex.bedwars.controllers.HylexPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.List;

import static com.uzm.hylex.bedwars.arena.player.ArenaPlayer.CurrentState.IN_GAME;

public class PlayerDeathListener implements Listener {

  @EventHandler
  public void onPlayerDeath(PlayerDeathEvent evt) {
    Player player = evt.getEntity();
    evt.setDeathMessage(null);

    HylexPlayer hp = HylexPlayer.get(player);
    if (hp != null) {
      evt.getDrops().clear();
      ArenaPlayer ap = hp.getArenaPlayer();
      if (ap != null) {
        Arena arena = ap.getArena();
        if (arena != null) {
          player.setHealth(20.0);
          List<HylexPlayer> hitters = hp.getLastHitters();
          HylexPlayer killer = hitters.size() > 0 ? hitters.get(0) : null;
          // TODO: matar jogador
          // arena.kill(hp, killer);
          for (HylexPlayer hitter : hitters) {
            if (hitter != null && hitter.getPlayer() != null && !hitter.equals(killer) && (hitter.getArenaPlayer() != null && arena
              .equals(hitter.getArenaPlayer().getArena())) && hitter.getArenaPlayer().getCurrentState() == IN_GAME) {
              // TODO: adicionar assistência
            }
          }
        }
      }
    }
  }
}
