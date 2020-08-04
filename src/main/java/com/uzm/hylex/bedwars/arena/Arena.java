package com.uzm.hylex.bedwars.arena;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.uzm.hylex.bedwars.Core;
import com.uzm.hylex.bedwars.arena.creator.inventory.PlayersMenu;
import com.uzm.hylex.bedwars.arena.enums.ArenaEnums;
import com.uzm.hylex.bedwars.arena.generators.Generator;
import com.uzm.hylex.bedwars.arena.management.ArenaBlocks;
import com.uzm.hylex.bedwars.arena.management.ArenaConfiguration;
import com.uzm.hylex.bedwars.arena.player.ArenaPlayer;
import com.uzm.hylex.bedwars.arena.team.Team;
import com.uzm.hylex.bedwars.arena.team.Teams;
import com.uzm.hylex.bedwars.controllers.HylexPlayerController;
import com.uzm.hylex.bedwars.proxy.ServerItem;
import com.uzm.hylex.bedwars.utils.PlayerUtils;
import com.uzm.hylex.core.api.Group;
import com.uzm.hylex.core.api.HylexPlayer;
import com.uzm.hylex.core.api.interfaces.Enums;
import com.uzm.hylex.core.api.interfaces.IArena;
import com.uzm.hylex.core.api.interfaces.IArenaPlayer;
import com.uzm.hylex.core.controllers.FakeController;
import com.uzm.hylex.core.controllers.TagController;
import com.uzm.hylex.core.java.util.configuration.ConfigurationCreator;
import com.uzm.hylex.core.nms.NMS;
import com.uzm.hylex.core.party.BukkitParty;
import com.uzm.hylex.core.party.BukkitPartyManager;
import com.uzm.hylex.core.spigot.features.SpigotFeatures;
import com.uzm.hylex.core.spigot.features.Titles;
import com.uzm.hylex.core.spigot.location.LocationSerializer;
import com.uzm.hylex.core.spigot.utils.BukkitUtils;
import com.uzm.hylex.core.utils.CubeId;
import com.uzm.hylex.services.lan.WebSocket;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.JSONObject;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.uzm.hylex.bedwars.arena.player.ArenaPlayer.CurrentState.*;

public class Arena implements IArena {


  private int restartCount;

  private String name;
  private String worldName;

  private ArenaConfiguration configuration;
  private LinkedHashSet<IArenaPlayer> arenaPlayers = new LinkedHashSet<>();

  private Enums.ArenaState state;
  private ArenaEnums.Events eventState;

  private Map<Teams, Team> teams = new LinkedHashMap<>();
  private ConfigurationCreator folder;

  private Location waitingLocation;
  private Location spectatorLocation;

  private ArenaTask mainTask;
  private ArenaBlocks blocks;
  private CubeId borders;
  private CubeId waitingLocationBorder;

  private List<CubeId> cantConstruct = new ArrayList<>();
  private List<Generator> generators = new ArrayList<>();

  public static List<String> MODES = Lists.newArrayList("solo", "dupla", "trio", "squad");

  private PlayersMenu spectatorMenu;

  public Arena(String name, boolean newArena) {
    if (!newArena) {
      this.name = name;
      this.mainTask = new ArenaTask(this);
      this.blocks = new ArenaBlocks(this);
      this.folder = ConfigurationCreator.find(name, Core.getInstance());
      assert folder != null;
      this.configuration = new ArenaConfiguration(folder.get().getInt("configuration.max-players"), folder.get().getInt("configuration.min-start-players"),
        folder.get().getInt("configuration.islands"), folder.get().getInt("configuration.teams-size"));

      for (int x = 0; x < getConfiguration().getIslands(); x++) {
        teams.put(Teams.values()[x], new Team(Teams.values()[x]));
      }

      getCantConstruct().addAll(folder.get().getStringList("security.protected").stream().map(CubeId::new).collect(Collectors.toList()));
      this.borders = new CubeId(folder.get().getString("security.border"));
      if (Objects.nonNull(folder.get().getString("security.waitingLocationBorder")))
        this.waitingLocationBorder = new CubeId(folder.get().getString("security.waitingLocationBorder"));
      for (String teams : folder.get().getConfigurationSection("locations.teams").getKeys(false)) {
        ConfigurationSection team = folder.get().getConfigurationSection("locations.teams." + teams);
        Team t = this.teams.get(Teams.valueOf(teams.toUpperCase()));

        t.setBorder(new CubeId(team.getString("borders")));
        t.setShopLocation(new LocationSerializer(team.getString("shop")).unserialize());
        t.setSpawnLocation(new LocationSerializer(team.getString("spawn")).unserialize());
        t.setUpgradeLocation(new LocationSerializer(team.getString("upgrade")).unserialize());
        t.setBedLocation(new LocationSerializer(team.getString("bedlocation")).unserialize());
        t.setTeamGenerators(team.getStringList("team-generators").stream().map(result -> new LocationSerializer(result).unserialize()).collect(Collectors.toList()));
      }

      setGenerators(folder.get().getStringList("locations.generators").stream()
        .map(result -> new Generator(this, Generator.Type.valueOf(result.split(" \\| ")[0].toUpperCase()), new LocationSerializer(result.split(" \\| ")[1]).unserialize()))
        .collect(Collectors.toList()));
      setWaitingLocation(new LocationSerializer(folder.get().getString("locations.map.waiting")).unserialize());
      setSpectatorLocation(new LocationSerializer(folder.get().getString("locations.map.spectator")).unserialize());

      setWorldName(folder.get().getString("configuration.worldName"));

      setState(Enums.ArenaState.IN_WAITING);
      setEventState(ArenaEnums.Events.IDLE);

      spectatorMenu = new PlayersMenu("§7Jogadores", 3, this);

    } else {
      this.name = name;
      this.configuration = new ArenaConfiguration(4, 3, 4, 1);

      for (int x = 0; x < Teams.values().length; x++) {
        teams.put(Teams.values()[x], new Team(Teams.values()[x]));
      }
    }
  }

