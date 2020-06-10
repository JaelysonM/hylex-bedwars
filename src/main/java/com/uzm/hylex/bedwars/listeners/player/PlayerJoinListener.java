package com.uzm.hylex.bedwars.listeners.player;

import com.uzm.hylex.bedwars.Core;
import com.uzm.hylex.bedwars.arena.Arena;
import com.uzm.hylex.bedwars.controllers.ArenaController;
import com.uzm.hylex.bedwars.controllers.HylexPlayerController;
import com.uzm.hylex.bedwars.proxy.ServerItem;
import com.uzm.hylex.core.api.HylexPlayer;
import com.uzm.hylex.core.api.events.HylexPlayerLoadEvent;
import com.uzm.hylex.core.api.interfaces.Enums;
import com.uzm.hylex.core.controllers.FakeController;
import com.uzm.hylex.core.nms.NMS;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;

import static com.uzm.hylex.bedwars.controllers.MatchmakingController.MINI_QUEUE;

public class PlayerJoinListener implements Listener {

  @EventHandler
  public void onHylexPlayerLoad(HylexPlayerLoadEvent evt) {
    HylexPlayer hp = evt.getHylexPlayer();

    new BukkitRunnable() {
      @Override
      public void run() {
        Player player = hp.getPlayer();
        if (MINI_QUEUE.containsKey(player.getName())) {
          Arena arena = ArenaController.getArena(MINI_QUEUE.remove(player.getName()));
          if (arena == null) {
            ServerItem.getServerItem("lobby").connect(hp);
            return;
          }
          if (arena.canJoin(hp)) {
            hp.getPlayer().getInventory().setItem(1, null);
            hp.getPlayer().getActivePotionEffects().forEach(effect -> hp.getPlayer().removePotionEffect(effect.getType()));
            Bukkit.getScheduler().runTask(Core.getInstance(), () -> arena.join(hp));
          }else {
            if (!player.hasPermission("hylex.staff")) {
              player.kickPlayer(" \n§cOcorreu um erro enquanto você tentava entrar na sala.\n \n§cIsso ocorre normalmente quando o servidor ainda está despreparado para receber logins, aguarde um pouco e tente novamente.");
            }
          }
        } else {
          Bukkit.getScheduler().runTask(Core.getInstance(), () -> {

            for (Player players : Bukkit.getOnlinePlayers()) {
              players.hidePlayer(player);
            }
          });
        }
      }
    }.runTask(Core.getInstance());
  }

  @EventHandler
  public void onPlayerLogin(PlayerLoginEvent evt) {
    HylexPlayer hp = HylexPlayer.create(evt.getPlayer());
    hp.requestLoad("BedWarsData");
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onPlayerLoginMonitor(PlayerLoginEvent evt) {
    if (HylexPlayer.getByPlayer(evt.getPlayer()) == null) {
      evt.disallow(PlayerLoginEvent.Result.KICK_OTHER,
        " \n§cAparentemente o servidor não conseguiu carregar seu Perfil.\n \n§cIsso ocorre normalmente quando o servidor ainda está despreparado para receber logins, aguarde um pouco e tente novamente.");
    }else {
      if (MINI_QUEUE
        .containsKey(evt.getPlayer().getName())) {
        Arena arena = ArenaController.getArena(MINI_QUEUE.getOrDefault(evt.getPlayer().getName(), null));
        if (!arena.canJoin(HylexPlayer.getByPlayer(evt.getPlayer())) && !evt.getPlayer().hasPermission("hylex.staff")) {
          evt.disallow(PlayerLoginEvent.Result.KICK_OTHER,
            " \n§cOcorreu um erro enquanto você tentava entrar na sala.\n \n§cIsso ocorre normalmente quando o servidor ainda está despreparado para receber logins, aguarde um pouco e tente novamente.");
        }

      }
    }
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent evt) {
    evt.setJoinMessage(null);
    Player player = evt.getPlayer();
    HylexPlayer hp = HylexPlayer.getByPlayer(player);
    hp.setupPlayer();
    HylexPlayerController.setupHotbar(hp);
  //  if (FakeController.isAvaliable("zRapido"))
    //FakeController.apply(player, "zRapido");

    //NMS.refreshPlayer(player);



  }
}
