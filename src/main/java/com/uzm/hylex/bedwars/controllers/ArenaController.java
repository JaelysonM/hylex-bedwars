package com.uzm.hylex.bedwars.controllers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.uzm.hylex.bedwars.Core;
import com.uzm.hylex.bedwars.arena.Arena;
import com.uzm.hylex.bedwars.arena.enums.ArenaEnums;
import com.uzm.hylex.bedwars.arena.team.Team;
import com.uzm.hylex.core.api.interfaces.Enums;
import com.uzm.hylex.core.java.util.configuration.ConfigurationCreator;
import com.uzm.hylex.core.java.util.file.FileUtils;
import com.uzm.hylex.core.spigot.location.LocationSerializer;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.uzm.hylex.bedwars.Core.getInstance;
import static com.uzm.hylex.core.spigot.utils.VoidChunkGenerator.VOID_CHUNK_GENERATOR;

public class ArenaController {

  public static Collection<Arena> listArenas() {
    return arenas.values();
  }

  public static HashMap<String, Arena> getArenas() {
    return arenas;
  }

  public static void loadArena(String name) {
    System.gc();
    ConfigurationCreator creator = new ConfigurationCreator(Core.getInstance(), name, "arenas/");
    World world;
    File file = new File(Core.getInstance().getDataFolder(), "backup/" + creator.get().getString("configuration.worldName"));

    if (!file.getParentFile().exists()) {
      file.getParentFile().mkdirs();
    };

    if ((world =Bukkit.getWorld(name)) != null) {
      Bukkit.unloadWorld(world, false);
      FileUtils.deleteFile(new File(name));
    }

    FileUtils.copyFiles(file, new File(name));

    WorldCreator wc = WorldCreator.name(name);
    wc.generator(VOID_CHUNK_GENERATOR);
    wc.generateStructures(false);
    world = wc.createWorld();
    world.setTime(0L);
    world.setStorm(false);
    world.setThundering(false);
    world.setAutoSave(false);
    world.setAnimalSpawnLimit(0);
    world.setWaterAnimalSpawnLimit(0);
    world.setKeepSpawnInMemory(false);
    world.setGameRuleValue("doMobSpawning", "false");
    world.setGameRuleValue("doDaylightCycle", "false");
    world.setGameRuleValue("mobGriefing", "false");
    world.getEntities().stream().filter(entity -> !(entity instanceof Player)).forEach(Entity::remove);

    arenas.computeIfAbsent(name, df -> new Arena(name, false));
  }

  public static void loadArenas() {
    if (new File(Core.getInstance().getDataFolder(), "arenas/").exists()) {
      for (File file : Objects.requireNonNull(new File(getInstance().getDataFolder(), "arenas/").listFiles())) {
        if (file.isFile() && file.getName().endsWith((".yml"))) {
          loadArena(file.getName().replace(".yml", ""));
        }
      }
    }
  }


  public static void saveArena(Arena interfaceArena) {
    ConfigurationCreator creator = new ConfigurationCreator(getInstance(), interfaceArena.getArenaName(), "arenas/");
    creator.get().set("configuration.arena-name", interfaceArena.getArenaName());
    creator.get().set("configuration.worldName", interfaceArena.getWorldName());
    creator.get().set("configuration.max-players", interfaceArena.getConfiguration().getMaxPlayers());
    creator.get().set("configuration.min-start-players", interfaceArena.getConfiguration().getMinPlayers());
    creator.get().set("configuration.islands", interfaceArena.getConfiguration().getIslands());
    creator.get().set("configuration.teams-size", interfaceArena.getConfiguration().getTeamsSize());

    creator.get()
      .set("locations.map.waiting", new LocationSerializer(interfaceArena.getWaitingLocation()).serialize().replace(interfaceArena.getWorldName(), interfaceArena.getArenaName()));
    creator.get().set("locations.map.spectator",
      new LocationSerializer(interfaceArena.getSpectatorLocation()).serialize().replace(interfaceArena.getWorldName(), interfaceArena.getArenaName()));
    for (Team teams : interfaceArena.listEachTeams()) {
      ConfigurationSection section = creator.get().createSection("locations.teams." + teams.getTeamType().toString().toUpperCase());
      section.set("spawn", new LocationSerializer(teams.getSpawnLocation()).serialize().replace(interfaceArena.getWorldName(), interfaceArena.getArenaName()));
      section.set("shop", new LocationSerializer(teams.getShopLocation()).serialize().replace(interfaceArena.getWorldName(), interfaceArena.getArenaName()));
      section.set("upgrade", new LocationSerializer(teams.getUpgradeLocation()).serialize().replace(interfaceArena.getWorldName(), interfaceArena.getArenaName()));
      section.set("bedlocation", new LocationSerializer(teams.getBedLocation()).serialize().replace(interfaceArena.getWorldName(), interfaceArena.getArenaName()));
      section.set("borders", teams.getBorder().toString().replace(interfaceArena.getWorldName(), interfaceArena.getArenaName()));
      section.set("team-generators",
        teams.getTeamGenerators().stream().map(result -> new LocationSerializer(result).serialize().replace(interfaceArena.getWorldName(), interfaceArena.getArenaName()))
          .collect(Collectors.toList()));
    }
    creator.get().set("locations.generators", interfaceArena.getGenerators().stream().map(
      result -> result.getType().toString().toUpperCase() + " | " + new LocationSerializer(result.getLocation()).serialize()
        .replace(interfaceArena.getWorldName(), interfaceArena.getArenaName())).collect(Collectors.toList()));

    creator.get().set("security.border", interfaceArena.getBorders().toString().replace(interfaceArena.getWorldName(), interfaceArena.getArenaName()));
    creator.get().set("security.waitingLocationBorder", interfaceArena.getWaitingLocationBorder().toString().replace(interfaceArena.getWorldName(), interfaceArena.getArenaName()));

    creator.get().set("security.protected",
      interfaceArena.getCantConstruct().stream().map(result -> result.toString().replace(interfaceArena.getWorldName(), interfaceArena.getArenaName()))
        .collect(Collectors.toList()));

    arenas.putIfAbsent(interfaceArena.getArenaName(), interfaceArena);

    Arena arena =arenas.getOrDefault(interfaceArena.getArenaName(), null);

    arena.setState(Enums.ArenaState.IN_WAITING);
    arena.setEventState(ArenaEnums.Events.IDLE);

    creator.save();
    FileUtils.copyFiles(new File(interfaceArena.getWorldName()), new File(Core.getInstance().getDataFolder(), "backup/" + interfaceArena.getWorldName()), "playerdata", "stats",
      "uid.dat");
  }