  public void destroy() {
    this.name = null;
    this.worldName = null;
    this.configuration = null;
    this.arenaPlayers.clear();
    this.arenaPlayers = null;
    this.state = null;
    this.eventState = null;
    this.teams.clear();
    this.teams = null;
    this.waitingLocation = null;
    this.spectatorLocation = null;
    this.folder = null;
    this.mainTask.destroy();
    if (this.mainTask != null)
      mainTask.cancel();
    this.mainTask = null;
    this.blocks = null;
    this.borders = null;
    this.waitingLocationBorder = null;
    this.cantConstruct.clear();
    this.generators.clear();
    this.spectatorMenu = null;
  }

  public void setRestartCount(int restartCount) {
    this.restartCount = restartCount;
  }

  public int getRestartCount() {
    return restartCount;
  }

  public boolean canJoin(HylexPlayer hp) {
    Player player = hp.getPlayer();
    if ((this.state.isLocked() || this.state.isInGame())) {
      if (!player.hasPermission("hylex.staff")) {
        player.sendMessage("§cNão existe partidas disponíveis no momento para o modo de jogo selecionado.");
      } else {
        player.sendMessage("§7Você esta assistindo a partida §b" + com.uzm.hylex.core.Core.SOCKET_NAME.replace("bedwars-", "") + "-" + getArenaName());
      }
      return false;
    } else {
      if (this.getPlayingPlayers().size() + 1 > getConfiguration().getMaxPlayers()) {
        if (!player.hasPermission("hylex.staff")) {
          player.sendMessage("§cNão existe partidas disponíveis no momento para o modo de jogo selecionado.");
        } else {
          player.sendMessage("§7Você esta assistindo a partida §b" + com.uzm.hylex.core.Core.SOCKET_NAME.replace("bedwars-", "") + "-" + getArenaName());
        }
        return false;
      } else {
        return true;
      }
    }

  }

  public void spec(HylexPlayer hp) {

    hp.setupPlayerWihoutTag();
    if (hp.isInvisible()) {
      hp.setVisibility(true);
    }
    Player player = hp.getPlayer();
    if (hp.getArenaPlayer() != null) {
      if (((ArenaPlayer) hp.getArenaPlayer()).getTeam() != null) {
        if (((ArenaPlayer) hp.getArenaPlayer()).getTeam().getTeam() != null) {
          ((ArenaPlayer) hp.getArenaPlayer()).getTeam().getTeam().removePlayer(player);
        }
      }
      hp.getArenaPlayer().destroy();
    }
    ArenaPlayer ap = new ArenaPlayer(hp, this);
    hp.setArenaPlayer(ap);
    TagController.remove(player);
    if (FakeController.has(player.getName())) {
      TagController tag = TagController.create(player);
      tag.setPrefix(Group.NORMAL.getColor());
      tag.setOrder(Group.NORMAL.getOrder());
      player.setDisplayName(Group.NORMAL.getDisplay() + player.getName());
      tag.update();
    } else {
      TagController tag = TagController.create(player);

      tag.setPrefix(hp.getGroup().getColor());
      tag.setOrder(hp.getGroup().getOrder());
      player.setDisplayName(hp.getGroup().getDisplay() + player.getName());
      tag.update();
    }

    if (this.state.isLocked() || this.state.isInGame()) {
      JSONObject json = new JSONObject();
      json.put("miniName", getArenaName());
      json.put("nickname", hp.getPlayer().getName());
      WebSocket.get("core-" + com.uzm.hylex.core.Core.SOCKET_NAME).getSocket().emit("save-mini", json);

      this.arenaPlayers.add(ap);
      ap.setCurrentState(SPECTATING);
      ap.update();
      player.teleport(getSpectatorLocation());

      for (Player pls : Bukkit.getOnlinePlayers()) {
        pls.hidePlayer(player);
      }
    }
  }

