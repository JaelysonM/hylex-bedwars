package com.uzm.hylex.bedwars.listeners.player;

import com.uzm.hylex.bedwars.arena.Arena;
import com.uzm.hylex.bedwars.arena.player.ArenaPlayer;
import com.uzm.hylex.core.api.HylexPlayer;
import com.uzm.hylex.core.controllers.FakeController;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.ArrayList;
import java.util.List;

import static com.uzm.hylex.core.api.interfaces.Enums.ArenaState.IN_GAME;

public class PlayerChatListener implements Listener {

  @EventHandler(priority = EventPriority.HIGH)
  public void onChat(AsyncPlayerChatEvent evt) {
    if (evt.isCancelled()) {
      return;
    }

    boolean shout = evt.getMessage().startsWith("!g");

    if (shout && evt.getMessage().trim().split("!g").length == 0 || evt.getMessage().trim().equalsIgnoreCase("!g")) {
      evt.getPlayer().sendMessage("§cUse /g <mensagem>");
      evt.setCancelled(true);
      return;
    }

    evt.setMessage(evt.getMessage().replace("!g", "").trim());

    Player player = evt.getPlayer();
    HylexPlayer hp = HylexPlayer.getByPlayer(player);

    String color = player.hasPermission("hylex.colorchat") && !FakeController.has(player.getName()) ? "§f" : "§7";
    if (player.hasPermission("hylex.colorchat")) {
      evt.setMessage(ChatColor.translateAlternateColorCodes('&', evt.getMessage()));
    }

    if (hp == null) {
      evt.setCancelled(true);
      return;
    }

    ArenaPlayer ap = (ArenaPlayer) hp.getArenaPlayer();
    if (ap != null) {
      Arena arena = ap.getArena();
      if (arena != null) {
        List<Player> newRecipients = new ArrayList<>();
        for (Player recipient : evt.getRecipients()) {
          HylexPlayer hps = HylexPlayer.getByPlayer(recipient);
          if (hps != null) {
            if (player.hasPermission("hylex.staff") && hps.getArenaPlayer() != null && hps.getArenaPlayer().getArena() == null) {
              newRecipients.add(recipient);
            } else {
              ArenaPlayer aps = (ArenaPlayer) hps.getArenaPlayer();
              if (aps != null && arena.equals(aps.getArena())) {
                if (ap.getCurrentState().isSpectating()) {
                  if (!aps.getCurrentState().isSpectating()) {
                    continue;
                  }
                }

                if (arena.getConfiguration().getTeamsSize() > 1) {
                  if (ap.getTeam() != null && !ap.getTeam().equals(aps.getTeam()) && !shout) {
                    continue;
                  }
                }

                newRecipients.add(recipient);
              }
            }
          }
        }

        evt.getRecipients().clear();
        evt.getRecipients().addAll(newRecipients);
        String p =
          arena.getState() == IN_GAME ? (ap.getCurrentState().isSpectating() ? "§8[ESPECTADOR] " : "§8[" + ap.getTeam().getTeamType().getDisplayName().toUpperCase() + "§8] ") : "";
        String prefix = shout ? "§6[GLOBAL] " + p : p;
        String format = "%s" + color + ": %s";
        evt.setFormat(prefix + format);
        return;
      }
    }

    evt.setFormat("§8[OUT] %s" + color + ": %s");
  }
}
