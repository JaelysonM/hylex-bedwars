package com.uzm.hylex.bedwars.arena;

import com.uzm.hylex.bedwars.Core;
import com.uzm.hylex.bedwars.arena.enums.ArenaEnums;
import com.uzm.hylex.bedwars.arena.generators.Generator;
import com.uzm.hylex.bedwars.arena.player.ArenaPlayer;
import com.uzm.hylex.bedwars.arena.team.Team;
import com.uzm.hylex.bedwars.utils.Utils;
import com.uzm.hylex.core.spigot.features.Titles;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static com.uzm.hylex.bedwars.arena.enums.ArenaEnums.ArenaState.*;
import static com.uzm.hylex.bedwars.arena.team.Team.Sitation.BROKEN_BED;
import static com.uzm.hylex.bedwars.arena.team.Team.Sitation.ELIMINATED;

public class ArenaTask {

  private BukkitTask task;
  private Arena arena;

  private int time;

  public ArenaTask(Arena arena) {
    this.arena = arena;
  }

  public Arena getArena() {
    return arena;
  }

  public void cancel() {
    if (this.task != null) {
      this.task.cancel();
      this.task = null;
    }
  }

  public void reset() {
    this.cancel();
    this.task = new BukkitRunnable() {
      @Override
      public void run() {
        execute();
      }
    }.runTaskTimer(Core.getInstance(), 0, 20);
  }

  private final DateFormat DF = new SimpleDateFormat("mm:ss");

  public void execute() {
    switch (getArena().getState()) {
      case END:
        break;
      case IN_GAME:
        getArena().getTeams().values().forEach(Team::tickGenerator);
        List<String> lines = new ArrayList<>();
        lines.add("      §8[" + getArena().getArenaName() + "]");
        lines.add("");
        lines.add(" " + getArena().getUpgradeState().getName() + " em §a" + DF.format(this.time * 1000) + "s");
        lines.add("");
        getArena().getTeams().values().forEach(team -> lines.add(" " + team.getTeamType().getScoreboardName() + " §f" + team.getTeamType().getName() + " " + (team.getSitation() == ELIMINATED ? "§c§l✖" : team.getSitation() == BROKEN_BED ? ("§e" + team.getAlive().size()) : "§a§l✔")));
        lines.add("");
        lines.add("       §bhylex.net");
        getArena().getArenaPlayers().forEach(ap -> {
          if (ap.getScoreboard() != null) {
            ap.getScoreboard().updateLines(lines);
          }
        });

        lines.clear();
        if (this.time == 0) {
          getArena().getGenerators().forEach(generator -> {
            if (generator.getType() == Generator.Type.DIAMOND) {
              generator.setTier(getArena().getUpgradeState().getDiamondDelay());
            } else {
              generator.setTier(getArena().getUpgradeState().getEmeraldDelay());
            }
          });
          getArena().setEventState(getArena().getUpgradeState().next());
          this.time = 60 * 5;
          return;
        }

        this.time--;
        break;
      default:
        if (getArena().getPlayingPlayers().size() < getArena().getConfiguration().getMinPlayers()) {
          getArena().getArenaPlayers().forEach(ap -> {
            if (ap.getScoreboard() != null) {
              ap.getScoreboard().updateLines("      §8[" + getArena().getArenaName() + "]",
                "",
                " Mapa: §a" + Utils.removeNumbers(getArena().getWorldName()),
                " Jogadores: §a" + getArena().getPlayingPlayers().size() + "/" + getArena().getConfiguration().getMaxPlayers(),
                "",
                " Aguardando jogadores...",
                "",
                " Modo: §a" + getArena().getConfiguration().getMode(),
                "",
                "       §bhylex.net");
            }
          });
          if (this.time
            != 20) {
            this.time = 20;
            getArena().getArenaPlayers().forEach(ap -> {
              Player player = ap.getPlayer();
              new Titles(player, Titles.TitleType.SUBTILE).setBottomMessage("§c§nJogadores insuficientes").send(20, 20 * 2, 20);
              player.sendMessage("§cO contador foi reiniciado, devido a falta de jogadores");
              player.playSound(player.getLocation(), Sound.CREEPER_HISS, 1.5F, 1.5F);
            });
            this.arena.setState(IN_WAITING);
          }
          return;
        }

        if (this.arena.getState() != PREPARE) {
          this.arena.setState(PREPARE);
        }

        getArena().getArenaPlayers().forEach(ap -> {
          if (ap.getScoreboard() != null) {
            ap.getScoreboard().updateLines("      §8[" + getArena().getArenaName(),
              "",
              " Mapa: §a" + Utils.removeNumbers(getArena().getWorldName()),
              " Jogadores: §a" + getArena().getPlayingPlayers().size() + "/" + getArena().getConfiguration().getMaxPlayers(),
              "",
              " Iniciando em §a" + this.time + "s",
              "",
              " Modo: §a" + getArena().getConfiguration().getMode(),
              "",
              "       §bhylex.net");
          }
        });

        if (this.time <= 0) {
          getArena().start();
          this.time = 60 * 5;
          return;
        }

        if (this.time <= 5) {
          if (this.arena.getState() != STARTING) {
            this.arena.setState(STARTING);
          }

          String color = this.time >= 3 ? "§a§l" : this.time == 2 ? "§6§l" : "§c§l";
          for (ArenaPlayer p : getArena().getArenaPlayers()) {
            new Titles(p.getPlayer(), Titles.TitleType.TITLE).setBottomMessage(color + this.time).send(0, 20, 0);
            p.getPlayer().sendMessage("§ePartida iniciando em §f: " + color + this.time + (this.time == 1 ? " §fsegundo" : " §fsegundos"));
            p.getPlayer().playSound(p.getPlayer().getLocation(), Sound.CLICK, 1.2F, 1.2F);
          }
        }

        this.time--;
        break;
    }
  }
}
