package com.uzm.hylex.bedwars.controllers;

import com.uzm.hylex.bedwars.Core;
import com.uzm.hylex.bedwars.arena.Arena;
import com.uzm.hylex.bedwars.proxy.ServerItem;
import com.uzm.hylex.core.api.HylexPlayer;
import com.uzm.hylex.core.api.interfaces.Enums;
import com.uzm.hylex.core.api.party.PartyPlayer;
import com.uzm.hylex.core.party.BukkitParty;
import com.uzm.hylex.core.party.BukkitPartyManager;
import com.uzm.hylex.core.utils.ProxyUtils;
import com.uzm.hylex.services.lan.WebSocket;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("all")
public class MatchmakingController {

  public static final Map<UUID, Long> DELAY = new HashMap<>();
  public static final Map<String, String> MINI_QUEUE = new HashMap<>();

  public static void setupMatchmaking() {
    new BukkitRunnable() {

      @Override
      public void run() {
        WebSocket socket = WebSocket.get("core-" + com.uzm.hylex.core.Core.SOCKET_NAME);
        JSONObject data = new JSONObject();
        data.put("minigame", "bedwars");
        JSONArray arenas = new JSONArray();
        ArenaController.listArenas().forEach(arena -> {
          JSONObject mini = new JSONObject();
          mini.put("name", arena.getArenaName());
          mini.put("attached", com.uzm.hylex.core.Core.SOCKET_NAME.replace("bedwars-", ""));
          mini.put("mapName", arena.getWorldName());
          JSONObject object = new JSONObject();
          object.put("players",
            arena.getState() != Enums.ArenaState.IN_GAME && arena.getState() != Enums.ArenaState.END ? arena.getArenaPlayers().size() : arena.getPlayingPlayers().size());
          object.put("maxPlayers", arena.getConfiguration().getMaxPlayers());
          object.put("state", arena.getState().name());
          object.put("mode", arena.getConfiguration().getMode().toUpperCase());
          mini.put("arena", object);
          arenas.add(mini);
        });
        data.put("body", arenas);
        socket.getSocket().emit("update-mini", data);
      }
    }.runTaskTimerAsynchronously(Core.getInstance(), 0, 10);

    WebSocket socket = WebSocket.get("core-" + com.uzm.hylex.core.Core.SOCKET_NAME);
    socket.getSocket().on("join-mini", (args) -> {
      if (args[0] instanceof org.json.JSONObject) {
        try {
          org.json.simple.JSONObject response = (org.json.simple.JSONObject) new JSONParser().parse(args[0].toString());
          String mini = response.get("name").toString();
          JSONArray players = (JSONArray) response.get("players");
          for (Object object : players) {
            String name = object.toString();
            Player target = Bukkit.getPlayerExact(name);
            if (target != null) {
              HylexPlayer hp = HylexPlayer.getByPlayer(target);
              if (hp != null) {
                Arena arena = ArenaController.getArena(mini);
                if (arena == null) {
                  ServerItem.getServerItem("lobby").connect(hp);
                  continue;
                }

                if (hp.getArenaPlayer() != null) {
                  hp.getArenaPlayer().getArena().leave(hp);
                }
                if (arena.canJoin(hp)) {
                  hp.getPlayer().getInventory().setItem(1, null);
                  hp.getPlayer().getActivePotionEffects().forEach(effect -> hp.getPlayer().removePotionEffect(effect.getType()));
                  Bukkit.getScheduler().runTask(Core.getInstance(), () -> arena.join(hp));
                }
              }
              continue;
            }

            MINI_QUEUE.put(name, mini);
          }
        } catch (Exception ex) {
          System.err.println("[HylexSocket.io - Matchmaking]  Não foi possível processar os dados recebidos.");
          ex.printStackTrace();
        }
      }
    });

    socket.getSocket().on("matchmaking-callback", (args) -> {
      if (args[0] instanceof org.json.JSONObject) {
        try {
          org.json.simple.JSONObject response = (org.json.simple.JSONObject) new JSONParser().parse(args[0].toString());
          JSONArray players = (JSONArray) response.get("players");


          String mega = "";
          String mini = "";
          boolean matchFound = ((JSONObject) response.get("response")).get("type").toString().equals("Matchs found");
          if (matchFound) {
            mini = ((JSONObject) response.get("matchFound")).get("name").toString();
            mega = ((JSONObject) response.get("matchFound")).get("attached").toString();
          }
          BukkitParty party = null;
          for (Object object : players) {
            if ((party = BukkitPartyManager.getLeaderParty(object.toString())) != null) {
              break;
            }
          }


          if (party != null) {
            Player leader = Bukkit.getPlayerExact(party.getLeader());
            if (leader != null) {
              if (matchFound) {
                com.uzm.hylex.bedwars.utils.ProxyUtils.sendPartyMembers(leader, mega, mini);
              } else {
                leader.sendMessage("§cNão existem partidas disponíveis no momento para o modo de jogo selecionado.");
              }
            }
          } else {
            String name = players.get(0).toString();
            Player target = Bukkit.getPlayerExact(name);
            if (target != null) {
              HylexPlayer hp = HylexPlayer.getByPlayer(target);
              if (hp != null) {
                if (!mega.equalsIgnoreCase(com.uzm.hylex.core.Core.SOCKET_NAME.replace("bedwars-", ""))) {
                  target.sendMessage(matchFound ? "§8Sendo enviado para " + mini + "..." : "§cNão existe partidas disponíveis no momento para o modo de jogo selecionado.");
                  if (matchFound)
                    ProxyUtils.connect(hp, mega);
                }
              }
            }
          }
        } catch (Exception ex) {
          System.err.println("[HylexSocket.io - Matchmaking]  Não foi possível processar os dados recebidos.");
          ex.printStackTrace();
        }
      }
    });
  }

  public static void findMatch(HylexPlayer hp, String mode) {
    Player player = hp.getPlayer();
    if (player != null) {
      BukkitParty party = BukkitPartyManager.getMemberParty(player.getName());
      if (party != null && !party.isLeader(player.getName())) {
        player.sendMessage("§cApenas o líder da Party pode buscar por partidas.");
        return;
      }

      if (DELAY.getOrDefault(player.getUniqueId(), 0L) > System.currentTimeMillis()) {
        return;
      }

      DELAY.put(player.getUniqueId(), System.currentTimeMillis() + 2000);
      JSONObject object = new JSONObject();
      object.put("minigame", "bedwars");
      JSONArray players = new JSONArray();
      if (party != null) {
        party.listMembers().stream().map(PartyPlayer::getName).forEach(players::add);
      }else {
        players.add(player.getName());
      }
      object.put("players", players);


      object.put("mode", mode);
      object.put("clientName", "core-" + com.uzm.hylex.core.Core.SOCKET_NAME);
      WebSocket.get("core-" + com.uzm.hylex.core.Core.SOCKET_NAME).getSocket().emit("search-arena", object);
    }
  }
}
