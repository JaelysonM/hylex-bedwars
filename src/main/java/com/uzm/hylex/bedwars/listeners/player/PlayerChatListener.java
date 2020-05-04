package com.uzm.hylex.bedwars.listeners.player;

import com.uzm.hylex.bedwars.arena.Arena;
import com.uzm.hylex.bedwars.arena.player.ArenaPlayer;
import com.uzm.hylex.core.api.HylexPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Set;

import static com.uzm.hylex.core.api.interfaces.Enums.ArenaState.IN_GAME;

public class PlayerChatListener implements Listener {

  @EventHandler
  public void onChat(AsyncPlayerChatEvent evt) {
    Set<Player> r = evt.getRecipients();
    Player player = evt.getPlayer();
    HylexPlayer hp = HylexPlayer.getByPlayer(player);
    if (player.hasPermission("hylex.colorchat")) {
      evt.setMessage(ChatColor.translateAlternateColorCodes('&', evt.getMessage()));
    }

    if (hp != null) {
      ArenaPlayer ap = (ArenaPlayer) hp.getArenaPlayer();
      if (ap != null) {
        if (ap.getArena() != null) {
          for (Player pls : Bukkit.getServer().getOnlinePlayers()) {
            HylexPlayer hps = HylexPlayer.getByPlayer(pls);
            if (hps != null) {
              ArenaPlayer aps = (ArenaPlayer) hps.getArenaPlayer();
              if (!ap.getArena().equals(aps.getArena())) {
                r.remove(pls);
              } else {
                Arena arena = ap.getArena();
                if (arena.getState() == IN_GAME) {
                  if (ap.getCurrentState().isSpectating() && !aps.getCurrentState().isSpectating()) {
                    r.remove(pls);
                  } else if (arena.getConfiguration().getTeamsSize() > 1 && !ap.getTeam().equals(aps.getTeam())) {
                    r.remove(pls);
                  }
                }
              }
            }
          }
        }

        if (!evt.isCancelled()) {
          if (ap.getArena() != null) {
            Arena arena = ap.getArena();
            if (arena != null) {
              if (arena.getState() == IN_GAME) {
                if (ap.getCurrentState().isSpectating()) {
                  evt.setFormat("§7[ESPECTADOR] %s§7: %s");
                } else {
                  evt.setFormat("§7[" + ap.getTeam().getTeamType().getDisplayName().toUpperCase() + "§7] " + "%s§7: %s");
                }
              } else {
                evt.setFormat("%s§7: %s");
              }
            }
          } else {
            evt.setFormat("§8[L]§r %s§7: %s");
          }
        }
      }
    }
  }
}
