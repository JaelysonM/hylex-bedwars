package com.uzm.hylex.bedwars.listeners.player;

import com.uzm.hylex.bedwars.arena.Arena;
import com.uzm.hylex.bedwars.arena.player.ArenaPlayer;
import com.uzm.hylex.core.api.HylexPlayer;
import com.uzm.hylex.core.api.interfaces.Enums;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;


public class PlayerCommandListener implements Listener {

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onChat(PlayerCommandPreprocessEvent evt) {
    Player player = evt.getPlayer();
    HylexPlayer hp = HylexPlayer.getByPlayer(player);

    ArenaPlayer ap = (ArenaPlayer) hp.getArenaPlayer();
    boolean shout = evt.getMessage().toLowerCase().startsWith("/g ") || evt.getMessage().equalsIgnoreCase("/g");
    if (shout) {
      if (ap != null) {
        Arena arena = ap.getArena();
        if (arena != null) {
          if (arena.getConfiguration().getTeamsSize() == 1) {
            return;
          }
          if (arena.getState() == Enums.ArenaState.IN_GAME) {
            evt.getPlayer().chat(evt.getMessage().replace("/g", "!g"));
          } else {
            evt.getPlayer().sendMessage("§cVocê não pode usar o /g agora.");
          }
          evt.setCancelled(true);
        }
      }
    }

  }
}