  @Override
  public void join(HylexPlayer hp) {
    if (hp.isInvisible()) {
      hp.setVisibility(true);
    }
    hp.setupPlayerWihoutTag();
    HylexPlayerController.setupHotbar(hp);
    Player player = hp.getPlayer();
    if (hp.getArenaPlayer() != null) {
      if (((ArenaPlayer) hp.getArenaPlayer()).getTeam() != null) {
        if (((ArenaPlayer) hp.getArenaPlayer()).getTeam().getTeam() != null) {
          ((ArenaPlayer) hp.getArenaPlayer()).getTeam().getTeam().removePlayer(player);
        }
      }
      hp.getArenaPlayer().destroy();
    }
    ArenaPlayer ap = new ArenaPlayer(hp, this);
    hp.setArenaPlayer(ap);
    ap.setTeam(null);
    TagController.remove(player);
    if (FakeController.has(player.getName())) {
      TagController tag = TagController.create(player);
      tag.setPrefix(Group.NORMAL.getColor());
      tag.setOrder(Group.NORMAL.getOrder());
      player.setDisplayName(Group.NORMAL.getDisplay() + player.getName());
      tag.update();
    } else {
      TagController tag = TagController.create(player);

      tag.setPrefix(hp.getGroup().getColor());
      tag.setOrder(hp.getGroup().getOrder());
      player.setDisplayName(hp.getGroup().getDisplay() + player.getName());
      tag.update();
    }
    JSONObject json = new JSONObject();
    json.put("miniName", getArenaName());
    json.put("nickname", hp.getPlayer().getName());
    WebSocket.get("core-" + com.uzm.hylex.core.Core.SOCKET_NAME).getSocket().emit("save-mini", json);

    if (this.state.isLocked() || this.state.isInGame()) {
      if (hp.getPlayer().hasPermission("hylex.staff")) {
        this.arenaPlayers.add(ap);
        ap.setCurrentState(SPECTATING);
        ap.update();
        player.teleport(getSpectatorLocation());
        NMS.sendTabColor(player, "\n §6§lREDE STONE \n    §7Seja bem-vindo §E" + player.getName() + "§7." + "\n",
          "\n §7Seu grupo é: " + hp.getGroup().getName() + "\n§7Você está em: §fBedWars - " + getArenaName() + "\n\n§6§nredestone.com§r \n ");
        for (Player pls : Bukkit.getOnlinePlayers()) {
          pls.hidePlayer(player);
        }
      } else {
        player.sendMessage("§cDesculpe, mas você não poderia entrar nessa sala, tente entrar em outra!");
        player.kickPlayer("§cVocê não possue permissão no momento para\nacessar essa sala.");

      }
      return;
    }
    if (getPlayingPlayers().size() >= getConfiguration().getMaxPlayers()) {
      setState(Enums.ArenaState.FULL);
    } else if (getPlayingPlayers().size() < getConfiguration().getMaxPlayers() && getState() == Enums.ArenaState.FULL) {
      setState(Enums.ArenaState.STARTING);
    }
    if (getMainTask().getTime() > 5 && getPlayingPlayers().size() == getConfiguration().getMinPlayers()) {
      getMainTask().setTime(5);
    }
    if (player != null) {
      this.arenaPlayers.add(ap);
      player.setAllowFlight(false);
      player.teleport(this.getWaitingLocation());
      for (Player players : Bukkit.getOnlinePlayers()) {
        if (player.getWorld().equals(players.getWorld())) {
          player.showPlayer(players);
          players.showPlayer(player);
        } else {
          player.hidePlayer(players);
          players.hidePlayer(player);
        }
      }

      if (hp.getGroup() == null) {
        if (hp.getPlayer() != null)
          NMS.sendTabColor(player, "\n §6§lREDE STONE \n    §7Seja bem-vindo §E" + player.getName() + "§7." + "\n §7Seu grupo é: ",
            "???" + "\n§7Você está em: §fBedWars - " + getArenaName() + "\n\n§6§nredestone.com§r \n ");

      } else {
        if (hp.getPlayer() != null)
          NMS.sendTabColor(player, "\n §6§lREDE STONE \n    §7Seja bem-vindo §E" + player.getName() + "§7." + "\n §7Seu grupo é: ",
            hp.getGroup().getName() + "\n§7Você está em: §fBedWars - " + getArenaName() + "\n\n§6§nredestone.com§r \n ");

      }
    }
    if (this.state != Enums.ArenaState.IN_GAME && this.state != Enums.ArenaState.END) {
      getArenaPlayers().stream().map(a -> ((ArenaPlayer) a).getPlayer())
        .forEach(players -> players.sendMessage(player.getDisplayName() + " §eentrou! §a(" + getPlayingPlayers().size() + "/" + this.getConfiguration().getMaxPlayers() + ")"));
    }
  }

  @Override
  public void leave(HylexPlayer hylexPlayer) {

    Player player = hylexPlayer.getPlayer();


    ArenaPlayer ap = (ArenaPlayer) hylexPlayer.getArenaPlayer();

    this.arenaPlayers.remove(ap);

    if (ap.getCurrentState().isInGame() && this.getState().isInGame()) {
      List<HylexPlayer> hitters = hylexPlayer.getLastHitters();
      ap.setCurrentState(IN_GAME);
      this.kill(hylexPlayer, hitters.size() > 0 ? hitters.get(0) : null, Team.Sitation.ELIMINATED);
      return;
    }

    if (!this.state.isInGame() && this.state != Enums.ArenaState.END && this.state != Enums.ArenaState.IDLE) {
      getArenaPlayers().stream().map(a -> ((ArenaPlayer) a).getPlayer())
        .forEach(players -> players.sendMessage(player.getDisplayName() + " §csaiu! §a(" + getPlayingPlayers().size() + "/" + this.getConfiguration().getMaxPlayers() + ")"));
    }
    if (getPlayingPlayers().size() < getConfiguration().getMaxPlayers() && getState() == Enums.ArenaState.FULL) {
      setState(Enums.ArenaState.STARTING);
    }
    if (ap.getTeam() != null) {
      if (ap.getTeam().getTeam() != null) {
        ap.getTeam().getTeam().removePlayer(player);
      }
    }
    ap.destroy();
    TagController.remove(player);
  }

