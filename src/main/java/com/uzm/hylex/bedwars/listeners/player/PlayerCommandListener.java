package com.uzm.hylex.bedwars.listeners.player;

import com.uzm.hylex.bedwars.arena.Arena;
import com.uzm.hylex.bedwars.arena.player.ArenaPlayer;
import com.uzm.hylex.bedwars.controllers.DiscordController;
import com.uzm.hylex.core.api.Group;
import com.uzm.hylex.core.api.HylexPlayer;
import com.uzm.hylex.core.api.interfaces.Enums;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Arrays;
import java.util.List;



public class PlayerCommandListener implements Listener {

  private static String[] TO_DISABLE;

  static {
    TO_DISABLE = new String[] {"gm", "gamemode", "tp", "teleport", "v", "vis"};
  }


  @EventHandler(priority = EventPriority.HIGHEST)
  public void onChat(PlayerCommandPreprocessEvent evt) {
    Player player = evt.getPlayer();
    HylexPlayer hp = HylexPlayer.getByPlayer(player);

    ArenaPlayer ap = (ArenaPlayer) hp.getArenaPlayer();

    boolean shout = evt.getMessage().toLowerCase().startsWith("/g ") || evt.getMessage().equalsIgnoreCase("/g");
    if (shout) {
      if (ap != null) {
        if (!ap.getCurrentState().isInGame()) {
          evt.getPlayer().sendMessage("§cVocê não pode usar o /g agora.");
          return;
        }
      }
      if (ap != null) {
        Arena arena = ap.getArena();
        if (arena != null) {
          if (arena.getConfiguration().getTeamsSize() == 1) {
            return;
          }
          if (arena.getState() == Enums.ArenaState.IN_GAME) {
            evt.getPlayer().chat(evt.getMessage().replace("/g", "!g").replace("/G", "!g"));
          } else {
            evt.getPlayer().sendMessage("§cVocê não pode usar o /g agora.");
          }
          evt.setCancelled(true);
        }
      }
    }

  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onCommmandAbuse(PlayerCommandPreprocessEvent evt) {
    Player player = evt.getPlayer();
    HylexPlayer hp = HylexPlayer.getByPlayer(player);

    if (evt.isCancelled()) {
      return;
    }

    if (hp != null) {
      if (hp.getArenaPlayer() != null) {
        ArenaPlayer ap = (ArenaPlayer) hp.getArenaPlayer();
        if (ap.getArena() != null) {
          if (ap.getCurrentState() != ArenaPlayer.CurrentState.SPECTATING) {
            List<String> lists = Arrays.asList(TO_DISABLE);
            String command = evt.getMessage().split(" ")[0].replace("bukkit:", "").replace("minecraft:", "").toLowerCase().replace("/", "");
            if (lists.contains(command)) {
              if (hp.getGroup().ordinal() > Group.GERENTE.ordinal()) {
                if (hp.getGroup() != Group.NORMAL) {
                  evt.setCancelled(true);
                  evt.getPlayer().sendMessage("§cVocê não possui permissão para executar esse comando enquanto joga.");
                  Bukkit.getConsoleSender()
                    .sendMessage("§e[TENTATIVA DE ABUSO] §fO jogador §b" + evt.getPlayer().getName() + " §ftentou executar §b" + command + "§f enquanto estava jogando.");
                  DiscordController.sendReport(evt.getPlayer().getName(), command, ChatColor.stripColor(hp.getGroup().getDisplay()), System.currentTimeMillis(),
                    com.uzm.hylex.core.Core.SOCKET_NAME.replace("bedwars-", "") + "-" + ap.getArena().getArenaName());

                }
              }
            }
          }


        }
      }
    }
  }
}


