package com.uzm.hylex.bedwars.controllers;

import com.google.common.collect.ImmutableList;
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
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.HashMap;
import java.util.Iterator;
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


        for (Arena arena : ImmutableList.copyOf(ArenaController.arenas.values())) {
          JSONObject mini = new JSONObject();
          mini.put("name", arena.getArenaName());
          mini.put("attached", com.uzm.hylex.core.Core.SOCKET_NAME.replace("bedwars-", ""));
          mini.put("mapName", arena.getWorldName());
          JSONObject object = new JSONObject();
          object.put("players",
            arena.getState() != Enums.ArenaState.IN_GAME && arena.getState() != Enums.ArenaState.END ? arena.getArenaPlayers().size() : arena.getPlayingPlayers().size());
          object.put("maxPlayers", arena.getConfiguration().getMaxPlayers());

          if (Bukkit.getServer().hasWhitelist()) {
            object.put("state", Enums.ArenaState.IDLE.name());
          } else {
            object.put("state", arena.getState().name());
          }
          object.put("mode", arena.getConfiguration().getMode().toUpperCase());
          mini.put("arena", object);
          arenas.add(mini);
        }



        data.put("body", arenas);
        socket.getSocket().emit("update-mini", data);
      }
    }.runTaskTimerAsynchronously(Core.getInstance(), 0, 5);

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
              if (target.isOnline()) {
                HylexPlayer hp = HylexPlayer.getByPlayer(target);
                if (hp != null) {
                  Arena arena = ArenaController.getArena(mini);
                  if (arena == null) {
                    ServerItem.getServerItem("lobby").connect(hp);
                    continue;
                  }
                  if (!hp.isAccountLoaded()) {
                    hp.getPlayer().kickPlayer(
                      " \n§cAparentemente o servidor não conseguiu carregar seu Perfil.\n \n§cIsso ocorre normalmente quando o servidor ainda está despreparado para receber logins, aguarde um pouco e tente novamente.");

                    continue;
                  }
                  if (hp.getBedWarsStatistics() == null) {
                    if (hp.getPlayer() != null) {
                      hp.getPlayer().kickPlayer(
                        " \n§cAparentemente o servidor não conseguiu carregar seu Perfil.\n \n§cIsso ocorre normalmente quando o servidor ainda está despreparado para receber logins, aguarde um pouco e tente novamente.");
                    }
                    continue;
                  }

                  if (arena.canJoin(hp)) {
                    hp.getPlayer().getInventory().setItem(1, new ItemStack(Material.AIR));
                    hp.getPlayer().getActivePotionEffects().forEach(effect -> hp.getPlayer().removePotionEffect(effect.getType()));
                    if (hp.getArenaPlayer() != null) {
                      if (hp.getArenaPlayer().getArena() != null)
                        Bukkit.getScheduler().runTask(Core.getInstance(), () -> hp.getArenaPlayer().getArena().leave(hp));
                    }
                    Bukkit.getScheduler().runTask(Core.getInstance(), () -> arena.join(hp));
                  } else {
                    if (!target.hasPermission("hylex.staff")) {
                      target.kickPlayer(
                        " \n§cOcorreu um erro enquanto você tentava entrar na sala.\n \n§cIsso ocorre normalmente quando o servidor ainda está despreparado para receber logins, aguarde um pouco e tente novamente.");
                    } else {
                      if (arena.getState().isInGame()) {
                        if (hp.getArenaPlayer() != null) {
                          if (hp.getArenaPlayer().getArena() != null)
                            Bukkit.getScheduler().runTask(Core.getInstance(), () -> hp.getArenaPlayer().getArena().leave(hp));
                        }
                        Bukkit.getScheduler().runTask(Core.getInstance(), () -> arena.spec(hp));

                      } else {
                        target.kickPlayer(
                          " \n§cOcorreu um erro enquanto você tentava entrar na sala.\n \n§cIsso ocorre normalmente quando o servidor ainda está despreparado para receber logins, aguarde um pouco e tente novamente.");
                      }
                    }
                  }
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

    socket.getSocket().on("send-restart-server", (args) -> {
      if (!(args[0] instanceof org.json.JSONObject)) {
        return;
      }
      try {
        JSONObject response = (JSONObject) new JSONParser().parse(args[0].toString());
        String clientName = ((String) response.get("name")).replace("core-bedwars-", "");
        Bukkit.getConsoleSender().sendMessage("§b[Hylex Module: Core] §7Restarting a permission " + clientName + " §f(AUTHORIZED FROM BUNGEE)");
        new BukkitRunnable() {
          @Override
          public void run() {
            Bukkit.shutdown();
          }
        }.runTask(Core.getInstance());

      } catch (Exception ex) {
        System.err.println("[Socket.io - ServerController ]  Não foi possível processar os dados recibos.");
        ex.printStackTrace();
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
                    ProxyUtils.connect(hp, mega, mini);
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

    int teamSize = 1;

    if (mode.equalsIgnoreCase("solo")) {
      teamSize = 1;
    } else if (mode.equalsIgnoreCase("dupla")) {
      teamSize = 2;
    } else if (mode.equalsIgnoreCase("trio")) {
      teamSize = 3;
    } else if (mode.equalsIgnoreCase("squad")) {
      teamSize = 4;
    } else if (mode.equalsIgnoreCase("1v1")) {
      teamSize = 2;
    } else if (mode.equalsIgnoreCase("2v2")) {
      teamSize = 2;
    } else if (mode.equalsIgnoreCase("5v5")) {
      teamSize = 5;
    } else if (mode.equalsIgnoreCase("10v10")) {
      teamSize = 10;
    } else if (mode.equalsIgnoreCase("20v20")) {
      teamSize = 20;
    }


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
        if (party.listMembers().size() > teamSize) {
          player.sendMessage("§cVocê não pode buscar por partidas no modo §b" + StringUtils
            .capitaliseAllWords(mode.toLowerCase()) + " §c, visto que sua party tem mais jogadores do que o tamanho máximo do time.");
          return;
        }
        party.listMembers().stream().map(PartyPlayer::getName).forEach(players::add);
      } else {
        players.add(player.getName());
      }
      object.put("players", players);


      object.put("mode", mode);
      object.put("clientName", "core-" + com.uzm.hylex.core.Core.SOCKET_NAME);
      WebSocket.get("core-" + com.uzm.hylex.core.Core.SOCKET_NAME).getSocket().emit("search-arena", object);
    }
  }
}