  public void kill(HylexPlayer hp, HylexPlayer hpk) {
    this.kill(hp, hpk, ((ArenaPlayer) hp.getArenaPlayer()).getTeam().getSitation());
  }

  public void kill(HylexPlayer hp, HylexPlayer hpk, Team.Sitation sitation) {

    Player player = hp.getPlayer();
    ArenaPlayer ap = (ArenaPlayer) hp.getArenaPlayer();


    if (!this.state.isInGame()) {
      return;
    }

    Team team = ap.getTeam();
    Player killer = hpk != null ? hpk.getPlayer() : null;

    ArenaPlayer apk = hpk != null ? (ArenaPlayer) hpk.getArenaPlayer() : null;
    if (player.equals(killer) || apk == null || !this.equals(apk.getArena())) {
      killer = null;
    }
    if (apk != null && !apk.getCurrentState().isInGame()) {
      killer = null;
    }

    if (killer != null) {
      PlayerUtils.giveResources(player, killer);
      killer.playSound(killer.getLocation(), Sound.ORB_PICKUP, 1.0F, 1.0F);
    }

    String message;
    if (killer == null) {
      if (player.getLastDamageCause() != null && player.getLastDamageCause().getCause() == EntityDamageEvent.DamageCause.VOID) {
        message = player.getDisplayName() + " §7se perdeu no vazio.";
      } else {
        message = player.getDisplayName() + " §7morreu.";
      }
    } else {
      if (player.getLastDamageCause() != null && player.getLastDamageCause().getCause() == EntityDamageEvent.DamageCause.VOID) {
        message = player.getDisplayName() + " §7foi jogado no vazio por " + killer.getDisplayName() + "§7.";
      } else if (player.getLastDamageCause() != null && player.getLastDamageCause() instanceof EntityDamageByEntityEvent && ((EntityDamageByEntityEvent) player
        .getLastDamageCause()).getDamager() instanceof Arrow) {
        message = player.getDisplayName() + " §7foi acertado pelo arco de " + killer.getDisplayName() + "§7.";
      } else {
        message = player.getDisplayName() + " §7foi morto por " + killer.getDisplayName() + "§7.";
      }
    }

    getArenaPlayers().stream().map(a -> ((ArenaPlayer) a).getPlayer()).forEach(players -> {
      if (players != null)
        players.sendMessage(message + (sitation == Team.Sitation.BROKEN_BED ? " §b§lKILL FINAL!" : ""));
    });
    if (sitation == Team.Sitation.STANDING) {
      if (hp.getBedWarsStatistics() != null) {
        hp.getBedWarsStatistics().addLong("deaths", "global");
        if (MODES.contains(getConfiguration().getMode().toLowerCase()))
          hp.getBedWarsStatistics().addLong("deaths", getConfiguration().getMode().toLowerCase());
      }
      if (hpk != null) {
        if (apk != null) {
          if (apk.getKillSequence() == 0) {
            apk.setKillSequence(1);
            apk.setLastKillTimeStamp(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(20));
            apk.setLastKill(player);
          } else {
            if (apk.getLastKillTimeStamp() != 0) {
              if (System.currentTimeMillis() <= apk.getLastKillTimeStamp() && (apk.getLastKill() == null || player != apk.getLastKill())) {
                apk.setLastKill(player);
                apk.setLastKillTimeStamp(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(20));
                apk.addKillSequence();
                String[] messages = {"§b%s §efez um §b§lDOUBLE-KILL§e!", "§b%s §efez um §a§lTRIPLE-KILL§e!", "§b%s §efez um §c§lQUADRA-KILL§e!", "§b%s §efez um §4§lPENTA-KILL§e!",
                  "§b%s §eestá §5§lIMPLACÁVEL§e!", "§b%s §eestá §d§lLENDÁRIO§e!"};
                if (apk.getKillSequence() >= 2 && apk.getKillSequence() <= 7) {
                  getArenaPlayers().stream().map(a -> ((ArenaPlayer) a).getPlayer()).forEach(players -> {
                    if (players != null) {

                      players.sendMessage("");
                      players.sendMessage(String.format(messages[apk.getKillSequence() - 2], apk.getPlayer().getName()));
                      players.sendMessage("");
                      if (apk.getKillSequence() == 7) {
                        players.playSound(players.getLocation(), Sound.HORSE_SKELETON_DEATH, 1.5F, 1.5F);
                        SpigotFeatures.sendTitle(players, Titles.TitleType.BOTH, "§d§lLENDÁRIO", "§7" + apk.getPlayer().getName(), 20, 200, 20);
                      }
                    }
                  });
                }
              } else {
                apk.setKillSequence(0);
                apk.setLastKillTimeStamp(0);
                apk.setLastKill(null);
              }
            } else {
              apk.setKillSequence(0);
              apk.setLastKill(null);
            }
          }
          apk.addKills();
        }
        if (hp.getBedWarsStatistics() != null) {
          hpk.getBedWarsStatistics().addLong("kills", "global");
          if (MODES.contains(getConfiguration().getMode().toLowerCase()))
            hpk.getBedWarsStatistics().addLong("kills", getConfiguration().getMode().toLowerCase());
        }
      }
      Bukkit.getScheduler().scheduleSyncDelayedTask(Core.getInstance(), () -> {
        player.setFireTicks(0);
        player.getActivePotionEffects().forEach(pe -> player.removePotionEffect(pe.getType()));

        player.teleport(getBorders().getCenterLocation());
        getArenaPlayers().stream().map(a -> ((ArenaPlayer) a).getPlayer()).forEach(players -> players.hidePlayer(player));

        ap.setCurrentState(ArenaPlayer.CurrentState.RESPAWNING);
        ap.update();
      }, 3);
      return;
    }
    if (hp.getBedWarsStatistics() != null) {
      hp.getBedWarsStatistics().addLong("finalDeaths", "global");
      if (MODES.contains(getConfiguration().getMode().toLowerCase()))
        hp.getBedWarsStatistics().addLong("finalDeaths", getConfiguration().getMode().toLowerCase());
    }

    if (hpk != null) {
      if (hpk.getBedWarsStatistics() != null) {
        hpk.getBedWarsStatistics().addLong("finalKills", "global");
        if (MODES.contains(getConfiguration().getMode().toLowerCase()))
          hpk.getBedWarsStatistics().addLong("finalKills", getConfiguration().getMode().toLowerCase());

        hpk.getBedWarsStatistics().addLong("coins", "global", HylexPlayerController.giveCoin(player, hpk, 25, "§6Você ganhou %s Bedwars Coins (Abate final)"));
        hpk.getBedWarsStatistics().addLong("exp", "global", HylexPlayerController.giveExp(player, hpk, 15, "§bVocê ganhou §f%s §bde experiência do bedwars (Abate final)"));

      }
      ((ArenaPlayer) hpk.getArenaPlayer()).addFinalKill();
    }
    Bukkit.getScheduler().scheduleSyncDelayedTask(Core.getInstance(), () -> {
      if (player.isOnline()) {
        player.setFireTicks(0);
        player.getActivePotionEffects().forEach(pe -> player.removePotionEffect(pe.getType()));

        player.teleport(getSpectatorLocation());
      }
      ap.setCurrentState(DEAD);
      ap.update();
      ap.setCurrentState(SPECTATING);
      ap.update();
      ap.rewardSumary();
      if (team != null) {
        team.updateAlive();
        if (team.getAlive().isEmpty()) {
          team.breakBed();
          team.setSitation(Team.Sitation.ELIMINATED);
          getArenaPlayers().stream().map(a -> ((ArenaPlayer) a).getPlayer()).forEach(
            players -> players.sendMessage(" \n§f§lTIME ELIMINADO > §cO " + team.getTeamType().getTagColor() + "Time " + team.getTeamType().getName() + " §cfoi eliminado!\n "));
        }
      }

      this.check();
    }, 3);
  }

