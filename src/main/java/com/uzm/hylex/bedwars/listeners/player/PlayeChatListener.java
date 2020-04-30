package com.uzm.hylex.bedwars.listeners.player;

import com.uzm.hylex.bedwars.arena.Arena;
import com.uzm.hylex.bedwars.arena.enums.ArenaEnums;
import com.uzm.hylex.bedwars.arena.player.ArenaPlayer;
import com.uzm.hylex.bedwars.controllers.HylexPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Set;

public class PlayeChatListener implements Listener {

  @EventHandler
  public void onChat(AsyncPlayerChatEvent evt) {
    Set<Player> r = evt.getRecipients();
    Player player = evt.getPlayer();
    HylexPlayer hp = HylexPlayer.get(player);
    if (hp != null) {
      ArenaPlayer ap = hp.getArenaPlayer();
      if (ap != null) {
        for (Player pls : Bukkit.getServer().getOnlinePlayers()) {
          HylexPlayer hps = HylexPlayer.get(pls);
          if (hps != null) {
            ArenaPlayer aps = hps.getArenaPlayer();
            if (ap.getArena() != aps.getArena()) {
              r.remove(pls);
            } else {
              Arena a = aps.getArena();
              if (a == null)
                continue;

              if (a.getState() == ArenaEnums.ArenaState.IN_GAME && a.getConfiguration().getTeamsSize() > 1 && aps.getCurrentState().isInGame()) {
                if (aps.getTeam() != ap.getTeam()) {
                  r.remove(pls);
                }
                if (ap.getCurrentState().isSpectating() && !aps.getCurrentState().isSpectating()) {
                  r.remove(pls);
                }
              }
            }
          }
        }
        if (!evt.isCancelled()) {
          if (ap.getArena() != null) {
            Arena arena = ap.getArena();
            if (arena != null) {
              if (arena.getState() == ArenaEnums.ArenaState.IN_GAME) {
                if (ap.getCurrentState().isSpectating()) {
                  r.forEach(recipents -> recipents.sendMessage("§7[ESPECTADOR]" + player.getName() + "§7: " + (player.hasPermission("hylex.chatcolor") ?
                    ChatColor.translateAlternateColorCodes('&', evt.getMessage()) :
                    evt.getMessage())));
                }else {
                  r.forEach(recipents -> recipents.sendMessage("§7[" + ap.getTeam().getTeamType().getDisplayName().toUpperCase() +"§7] " + player.getName() + "§7: " + (player.hasPermission("hylex.chatcolor") ?
                    ChatColor.translateAlternateColorCodes('&', evt.getMessage()) :
                    evt.getMessage())));
                }

              }else {
                  r.forEach(recipents -> recipents.sendMessage(hp.getGroup().getDisplay() + player.getName() + "§7: " + (player.hasPermission("hylex.chatcolor") ?
                    ChatColor.translateAlternateColorCodes('&', evt.getMessage()) :
                    evt.getMessage())));
              }
            }
          } else {
              r.forEach(recipents -> recipents.sendMessage("§8[L]§r " + hp.getGroup().getDisplay() + player.getName() + "§7: " + (player.hasPermission("hylex.chatcolor") ?
                ChatColor.translateAlternateColorCodes('&', evt.getMessage()) :
                evt.getMessage())));
          }
        }

      }
      evt.setCancelled(true);
    }
  }


}
