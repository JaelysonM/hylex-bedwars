package com.uzm.hylex.bedwars;

import com.uzm.hylex.bedwars.arena.player.ArenaEquipment;
import com.uzm.hylex.bedwars.controllers.ArenaController;
import com.uzm.hylex.bedwars.loaders.PluginLoader;
import com.uzm.hylex.bedwars.proxy.BungeePluginMessageListener;
import com.uzm.hylex.bedwars.proxy.LobbyMessageListener;
import com.uzm.hylex.core.java.util.configuration.ConfigurationCreator;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;

public class Core extends JavaPlugin {

  private static Core core;
  public static PluginLoader loader;
  public static Team team;

  @Override
  public void onLoad() {
    new ConfigurationCreator(this, "setup", "");
    com.uzm.hylex.core.Core.SOCKET_NAME = "bedwars-" + ConfigurationCreator.find("setup", this).get().getString("mega-name");
    com.uzm.hylex.core.Core.IS_ARENA_CLIENT = true;
    com.uzm.hylex.core.Core.DISABLE_FLY = true;
  }

  public void onEnable() {
    System.gc();
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

    for (Team team : new ArrayList<>(Bukkit.getScoreboardManager().getMainScoreboard().getTeams())) {
      if (team.getName().contains("mini")) {
        team.unregister();
      }
    }

    Objective finalHealthPL = healthPL;
    new BukkitRunnable() {
      @Override
      public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
          int level = (int) player.getHealth();
          level += ((CraftPlayer) player).getHandle().getAbsorptionHearts();
          finalHealthPL.getScore(player.getName()).setScore(level);
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
