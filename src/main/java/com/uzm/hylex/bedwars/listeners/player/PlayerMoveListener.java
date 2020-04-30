package com.uzm.hylex.bedwars.listeners.player;

import com.uzm.hylex.bedwars.arena.Arena;
import com.uzm.hylex.bedwars.arena.player.ArenaPlayer;
import com.uzm.hylex.bedwars.controllers.HylexPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import static com.uzm.hylex.bedwars.arena.enums.ArenaEnums.ArenaState.IN_GAME;

public class PlayerMoveListener implements Listener {

  @EventHandler
  public void onPlayerMove(PlayerMoveEvent evt) {
    Player player = evt.getPlayer();

    HylexPlayer hp = HylexPlayer.get(player);
    if (hp != null) {
      ArenaPlayer ap = hp.getArenaPlayer();
      if (ap != null) {
        Arena arena = ap.getArena();
        if (arena != null && arena.getState() == IN_GAME) {
          arena.getTeams().values().stream()
            .filter(t -> t.getBorder().contains(player.getLocation()) && !player.equals(t.getLastTrapped()))
            .findFirst()
            .ifPresent(team -> team.getTraps().stream()
              .findFirst()
              .ifPresent(trap -> trap.onEnter(team, ap)));
        }
      }
    }
  }
}
