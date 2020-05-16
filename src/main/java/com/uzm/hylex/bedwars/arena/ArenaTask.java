package com.uzm.hylex.bedwars.arena;

import com.uzm.hylex.bedwars.Core;
import com.uzm.hylex.bedwars.arena.enums.ArenaEnums;
import com.uzm.hylex.bedwars.arena.player.ArenaPlayer;
import com.uzm.hylex.bedwars.arena.team.Team;
import com.uzm.hylex.core.api.interfaces.IArenaPlayer;
import com.uzm.hylex.core.java.util.StringUtils;
import com.uzm.hylex.core.nms.NMS;
import com.uzm.hylex.core.spigot.features.Titles;
import com.uzm.hylex.core.utils.Utils;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.NameTagVisibility;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.uzm.hylex.bedwars.arena.team.Team.Sitation.*;
import static com.uzm.hylex.core.api.interfaces.Enums.ArenaState.*;

public class ArenaTask {

  private BukkitTask task;
  private Arena arena;

  private int time;

  public ArenaTask(Arena arena) {
    this.arena = arena;
    this.reset();
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

  public int getTime() {
    return time;
  }

  private static final DateFormat DF = new SimpleDateFormat("mm:ss");
  private static final DecimalFormat TRACKING_FORMAT = new DecimalFormat("###.#");

  public void execute() {
    switch (getArena().getState()) {
      case END:
        List<String> lines = new ArrayList<>();
        lines.add(" §8[BedWars - " + getArena().getArenaName() + "]");
        lines.add("");
        lines.add(" Fim de jogo!");
        lines.add("");
        getArena().listTeams().forEach(team -> lines.add(
          " " + team.getTeamType().getScoreboardName() + " §f" + team.getTeamType().getName() + ": " + (team.getSitation() == ELIMINATED ?
            "§c✖" :
            team.getSitation() == BROKEN_BED ? ("§e" + team.getAlive().size()) : "§a✔")));
        if (lines.size() == 8) {
          lines.add("");
          lines.add("Abates: §a{kills}");
          lines.add("Abates Finais: §a{fKills}");
          lines.add("Camas Destruídas: §a{beds}");
        }
        lines.add("");
        lines.add(" §bhylex.net");
        getArena().getArenaPlayers().forEach(a -> {
          ArenaPlayer ap = (ArenaPlayer) a;
          if (ap.getScoreboard() != null) {
            ap.getScoreboard().updateLines(lines.stream().map(result -> {
              result = result.replace("{kills}", StringUtils.formatNumber(ap.getKills())).replace("{fKills}", StringUtils.formatNumber(ap.getFinalKills()))
                .replace("{beds}", StringUtils.formatNumber(ap.getBedsBroken()));

              if (ap.getTeam() != null && result.contains(ap.getTeam().getTeamType().getName())) {
                return result + "§7 *";
              }

              return result;
            }).collect(Collectors.toList()));
          }
        });
        lines.clear();
        break;
      case IN_GAME:
        getArena().listTeams().forEach(Team::tick);
        lines = new ArrayList<>();
        lines.add(" §8[BedWars - " + getArena().getArenaName() + "]");
        lines.add("");
        lines.add(" " + getArena().getUpgradeState().getName() + " em §a" + DF.format(time * 1000));
        lines.add("");
        getArena().listTeams().forEach(team -> lines.add(
          " " + team.getTeamType().getScoreboardName() + " §f" + team.getTeamType().getName() + ": " + (team.getSitation() == ELIMINATED ?
            "§c✖" :
            team.getSitation() == BROKEN_BED ? ("§e" + team.getAlive().size()) : "§a✔")));
        if (lines.size() == 8) {
          lines.add("");
          lines.add("Abates: §a{kills}");
          lines.add("Abates Finais: §a{fKills}");
          lines.add("Camas Destruídas: §a{beds}");
        }
        lines.add("");
        lines.add(" §bhylex.net");
        getArena().getArenaPlayers().forEach(a -> {
          ArenaPlayer ap = (ArenaPlayer) a;
          Player player = ap.getPlayer();
          if (ap.getScoreboard() != null) {
            ap.getScoreboard().updateLines(lines.stream().map(result -> {
              result = result.replace("{kills}", StringUtils.formatNumber(ap.getKills())).replace("{fKills}", StringUtils.formatNumber(ap.getFinalKills()))
                .replace("{beds}", StringUtils.formatNumber(ap.getBedsBroken()));
              if (ap.getTeam() != null && result.contains(ap.getTeam().getTeamType().getName())) {
                return result + "§7 *";
              }

              return result;
            }).collect(Collectors.toList()));
          }
          if (ap.getEquipment() != null) {
            org.bukkit.scoreboard.Team scoreboardTeam = ap.getTeam().getTeam();
            if (ap.getEquipment().update()) {
              scoreboardTeam.setNameTagVisibility(NameTagVisibility.HIDE_FOR_OTHER_TEAMS);
              com.uzm.hylex.bedwars.nms.NMS.hideOrShowArmor(player, true,
                getArena().getArenaPlayers().stream().filter(aps -> !ap.getTeam().equals(((ArenaPlayer) aps).getTeam())).map(aps -> ((ArenaPlayer) aps).getPlayer())
                  .collect(Collectors.toList()));
            } else if (ap.getEquipment().isDisableInvisibility()) {
              scoreboardTeam.setNameTagVisibility(NameTagVisibility.ALWAYS);
              com.uzm.hylex.bedwars.nms.NMS.hideOrShowArmor(player, false,
                getArena().getArenaPlayers().stream().filter(aps -> !ap.getTeam().equals(((ArenaPlayer) aps).getTeam())).map(aps -> ((ArenaPlayer) aps).getPlayer())
                  .collect(Collectors.toList()));
            }

            if (ap.getEquipment().getTracking() != null) {
              Team team = arena.getTeams().get(ap.getEquipment().getTracking());
              if (team.getSitation() != ELIMINATED) {
                Player target =
                  team.getAlive().stream().map(ArenaPlayer::getPlayer).min(Comparator.comparingDouble(p -> p.getLocation().distance(player.getLocation()))).orElse(null);
                if (target != null) {
                  player.setCompassTarget(target.getLocation());
                  NMS.sendActionBar(player,
                    "§7Rastreando: " + target.getDisplayName() + " §a(" + TRACKING_FORMAT.format(player.getLocation().distance(target.getLocation())) + "m)");
                }
              }
            }
          }
        });
        lines.clear();
        if (getArena().getUpgradeState().getSubMessage() != null && getTime() == 60 * 5) {
          getArena().getArenaPlayers().stream().map(a -> ((ArenaPlayer) a).getPlayer()).forEach(player -> player.sendMessage(
            getArena().getUpgradeState().getSubMessage().replace("%s", String.valueOf(getTime() / 60)).replace("%format", (getTime() / 60 > 1 ? "minutos" : "minuto"))));
        }

        if (this.time == 0) {
          if (getArena().getUpgradeState() == ArenaEnums.Events.BED_DESTRUCTION) {
            getArena().getArenaPlayers().stream().map(a -> (ArenaPlayer) a).forEach(ap -> {
              Player player = ap.getPlayer();
              player.playSound(player.getLocation(), Sound.ENDERDRAGON_GROWL, 1.0F, 1.0F);
              NMS.sendTitle(player, Titles.TitleType.BOTH, "§fTodas as camas foram destruidas!", "§c§lCAMAS DESTRUÍDAS", 0, 60, 0);
            });
            getArena().listTeams().stream().filter(team -> team.getSitation() == STANDING).forEach(Team::breakBed);
          } else if (getArena().getUpgradeState() == ArenaEnums.Events.SUDDEN_DEATH) {
            getArena().listTeams().forEach(Team::disableGravity);
            getArena().listTeams().stream().filter(team -> team.getSitation() != ELIMINATED).forEach(team -> com.uzm.hylex.bedwars.nms.NMS.createTeamDragon(getArena(), team));
          } else if (getArena().getUpgradeState() == ArenaEnums.Events.GAME_END) {
            getArena().getArenaPlayers().stream().map(a -> (ArenaPlayer) a)
              .forEach(ap -> NMS.sendTitle(ap.getPlayer(), Titles.TitleType.BOTH, "§fNão houve ganhadores!", "§c§lFIM DE JOGO", 0, 60, 0));
            getArena().stop(null);
            return;
          }
          getArena().setEventState(getArena().getUpgradeState().next());
          getArena().getArenaPlayers().stream().map(a -> (ArenaPlayer) a).forEach(ap -> {
            Player player = ap.getPlayer();
            player.sendMessage(getArena().getUpgradeState().getMessage());
          });
          this.time = 60 * 6;
          return;
        }

        this.time--;
        break;
      default:
        if (getArena().getArenaPlayers().size() < getArena().getConfiguration().getMinPlayers()) {
          getArena().getArenaPlayers().forEach(a -> {
            ArenaPlayer ap = (ArenaPlayer) a;
            if (ap.getScoreboard() != null) {
              ap.getScoreboard().updateLines(" §8[BedWars - " + getArena().getArenaName() + "]", "", " Mapa: §a" + StringUtils.removeNumbers(getArena().getWorldName()),
                " Jogadores: §a" + getArena().getArenaPlayers().size() + "/" + getArena().getConfiguration().getMaxPlayers(), "", " Aguardando...", "",
                " Modo: §a" + getArena().getConfiguration().getMode(), "", " §bhylex.net");
            }
          });
          if (this.time != 20) {
            this.time = 20;
            getArena().getArenaPlayers().forEach(a -> {
              ArenaPlayer ap = (ArenaPlayer) a;
              Player player = ap.getPlayer();
              NMS.sendTitle(player, Titles.TitleType.BOTH, "§fJogadores insuficientes", "§c§lCONTAGEM CANCELADA", 0, 40, 0);
              player.sendMessage("§cO contador foi reiniciado, devido a falta de jogadores");
              player.playSound(player.getLocation(), Sound.CLICK, 1.2F, 1.2F);
            });
            this.arena.setState(IN_WAITING);
          }
          return;
        }

        if (this.arena.getState() != PREPARE) {
          this.arena.setState(PREPARE);
        }

        getArena().getArenaPlayers().forEach(a -> {
          ArenaPlayer ap = (ArenaPlayer) a;
          if (ap.getScoreboard() != null) {
            ap.getScoreboard().updateLines(" §8[BedWars - " + getArena().getArenaName() + "]", "", " Mapa: §a" + Utils.removeNumbers(getArena().getWorldName()),
              " Jogadores: §a" + getArena().getArenaPlayers().size() + "/" + getArena().getConfiguration().getMaxPlayers(), "", " Iniciando em §a" + this.time + "s", "",
              " Modo: §a" + getArena().getConfiguration().getMode(), "", " §bhylex.net");
          }
        });

        if (this.time <= 0) {
          this.time = 60 * 6;
          getArena().start();
          return;
        }

        if (this.time <= 5) {
          if (this.arena.getState() != STARTING) {
            this.arena.setState(STARTING);
          }

          String color = this.time >= 3 ? "§a§l" : this.time == 2 ? "§6§l" : "§c§l";
          for (IArenaPlayer a : getArena().getArenaPlayers()) {
            ArenaPlayer ap = (ArenaPlayer) a;
            Player player = ap.getPlayer();
            NMS.sendTitle(player, Titles.TitleType.BOTH, "§fPrepare-se para lutar!", color + this.time, 0, 20, 0);
            player.sendMessage("§ePartida iniciando em " + (color.replace("§l", "")) + this.time + (this.time == 1 ? " §esegundo." : " §esegundos."));
            player.playSound(player.getLocation(), Sound.CLICK, 1.2F, 1.2F);
          }
        }

        this.time--;
        break;
    }
  }
}