  public void destroyBed(Team team, HylexPlayer hp) {
    team.breakBed();
    if (hp != null) {
      ArenaPlayer breaker = (ArenaPlayer) hp.getArenaPlayer();
      if (hp.getBedWarsStatistics() != null) {
        hp.getBedWarsStatistics().addLong("bedsBroken", "global");
        if (MODES.contains(getConfiguration().getMode().toLowerCase()))
          hp.getBedWarsStatistics().addLong("bedsBroken", getConfiguration().getMode().toLowerCase());
        hp.getBedWarsStatistics().addLong("coins", "global", HylexPlayerController.giveCoin(breaker.getPlayer(), hp, 40, "§6Você ganhou %s Bedwars Coins (Cama quebrada)"));
      }
      ((ArenaPlayer) hp.getArenaPlayer()).addBedBroken();


      for (IArenaPlayer a : this.getPlayingPlayers()) {
        ArenaPlayer ap = (ArenaPlayer) a;
        Player player = ap.getPlayer();

        if (team.equals(ap.getTeam())) {
          player.sendMessage(" \n§f§lCAMA DESTRUIDA > §7Sua cama foi destruída por " + breaker.getPlayer().getDisplayName() + "§7.\n ");
          NMS.sendTitle(player, Titles.TitleType.BOTH, "§fVocê não irá mais renascer", "§c§lSUA CAMA FOI DESTRUIDA", 10, 60, 10);
        } else {
          player.sendMessage(
            " \n§f§lCAMA DESTRUIDA > §7A cama do " + team.getTeamType().getTagColor() + "Time " + team.getTeamType().getName() + " §7foi destruída por " + breaker.getPlayer()
              .getDisplayName() + "§7.\n ");
        }

        player.playSound(player.getLocation(), Sound.ENDERDRAGON_GROWL, 1.0F, 1.0F);
      }
    }
  }


  public CubeId getWaitingLocationBorder() {
    return waitingLocationBorder;
  }

  public void setWaitingLocationBorder(CubeId waitingLocationBorder) {
    this.waitingLocationBorder = waitingLocationBorder;
  }

