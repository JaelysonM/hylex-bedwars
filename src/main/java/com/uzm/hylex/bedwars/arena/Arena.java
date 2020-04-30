package com.uzm.hylex.bedwars.arena;

import com.uzm.hylex.bedwars.arena.enums.ArenaEnums;
import com.uzm.hylex.bedwars.arena.generators.Generator;
import com.uzm.hylex.bedwars.arena.management.ArenaBlocks;
import com.uzm.hylex.bedwars.arena.management.ArenaConfiguration;
import com.uzm.hylex.bedwars.arena.player.ArenaPlayer;
import com.uzm.hylex.bedwars.arena.team.Team;
import com.uzm.hylex.bedwars.arena.team.Teams;
import com.uzm.hylex.bedwars.utils.CubeId;
import com.uzm.hylex.core.java.util.ConfigurationCreator;
import com.uzm.hylex.core.spigot.location.LocationSerializer;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static com.uzm.hylex.bedwars.Core.getInstance;
import static com.uzm.hylex.bedwars.arena.player.ArenaPlayer.CurrentState.SPECTATING;

public class Arena {

  private String name;
  private String worldName;

  private ArenaConfiguration configuration;
  private List<ArenaPlayer> arenaPlayers = new ArrayList<>();

  private ArenaEnums.ArenaState state;
  private ArenaEnums.Events eventState;

  private HashMap<Teams, Team> teams = new HashMap<>();
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
      this.folder = ConfigurationCreator.configs.put(getArenaName() + "-" + getInstance().getName(), new ConfigurationCreator(getInstance(), getArenaName(), "arenas/"));

      assert folder != null;
      this.configuration = new ArenaConfiguration(folder.get().getInt("configuration.max-players"), folder.get().getInt("configuration.min-start-players"),
        folder.get().getInt("configuration.islands"), folder.get().getInt("configuration.teams-size"));

      for (int x = 0; x < getConfiguration().getIslands(); x++) {
        Teams team = Teams.values()[x];
        teams.computeIfAbsent(team, defaultValue -> new Team(team));
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

      setEventState(ArenaEnums.Events.IDLE);
    } else {
      this.name = name;
      this.configuration = new ArenaConfiguration(4, 3, 4, 1);

      for (int x = 0; x < getConfiguration().getIslands(); x++) {
        Teams team = Teams.values()[x];
        teams.computeIfAbsent(team, defaultValue -> new Team(team));
      }
    }
  }

  public void start() {
    this.setState(ArenaEnums.ArenaState.IN_GAME);
    this.setEventState(ArenaEnums.Events.DIAMOND_II);

    this.configureTeams();
    getGenerators().forEach(Generator::enable);
    getArenaPlayers().forEach(ap -> {
      Player player = ap.getPlayer();
      if (ap.getCurrentState() != SPECTATING) {
        if (ap.getTeam() == null) {
          Team find = findTeam();
          find.addMember(ap, false);
          ap.setTeam(findTeam());
        }

        player.sendMessage("§aPartida iniciada, boa sorte a todos os times.");

        player.sendMessage("§a§m---------------------------------------------------");
        player.sendMessage("                  §e§n§lBed Wars");
        player.sendMessage("");
        player.sendMessage(" §7Proteja sua cama e destrua as camas inimigas.");
        player.sendMessage(" §7Atualize você e sua equipe coletando");
        player.sendMessage(" §7Ferro, Ouro, Esmeralda e Diamante de geradores");
        player.sendMessage(" §7para acessar atualizações poderosas.");
        player.sendMessage("§a§m---------------------------------------------------");

        ap.setCurrentState(ArenaPlayer.CurrentState.IN_GAME);
        ap.update();
        player.teleport(ap.getTeam().getSpawnLocation());
        player.playSound(player.getLocation(), Sound.LEVEL_UP, 1.5F, 1.5F);
      } else {
        player.teleport(spectatorLocation);
      }
    });
  }

  public void configureTeams() {
    for (Team teams : teams.values()) {
      if (teams.getMembers().size() == 0) {
        teams.setSitation(Team.Sitation.ELIMINATED);
        teams.getBedLocation().getBlock().breakNaturally(null);
        return;
      }

      teams.addAlives();
      teams.enableHolograms();
    }
  }

  public void fixPlayers(Player player) {

  }

  public ArenaConfiguration getConfiguration() {
    return configuration;
  }


  public List<ArenaPlayer> getArenaPlayers() {
    return arenaPlayers;
  }

  public List<ArenaPlayer> getPlayingPlayers() {
    return arenaPlayers.stream().filter(result -> result.getCurrentState() == ArenaPlayer.CurrentState.IN_GAME || result.getCurrentState() == ArenaPlayer.CurrentState.RESPAWNING)
      .collect(Collectors.toList());
  }


  public ArenaEnums.ArenaState getState() {
    return state;
  }

  public void setState(ArenaEnums.ArenaState state) {
    this.state = state;
  }

  public void setEventState(ArenaEnums.Events upgrade) {
    this.eventState = upgrade;
  }

  public ArenaEnums.Events getUpgradeState() {
    return eventState;
  }

  public Team findTeam() {
    return teams.values().stream().filter(result -> result.getMembers().size() < getConfiguration().getTeamsSize()).findFirst().orElse(null);
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

  public HashMap<Teams, Team> getTeams() {
    return teams;
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

  public void setCantConstruct(List<CubeId> cantConstruct) {
    this.cantConstruct = cantConstruct;
  }

  public List<CubeId> getCantConstruct() {
    return this.cantConstruct;
  }

  public void setBorders(CubeId borders) {
    this.borders = borders;
  }


  public boolean isFullyConfigured(int teams) {
    if (teams == getConfiguration().getMaxPlayers() && getSpectatorLocation() != null && getWaitingLocation() != null && getBorders() != null) {
      return true;
    }
    return false;
  }
}
