package com.uzm.hylex.bedwars.arena;

import com.google.common.collect.ImmutableList;
import com.uzm.hylex.bedwars.Core;
import com.uzm.hylex.bedwars.arena.enums.ArenaEnums;
import com.uzm.hylex.bedwars.arena.generators.Generator;
import com.uzm.hylex.bedwars.arena.management.ArenaBlocks;
import com.uzm.hylex.bedwars.arena.management.ArenaConfiguration;
import com.uzm.hylex.bedwars.arena.player.ArenaPlayer;
import com.uzm.hylex.bedwars.arena.team.Team;
import com.uzm.hylex.bedwars.arena.team.Teams;
import com.uzm.hylex.bedwars.proxy.ServerItem;
import com.uzm.hylex.bedwars.utils.PlayerUtils;
import com.uzm.hylex.core.api.HylexPlayer;
import com.uzm.hylex.core.api.interfaces.Enums;
import com.uzm.hylex.core.api.interfaces.IArena;
import com.uzm.hylex.core.api.interfaces.IArenaPlayer;
import com.uzm.hylex.core.controllers.TagController;
import com.uzm.hylex.core.java.util.ConfigurationCreator;
import com.uzm.hylex.core.nms.NMS;
import com.uzm.hylex.core.spigot.features.TabColor;
import com.uzm.hylex.core.spigot.features.Titles;
import com.uzm.hylex.core.spigot.location.LocationSerializer;
import com.uzm.hylex.core.utils.BukkitUtils;
import com.uzm.hylex.core.utils.CubeId;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.stream.Collectors;

import static com.uzm.hylex.bedwars.arena.player.ArenaPlayer.CurrentState.SPECTATING;

public class Arena implements IArena {

  private String name;
  private String worldName;

  private ArenaConfiguration configuration;
  private List<IArenaPlayer> arenaPlayers = new ArrayList<>();

  private Enums.ArenaState state;
  private ArenaEnums.Events eventState;

  private Map<Teams, Team> teams = new LinkedHashMap<>();
  private ConfigurationCreator folder;

  private Location waitingLocation;
  private Location spectatorLocation;

  private ArenaTask mainTask;
  private ArenaBlocks blocks;
  private CubeId borders;