  public void start() {
    this.mainTask.reset();
    getGenerators().forEach(Generator::enable);
    this.setState(Enums.ArenaState.PRE_GAME);
    for (IArenaPlayer aps : getArenaPlayers()) {
      ArenaPlayer ap = (ArenaPlayer) aps;
      Player player = ap.getPlayer();
      if (ap.getCurrentState() != SPECTATING) {
        if (ap.getTeam() == null) {
          BukkitParty party = BukkitPartyManager.getMemberParty(player.getName());
          if (party != null) {
            if (Bukkit.getPlayerExact(party.getLeader()) != null) {
              if (getConfiguration().getTeamsSize() > 1 && party.listMembers().stream().map(result -> Bukkit.getPlayerExact(result.getName())).count() > 1) {
                Team team = findTeamParty(ap, BukkitPartyManager.getMemberParty(player.getName()));
                if (team == null) {
                  findTeam(ap);
                }
              } else {
                findTeam(ap);
              }

            } else {
              findTeam(ap);
            }
          } else {
            Team team =findTeam(ap);
            if (team == null) {
              findTeam(ap);
            }
          }
        }


        player.setLevel(HylexPlayerController.getLevel(HylexPlayer.getByPlayer(player)));
        player.setExp(((float) HylexPlayerController.getExp(HylexPlayer.getByPlayer(player)) / 5000.0F));


        HylexPlayer hp = HylexPlayer.getByPlayer(player);
        hp.getBedWarsStatistics().addLong("games", "global");
        if (MODES.contains(getConfiguration().getMode().toLowerCase()))
          hp.getBedWarsStatistics().addLong("games", getConfiguration().getMode().toLowerCase());

        player.sendMessage("§a§m-------------------------------------------");
        player.sendMessage("                  §e§n§lBed Wars");
        player.sendMessage("");
        player.sendMessage(" §7Proteja sua cama e destrua as camas inimigas.");
        player.sendMessage(" §7Adquira melhorias para você e sua equipe coletando");
        player.sendMessage(" §fFerro§7, §6Ouro§7, §bDiamante§7, e §2Esmeralda §7dos geradores");
        player.sendMessage(" §7para vencer a luta contra os seus oponentes.");
        player.sendMessage("");
        player.sendMessage("§a§m-------------------------------------------");
        player.sendMessage("§aPartida iniciada, boa sorte a todos os times.");

        if (ap.getTeam() == null) {
          player.kickPlayer(
            " \n§cAparentemente o servidor não conseguiu carregar seu Perfil.\n \n§cIsso ocorre normalmente quando o servidor ainda está despreparado para receber logins, aguarde um pouco e tente novamente.");

          return;
        }
        if (hp.getBedWarsStatistics() != null) {
          ap.setExpEarned((int) hp.getBedWarsStatistics().getLong("exp", "global"));
          ap.setCoinsEarned((int) hp.getBedWarsStatistics().getLong("coins", "global"));
          ap.setLevelEarned(HylexPlayerController.getLevel(hp));
        }
        ap.setStartedTime(System.currentTimeMillis());

        ap.setCurrentState(ArenaPlayer.CurrentState.IN_GAME);
        ap.update();

        player.setDisplayName(ap.getTeam().getTeamType().getTagColor() + player.getName());

        TagController.remove(player);

        player.closeInventory();
        player.getEnderChest().clear();
        player.teleport(ap.getTeam().getSpawnLocation());
        player.playSound(player.getLocation(), Sound.LEVEL_UP, 1.5F, 1.5F);

        NMS.sendTitle(player, Titles.TitleType.BOTH, "§fLutem!", "§a§lBED WARS", 10, 10, 10);
      } else {
        player.closeInventory();
        player.teleport(spectatorLocation);
      }
    }

    for (Team teams : listTeams()) {
      teams.registerTeam(getArenaName());
      teams.enableHolograms();
      if (teams.getMembers().size() == 0) {
        teams.setSitation(Team.Sitation.ELIMINATED);
        BukkitUtils.getBedNeighbor(teams.getBedLocation().getBlock()).breakNaturally(new ItemStack(Material.AIR));
        BukkitUtils.getBedNeighbor(teams.getBedLocation().clone().add(1, 0, 0).getBlock()).breakNaturally(new ItemStack(Material.AIR));
        BukkitUtils.getBedNeighbor(teams.getBedLocation().clone().add(0, 0, 1).getBlock()).breakNaturally(new ItemStack(Material.AIR));
        BukkitUtils.getBedNeighbor(teams.getBedLocation().clone().add(-1, 0, 0).getBlock()).breakNaturally(new ItemStack(Material.AIR));
        BukkitUtils.getBedNeighbor(teams.getBedLocation().clone().add(0, 0, 1).getBlock()).breakNaturally(new ItemStack(Material.AIR));
        teams.getBedLocation().getBlock().breakNaturally(new ItemStack(Material.AIR));
        continue;
      }
      teams.setSitation(Team.Sitation.STANDING);
      teams.addAlives();
    }
    this.setState(Enums.ArenaState.IN_GAME);
    this.setEventState(ArenaEnums.Events.DIAMOND_II);

    this.check();

    if (getWaitingLocationBorder() != null) {
      new BukkitRunnable() {
        @Override
        public void run() {
          for (Iterator<Block> blockIterator = getWaitingLocationBorder().iterator(); blockIterator.hasNext(); ) {
            Block b = blockIterator.next();
            if (b.getType() != Material.AIR) {
              b.setType(Material.AIR);
            }
          }
        }
      }.runTaskLater(Core.getInstance(), 20);
    }
    System.gc();
  }

  public void check() {
    if (this.state == Enums.ArenaState.IN_GAME) {
      if (listTeams().stream().filter(t -> t.getSitation() != Team.Sitation.ELIMINATED).count() <= 1) {
        this.stop(listTeams().stream().filter(t -> t.getSitation() != Team.Sitation.ELIMINATED).findFirst().orElse(null));
      }
    }
  }

