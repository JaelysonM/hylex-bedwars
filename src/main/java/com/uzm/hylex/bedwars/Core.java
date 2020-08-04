package com.uzm.hylex.bedwars;

import com.uzm.hylex.bedwars.arena.Arena;
import com.uzm.hylex.bedwars.arena.player.ArenaEquipment;
import com.uzm.hylex.bedwars.controllers.ArenaController;
import com.uzm.hylex.bedwars.controllers.DiscordController;
import com.uzm.hylex.bedwars.loaders.PluginLoader;
import com.uzm.hylex.bedwars.proxy.BungeePluginMessageListener;
import com.uzm.hylex.bedwars.proxy.LobbyMessageListener;
import com.uzm.hylex.core.controllers.FakeController;
import com.uzm.hylex.core.java.util.configuration.ConfigurationCreator;
import com.uzm.hylex.services.lan.WebSocket;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class Core extends JavaPlugin {

  private static Core core;
  public static PluginLoader loader;
  public static Team team;
  public static int restartingCount;

  @Override
  public void onLoad() {
    new ConfigurationCreator(this, "setup", "");
    com.uzm.hylex.core.Core.SOCKET_NAME = "bedwars-" + ConfigurationCreator.find("setup", this).get().getString("mega-name");
    com.uzm.hylex.core.Core.IS_ARENA_CLIENT = true;
    com.uzm.hylex.core.Core.DISABLE_FLY = true;
    restartingCount =
      ConfigurationCreator.find("setup", this).get().get("restarting-count") == null ? 3 : ConfigurationCreator.find("setup", this).get().getInt("restarting-count");
  }

  public void onEnable() {
    long aux = System.currentTimeMillis();


    Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
    if (scoreboard.getObjective("healthBN") == null) {
      Objective objective = scoreboard.registerNewObjective("healthBN", "health");
      objective.setDisplayName("§c❤");
      objective.setDisplaySlot(DisplaySlot.BELOW_NAME);
    }

    Objective healthPL = scoreboard.getObjective("healthPL");
    if (healthPL == null) {
      healthPL = scoreboard.registerNewObjective("healthPL", "dummy");
      healthPL.setDisplaySlot(DisplaySlot.PLAYER_LIST);
    }

    team = scoreboard.getTeam("spectators");
    if (team == null) {
      team = scoreboard.registerNewTeam("spectators");
      team.setPrefix("§8");
      team.setCanSeeFriendlyInvisibles(true);
    }

    for (Team teams : new ArrayList<>(Bukkit.getScoreboardManager().getMainScoreboard().getTeams())) {
      if (teams.getName().contains("mini")) {
        teams.unregister();
      }
    }

    Objective finalHealthPL = healthPL;
    new BukkitRunnable() {
      @Override
      public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
          int level = (int) player.getHealth();
          level += ((CraftPlayer) player).getHandle().getAbsorptionHearts();
          if (FakeController.has(player.getName())) {
            finalHealthPL.getScore(FakeController.getFake(player.getName())).setScore(level);
          } else {
            finalHealthPL.getScore(player.getName()).setScore(level);
          }

        }
      }
    }.runTaskTimerAsynchronously(this, 0, 20);


    new BukkitRunnable() {
      @Override
      public void run() {
        for (Arena arenas : ArenaController.listArenas()) {
          if (arenas.getSpectatorMenu() != null) {
            arenas.getSpectatorMenu().update();
          }
        }
      }
    }.runTaskTimerAsynchronously(this, 0, 20);

    getServer().getConsoleSender().sendMessage("§b[Hylex Module: BedWars] §7Plugin §fessencialmente §7carregado com sucesso.");
    getServer().getConsoleSender().sendMessage("§eVersão: §f" + getDescription().getVersion() + " e criado por §f" + getDescription().getAuthors());

    core = this;
    loader = new PluginLoader(this);

    Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
    Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(this, "hylex-core");
    Bukkit.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", new BungeePluginMessageListener());
    Bukkit.getServer().getMessenger().registerIncomingPluginChannel(this, "hylex-core", new LobbyMessageListener());


    getServer().getConsoleSender()
      .sendMessage("§b[Hylex Module: BedWars] §7Plugin §fdefinitivamente §7carregado com sucesso (§f" + (System.currentTimeMillis() - aux + " milisegundos§7)"));

    ArenaEquipment.woodSword();

    JSONObject json = new JSONObject();
    json.put("clientName", "core-" + com.uzm.hylex.core.Core.SOCKET_NAME);
    WebSocket.get("core-" + com.uzm.hylex.core.Core.SOCKET_NAME).getSocket().emit("send-finished-restart", json);

    Bukkit.getConsoleSender().sendMessage("§b[Hylex Module: Core] §7Send a finish restart");

  }



  public void onDisable() {
    getServer().getConsoleSender().sendMessage("§b[Hylex Module: BedWars] §7Deletando mundos das arenas...");

    getServer().getConsoleSender().sendMessage("§b[Hylex Module: BedWars] §7Plugin §bdesligado§7, juntamente todos os eventos e comandos também.");
  }

  public static Core getInstance() {
    return core;
  }

  public static PluginLoader getLoader() {
    return loader;
  }
}