  private List<CubeId> cantConstruct = new ArrayList<>();
  private List<Generator> generators = new ArrayList<>();

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
    } else {
      this.name = name;
      this.configuration = new ArenaConfiguration(4, 3, 4, 1);

      for (int x = 0; x < getConfiguration().getIslands(); x++) {
        teams.put(Teams.values()[x], new Team(Teams.values()[x]));
      }
    }
  }

  @Override
  public void join(HylexPlayer hp) {
    Player player = hp.getPlayer();

    ArenaPlayer ap = new ArenaPlayer(player, this);
    hp.setArenaPlayer(ap);

    this.arenaPlayers.add(ap);

    TagController tag = TagController.get(player);
    tag.setPrefix(hp.getGroup().getColor());
    tag.update();

    if (this.state == Enums.ArenaState.IN_GAME || this.state == Enums.ArenaState.END) {
      ap.setCurrentState(SPECTATING);
      ap.update();

      player.teleport(this.getSpectatorLocation());
      new TabColor(player).setHeader("\n §b§lHYLEX \n    §7Seja bem-vindo §E" + player.getName() + "§7." + "\n")
        .setBottom("\n §7Seu grupo é: " + hp.getGroup().getName() + "\n§7Você está em: §fBedWars - " + getArenaName() + "\n\n§b§nhylex.net§r \n ").send();
      return;
    }

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

    new TabColor(player).setHeader("\n §b§lHYLEX \n    §7Seja bem-vindo §E" + player.getName() + "§7." + "\n")
      .setBottom("\n §7Seu grupo é: " + hp.getGroup().getName() + "\n§7Você está em: §fBedWars - " + getArenaName() + "\n\n§b§nhylex.net§r \n ").send();
    if (this.state != Enums.ArenaState.IN_GAME && this.state != Enums.ArenaState.END) {
      this.arenaPlayers.stream().map(a -> ((ArenaPlayer) a).getPlayer())
        .forEach(players -> players.sendMessage(player.getDisplayName() + " §eentrou! §a(" + this.arenaPlayers.size() + "/" + this.getConfiguration().getMaxPlayers() + ")"));
    }
  }

  @Override
  public void leave(HylexPlayer hylexPlayer) {
    Player player = hylexPlayer.getPlayer();

    ArenaPlayer ap = (ArenaPlayer) hylexPlayer.getArenaPlayer();

    this.arenaPlayers.remove(ap);

    if (ap.getCurrentState().isInGame()) {
      List<HylexPlayer> hitters = hylexPlayer.getLastHitters();
      this.kill(hylexPlayer, hitters.size() > 0 ? hitters.get(0) : null, Team.Sitation.ELIMINATED);
    }

    if (this.state != Enums.ArenaState.IN_GAME && this.state != Enums.ArenaState.END) {
      this.arenaPlayers.stream().map(a -> ((ArenaPlayer) a).getPlayer())
        .forEach(players -> players.sendMessage(player.getDisplayName() + " §csaiu! §a(" + this.arenaPlayers.size() + "/" + this.getConfiguration().getMaxPlayers() + ")"));
    }
  }

  public void kill(HylexPlayer hp, HylexPlayer hpk) {
    this.kill(hp, hpk, ((ArenaPlayer) hp.getArenaPlayer()).getTeam().getSitation());
  }

  public void kill(HylexPlayer hp, HylexPlayer hpk, Team.Sitation sitation) {
    Player player = hp.getPlayer();
    ArenaPlayer ap = (ArenaPlayer) hp.getArenaPlayer();

    if (this.state == Enums.ArenaState.END) {
      return;
    }

    if (ap.getCurrentState() != ArenaPlayer.CurrentState.IN_GAME) {
      return;
    }

    Team team = ap.getTeam();
    Player killer = hpk != null ? hpk.getPlayer() : null;

    ArenaPlayer apk = hpk != null ? (ArenaPlayer) hpk.getArenaPlayer() : null;
    if (player.equals(killer) || apk == null || !this.equals(apk.getArena())) {
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

    this.arenaPlayers.stream().map(a -> ((ArenaPlayer) a).getPlayer())
      .forEach(players -> players.sendMessage(message + (sitation == Team.Sitation.BROKEN_BED ? " §b§lKILL FINAL!" : "")));
    if (sitation == Team.Sitation.STANDING) {
      hp.getBedWarsStatistics().addLong("deaths", "global");
      hp.getBedWarsStatistics().addLong("deaths", getConfiguration().getMode().toLowerCase());
      if (hpk != null) {
        hpk.getBedWarsStatistics().addLong("kills", "global");
        hpk.getBedWarsStatistics().addLong("kills", getConfiguration().getMode().toLowerCase());
      }
      Bukkit.getScheduler().scheduleSyncDelayedTask(Core.getInstance(), () -> {
        player.setFireTicks(0);
        player.getActivePotionEffects().forEach(pe -> player.removePotionEffect(pe.getType()));

        player.teleport(getWaitingLocation());
        getArenaPlayers().stream().map(a -> ((ArenaPlayer) a).getPlayer()).forEach(players -> players.hidePlayer(player));

        ap.setCurrentState(ArenaPlayer.CurrentState.RESPAWNING);
        ap.update();
      }, 3);
      return;
    }

    hp.getBedWarsStatistics().addLong("finalDeaths", "global");
    hp.getBedWarsStatistics().addLong("finalDeaths", getConfiguration().getMode().toLowerCase());
    if (hpk != null) {
      hpk.getBedWarsStatistics().addLong("finalKills", "global");
      hpk.getBedWarsStatistics().addLong("finalKills", getConfiguration().getMode().toLowerCase());
      hpk.getBedWarsStatistics().addLong("coins", "global", 25);
    }
    team.getAlive().remove(ap);
    if (team.getAlive().isEmpty()) {
      team.setSitation(Team.Sitation.ELIMINATED);
      getArenaPlayers().stream().map(a -> ((ArenaPlayer) a).getPlayer()).forEach(
        players -> players.sendMessage(" \n§f§lTIME ELIMINADO > §cO " + team.getTeamType().getTagColor() + "Time " + team.getTeamType().getName() + " §cfoi eliminado!\n "));
    }

    Bukkit.getScheduler().scheduleSyncDelayedTask(Core.getInstance(), () -> {
      if (player.isOnline()) {
        player.setFireTicks(0);
        player.getActivePotionEffects().forEach(pe -> player.removePotionEffect(pe.getType()));

        ap.setCurrentState(ArenaPlayer.CurrentState.DEAD);
        ap.update();
        this.check();
      }
    }, 3);
  }

  public void destroyBed(Team team, HylexPlayer hp) {
    team.breakBed();
    if (hp != null) {
      ArenaPlayer breaker = (ArenaPlayer) hp.getArenaPlayer();
      hp.getBedWarsStatistics().addLong("bedsBroken", "global");
      hp.getBedWarsStatistics().addLong("bedsBroken", getConfiguration().getMode().toLowerCase());

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

  public void start() {
    this.setState(Enums.ArenaState.IN_GAME);
    this.setEventState(ArenaEnums.Events.DIAMOND_II);

    this.mainTask.reset();
    getGenerators().forEach(Generator::enable);
    getArenaPlayers().forEach(a -> {
      ArenaPlayer ap = (ArenaPlayer) a;
      Player player = ap.getPlayer();

      HylexPlayer hp = HylexPlayer.getByPlayer(player);
      hp.getBedWarsStatistics().addLong("games", "global");
      hp.getBedWarsStatistics().addLong("games", getConfiguration().getMode().toLowerCase());
      if (ap.getCurrentState() != SPECTATING) {
        if (ap.getTeam() == null) {
          Team find = findTeam();
          find.addMember(ap, false);
          ap.setTeam(find);
        }

        player.sendMessage("§a§m-------------------------------------------");
        player.sendMessage("                  §e§n§lBed Wars");
        player.sendMessage("");
        player.sendMessage(" §7Proteja sua cama e destrua as camas inimigas.");
        player.sendMessage(" §7Atualize você e sua equipe coletando");
        player.sendMessage(" §7Ferro, Ouro, Esmeralda e Diamante de geradores");
        player.sendMessage(" §7para acessar atualizações poderosas.");
        player.sendMessage("");
        player.sendMessage("§a§m-------------------------------------------");

        player.sendMessage("§aPartida iniciada, boa sorte a todos os times.");

        ap.setCurrentState(ArenaPlayer.CurrentState.IN_GAME);
        ap.update();

        TagController tag = TagController.get(ap.getPlayer());
        tag.setOrder(ap.getTeam().getTeamType().getOrder());
        tag.setPrefix(ap.getTeam().getTeamType().getScoreboardName() + " ");
        tag.setSuffix("");
        tag.update();
        player.setDisplayName(ap.getTeam().getTeamType().getTagColor() + player.getName());

        player.getEnderChest().clear();
        player.teleport(ap.getTeam().getSpawnLocation());
        player.playSound(player.getLocation(), Sound.LEVEL_UP, 1.5F, 1.5F);
      } else {
        player.teleport(spectatorLocation);
      }
    });

    for (Team teams : listTeams()) {
      teams.enableHolograms();
      if (teams.getMembers().size() == 0) {
        teams.setSitation(Team.Sitation.ELIMINATED);
        BukkitUtils.getBedNeighbor(teams.getBedLocation().getBlock()).breakNaturally(null);
        teams.getBedLocation().getBlock().breakNaturally(null);
        continue;
      }

      teams.setSitation(Team.Sitation.STANDING);
      teams.addAlives();
    }

    this.check();
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
    if (team == null) {
      getArenaPlayers().stream().map(a -> (ArenaPlayer) a).forEach(ap -> {
        ap.setCurrentState(ArenaPlayer.CurrentState.DEAD);
        ap.update();
        ap.getPlayer().sendMessage(" \n§7Não houve ganhadores, finalizando partida!\n ");
      });
    } else {
      team.getMembers().forEach(ap -> {
        Player player = ap.getPlayer();

        HylexPlayer hp = HylexPlayer.getByPlayer(player);
        if (hp != null) {
          hp.getBedWarsStatistics().addLong("wins", "global");
          hp.getBedWarsStatistics().addLong("wins", getConfiguration().getMode().toLowerCase());
          hp.getBedWarsStatistics().addLong("coins", "global", 100);
        }

        ap.setCurrentState(ArenaPlayer.CurrentState.DEAD);
        ap.update();

        NMS.sendTitle(player, Titles.TitleType.BOTH, "", "§6§lVITÓRIA", 10, 60, 10);
      });
      getArenaPlayers().stream().map(a -> ((ArenaPlayer) a).getPlayer())
        .forEach(players -> players.sendMessage(" \n§7O " + team.getTeamType().getTagColor() + "Time " + team.getTeamType().getName() + " §7foi vitorioso!\n "));
    }

    new BukkitRunnable() {
      private int time = 10;

      @Override
      public void run() {
        if (time <= 0) {
          getArenaPlayers().stream().map(a -> ((ArenaPlayer) a).getPlayer()).forEach(player -> ServerItem.getServerItem("lobby").connect(HylexPlayer.getByPlayer(player)));
          Bukkit.getScheduler().scheduleSyncDelayedTask(Core.getInstance(), () -> {
            blocks.clearArena();
          }, 40L);
          cancel();
          return;
        }

        if (team != null) {
          for (IArenaPlayer a : getArenaPlayers()) {
            ArenaPlayer ap = (ArenaPlayer) a;
            if (ap.getTeam().equals(team)) {
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
    return arenaPlayers;
  }

  @Override
  public List<IArenaPlayer> getPlayingPlayers() {
    return arenaPlayers.stream().map(a -> (ArenaPlayer) a)
      .filter(result -> result.getCurrentState() == ArenaPlayer.CurrentState.IN_GAME || result.getCurrentState() == ArenaPlayer.CurrentState.RESPAWNING)
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

  public Team findTeam() {
    return listTeams().stream().filter(result -> result.getMembers().size() < getConfiguration().getTeamsSize()).findFirst().orElse(null);
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
    return teams * getConfiguration().getTeamsSize() == getConfiguration()
      .getMaxPlayers() && getSpectatorLocation() != null && getWaitingLocation() != null && getBorders() != null;
  }
}