  public void stop(Team team) {

    this.setState(Enums.ArenaState.END);
    this.mainTask.reset();
    getArenaPlayers().stream().map(a -> ((ArenaPlayer) a).getPlayer()).forEach(players -> {
      if (players != null) {
        if (FakeController.has(players.getName())) {
          players.setDisplayName(Group.NORMAL.getDisplay() + players.getName());
        } else {
          players.setDisplayName(HylexPlayer.getByPlayer(players).getGroup().getDisplay() + players.getName());
        }

      }
    });
    if (team == null) {
      getArenaPlayers().stream().filter(Objects::nonNull).map(a -> (ArenaPlayer) a).forEach(ap -> {
        ap.setCurrentState(SPECTATING);
        ap.update();
        ap.getPlayer().sendMessage(" \n§7Não houve ganhadores, finalizando partida!\n ");
      });
    } else {
      team.getMembers().forEach(ap -> {
        Player player = ap.getPlayer();

        if (player != null) {
          HylexPlayer hp = HylexPlayer.getByPlayer(player);
          if (hp != null) {
            if (hp.getBedWarsStatistics() != null) {
              hp.getBedWarsStatistics().addLong("wins", "global");
              if (MODES.contains(getConfiguration().getMode().toLowerCase()))
                hp.getBedWarsStatistics().addLong("wins", getConfiguration().getMode().toLowerCase());

              hp.getBedWarsStatistics().addLong("coins", "global", HylexPlayerController.giveCoin(player, hp, 100, "§6Você ganhou %s Bedwars Coins (Vitória)"));
              hp.getBedWarsStatistics().addLong("exp", "global", HylexPlayerController.giveExp(player, hp, 26 * team.getMembers().size(),
                "§bVocê ganhou §f%s §bde experiência do bedwars (" + (team.getMembers().size() == 1 ? "Vitória" : "Vitória em equipe") + ")"));
            }
          }
          NMS.sendTitle(player, Titles.TitleType.BOTH, "", "§6§lVITÓRIA", 10, 60, 10);

        }
        ap.setCurrentState(SPECTATING);
        ap.update();
        ap.rewardSumary();

      });
      getArenaPlayers().stream().map(a -> ((ArenaPlayer) a).getPlayer()).forEach(players -> {
        if (players != null) {
          players.sendMessage(" \n§7O " + team.getTeamType().getTagColor() + "Time " + team.getTeamType().getName() + " §7foi vitorioso!\n ");
        }
      });
    }

    new BukkitRunnable() {
      private int time = 10;

      @Override
      public void run() {
        if (time <= 0) {
          Bukkit.getWorld(getArenaName()).getPlayers().forEach(p -> p.kickPlayer("§cPartida encerrada!"));
          Bukkit.getScheduler().scheduleSyncDelayedTask(Core.getInstance(), () -> {
            blocks.clearArena();
            arenaPlayers.clear();
            teams.clear();

          }, 40L);
          cancel();
          return;
        }

        if (time == 2) {
          getArenaPlayers().stream().map(a -> ((ArenaPlayer) a).getPlayer()).forEach(players -> {
            if (players != null) {
              ServerItem.getServerItem("lobby").connect(HylexPlayer.getByPlayer(players));
            }
          });
        }

        if (team != null) {
          for (IArenaPlayer a : getArenaPlayers()) {
            ArenaPlayer ap = (ArenaPlayer) a;
            if (ap.getTeam() != null && ap.getTeam().equals(team)) {
              Player player = ap.getPlayer();
              Firework fire = (Firework) player.getWorld().spawnEntity(player.getLocation(), EntityType.FIREWORK);
              FireworkMeta meta = fire.getFireworkMeta();
              int random = new Random().nextInt(5) + 1;
              Color color = random == 1 ? Color.BLUE : random == 2 ? Color.RED : random == 3 ? Color.GREEN : random == 4 ? Color.MAROON : Color.ORANGE;
              meta.addEffect(FireworkEffect.builder().withColor(color).with(FireworkEffect.Type.STAR).build());
              meta.setPower(1);
              fire.setFireworkMeta(meta);
            }
          }
        }
        time--;
      }
    }.runTaskTimer(Core.getInstance(), 0, 20);
  }

  public ArenaConfiguration getConfiguration() {
    return configuration;
  }

  @Override
  public List<IArenaPlayer> getArenaPlayers() {
    return getState() == Enums.ArenaState.IN_GAME || getState() == Enums.ArenaState.END ?
      this.arenaPlayers.stream().filter(Objects::nonNull).filter(ap -> ((ArenaPlayer) ap).getPlayer() != null).filter(ap -> ((ArenaPlayer) ap).getTeam() != null).collect(Collectors.toList()) :
      this.arenaPlayers.stream().filter(Objects::nonNull).filter(ap -> ((ArenaPlayer) ap).getPlayer() != null).collect(Collectors.toList());
  }

  @Override
  public List<IArenaPlayer> getPlayingPlayers() {
    return this.arenaPlayers.stream().filter(Objects::nonNull).map(a -> (ArenaPlayer) a).filter(a -> a.getPlayer() != null).filter(result -> result.getCurrentState() != SPECTATING)
      .collect(Collectors.toList());
  }