  public static void saveExistentArena(Arena interfaceArena) {
    ConfigurationCreator creator = interfaceArena.getFolder();
    creator.get().set("configuration.arena-name", interfaceArena.getArenaName());
    creator.get().set("configuration.worldName", interfaceArena.getWorldName());
    creator.get().set("configuration.max-players", interfaceArena.getConfiguration().getMaxPlayers());
    creator.get().set("configuration.min-start-players", interfaceArena.getConfiguration().getMinPlayers());
    creator.get().set("configuration.islands", interfaceArena.getConfiguration().getIslands());
    creator.get().set("configuration.teams-size", interfaceArena.getConfiguration().getTeamsSize());

    creator.get()
      .set("locations.map.waiting", new LocationSerializer(interfaceArena.getWaitingLocation()).serialize().replace(interfaceArena.getWorldName(), interfaceArena.getArenaName()));
    creator.get().set("locations.map.spectator",
      new LocationSerializer(interfaceArena.getSpectatorLocation()).serialize().replace(interfaceArena.getWorldName(), interfaceArena.getArenaName()));
    for (Team teams : interfaceArena.listEachTeams()) {
      ConfigurationSection section = creator.get().createSection("locations.teams." + teams.getTeamType().toString().toUpperCase());
      section.set("spawn", new LocationSerializer(teams.getSpawnLocation()).serialize().replace(interfaceArena.getWorldName(), interfaceArena.getArenaName()));
      section.set("shop", new LocationSerializer(teams.getShopLocation()).serialize().replace(interfaceArena.getWorldName(), interfaceArena.getArenaName()));
      section.set("upgrade", new LocationSerializer(teams.getUpgradeLocation()).serialize().replace(interfaceArena.getWorldName(), interfaceArena.getArenaName()));
      section.set("bedlocation", new LocationSerializer(teams.getBedLocation()).serialize().replace(interfaceArena.getWorldName(), interfaceArena.getArenaName()));
      section.set("borders", teams.getBorder().toString());
      section.set("team-generators",
        teams.getTeamGenerators().stream().map(result -> new LocationSerializer(result).serialize().replace(interfaceArena.getWorldName(), interfaceArena.getArenaName()))
          .collect(Collectors.toList()));
    }
    creator.get().set("locations.generators", interfaceArena.getGenerators().stream().map(
      result -> result.getType().toString().toUpperCase() + " | " + new LocationSerializer(result.getLocation()).serialize()
        .replace(interfaceArena.getWorldName(), interfaceArena.getArenaName())).collect(Collectors.toList()));

    creator.get().set("security.border", interfaceArena.getBorders().toString().replace(interfaceArena.getWorldName(), interfaceArena.getArenaName()));
    creator.get().set("security.waitingLocationBorder", interfaceArena.getWaitingLocationBorder().toString().replace(interfaceArena.getWorldName(), interfaceArena.getArenaName()));

    creator.get().set("security.protected",
      interfaceArena.getCantConstruct().stream().map(result -> result.toString().replace(interfaceArena.getWorldName(), interfaceArena.getArenaName()))
        .collect(Collectors.toList()));
    creator.save();
  }

  public static boolean hasArena(String worldName) {
    return arenas.values().stream().anyMatch(arena -> arena.getWorldName().equalsIgnoreCase(worldName));
  }

  public static Arena getArena(String name) {
    return arenas.getOrDefault(name, null);
  }

  public static HashMap<String, Arena> arenas = Maps.newHashMap();
}