  @Override
  public void setState(Enums.ArenaState state) {
    this.state = state;
  }

  public Enums.ArenaState getState() {
    return this.state;
  }

  public void setEventState(ArenaEnums.Events upgrade) {
    this.eventState = upgrade;
  }

  public ArenaEnums.Events getUpgradeState() {
    return eventState;
  }

  public Team findTeam(ArenaPlayer ap) {
    Team team = listTeams().stream().filter(result -> (result.getMembers().size()) < getConfiguration().getTeamsSize()).findFirst().orElse(null);
    if (team != null) {
      ap.setTeam(team);
      team.addMember(ap, false);
    }
    return team;
  }

  public Team findTeamParty(ArenaPlayer ap, BukkitParty party) {
    List<Player> leftPlayers =
      party.listMembers().stream().filter(name -> Bukkit.getPlayerExact(name.getName()) != null).map(name -> Bukkit.getPlayerExact(name.getName())).collect(Collectors.toList());
    if (leftPlayers.size() <= getConfiguration().getTeamsSize()) {
      for (Team team1 : listEachTeams()) {
        List<Player> membersName = team1.getMembers().stream().map(ArenaPlayer::getPlayer).collect(Collectors.toList());
        if (party.getLockedOnParty().size() >= leftPlayers.size()) {
          break;
        }
        if (membersName.size() == 0) {
          for (Player player : leftPlayers) {
            if (team1.getMembers().size() == getConfiguration().getTeamsSize())
              continue;
            if (!team1.getMembers().stream().map(ArenaPlayer::getPlayer).collect(Collectors.toList()).contains(player)) {
              if (ap.getPlayer() == player) {
                team1.addMember(ap, false);
                ap.setTeam(team1);
                party.getLockedOnParty().add(ap.getPlayer());
              } else {
                if (HylexPlayer.getByPlayer(player) != null) {
                  if (HylexPlayer.getByPlayer(player).getArenaPlayer() != null) {
                    team1.addMember((ArenaPlayer) HylexPlayer.getByPlayer(player).getArenaPlayer(), false);
                    ((ArenaPlayer) HylexPlayer.getByPlayer(player).getArenaPlayer()).setTeam(team1);
                    party.getLockedOnParty().add(player);
                  }
                }
              }
            }
          }

        }
      }
    }

    party.getLockedOnParty().clear();
    if (ap.getTeam() == null) {
      return null;
    } else {
      return ap.getTeam();
    }

    /*
    List<String> partyMembers = party.listMembers().stream().map(PartyPlayer::getName).collect(Collectors.toList());
    Team team = listEachTeams().stream().filter(result -> result.getMembers().size() < getConfiguration().getTeamsSize()).filter(result -> {
      long count = result.getMembers().stream().filter(member -> partyMembers.contains(party.getLeader())).count();
      return result.getMembers().size() == 0 || count > 0;
    }).findFirst().orElse(null);
    if (team != null) {
      ap.setTeam(team);
      team.addMember(ap, false);
    }
    */
  }

  public String getArenaName() {
    return name;
  }

  public Location getWaitingLocation() {
    return waitingLocation;
  }

  public Location getSpectatorLocation() {
    return spectatorLocation;
  }

  public void setSpectatorLocation(Location spectatorLocation) {
    this.spectatorLocation = spectatorLocation;
  }

  public void setWaitingLocation(Location waitingLocation) {
    this.waitingLocation = waitingLocation;
  }

  public Map<Teams, Team> getTeams() {
    return this.teams;
  }

  public Collection<Team> listTeams() {
    return ImmutableList.copyOf(this.teams.values());
  }

  public Collection<Team> listEachTeams() {
    return ImmutableList.copyOf(this.teams.values()).stream().limit(getConfiguration().getIslands()).collect(Collectors.toList());
  }

  public ConfigurationCreator getArchive() {
    return folder;
  }

  public ArenaTask getMainTask() {
    return mainTask;
  }

  public ArenaBlocks getBlocks() {
    return blocks;
  }

  public void setFolder(ConfigurationCreator folder) {
    this.folder = folder;
  }

  public ConfigurationCreator getFolder() {
    return folder;
  }

  public void setConfiguration(ArenaConfiguration configuration) {
    this.configuration = configuration;
  }

  public CubeId getBorders() {
    return this.borders;
  }

  public void setWorldName(String worldName) {
    this.worldName = worldName;
  }

  public String getWorldName() {
    return worldName;
  }

  public void setGenerators(List<Generator> generators) {
    this.generators = generators;
  }

  public List<Generator> getGenerators() {
    return this.generators;
  }

  public List<CubeId> getCantConstruct() {
    return this.cantConstruct;
  }

  public void setBorders(CubeId borders) {
    this.borders = borders;
  }

  public boolean isProtected(Location location) {
    return this.getGenerators().stream().anyMatch(generator -> generator.getLocation().clone().subtract(0.5, 0, 0.5).distance(location) < 2) || this.getCantConstruct().stream()
      .anyMatch(cube -> cube.contains(location));
  }

  public boolean isFullyConfigured(int teams) {
    return teams == getConfiguration().getIslands() && getSpectatorLocation() != null && getWaitingLocation() != null && getBorders() != null && getWaitingLocationBorder() != null;
  }

  public PlayersMenu getSpectatorMenu() {
    return spectatorMenu;
  }
}
