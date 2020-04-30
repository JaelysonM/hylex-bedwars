package com.uzm.hylex.bedwars.controllers;

import com.google.common.collect.Maps;
import com.uzm.hylex.bedwars.Core;
import com.uzm.hylex.bedwars.arena.Arena;
import com.uzm.hylex.bedwars.arena.team.Team;
import com.uzm.hylex.bedwars.utils.CubeId;
import com.uzm.hylex.bedwars.utils.Utils;
import com.uzm.hylex.core.java.util.ConfigurationCreator;
import com.uzm.hylex.core.spigot.location.LocationSerializer;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashMap;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.uzm.hylex.bedwars.Core.getInstance;
import static com.uzm.hylex.bedwars.utils.VoidChunkGenerator.VOID_CHUNK_GENERATOR;

public class ArenaController {

    public static HashMap<String, Arena> getArenas() {
        return arenas;
    }

    public static void loadArena(String name) {
        World world;
        File file = new File(Core.getInstance().getDataFolder(), "backup/" + name);
        if ((world = Bukkit.getWorld(file.getName())) != null) {
            Bukkit.unloadWorld(world, false);
        }

        Utils.deleteFile(new File(file.getName()));
        Utils.copyFiles(file, new File(file.getName()));

        WorldCreator wc = WorldCreator.name(file.getName());
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

        creator.get().set("locations.map.waiting", new LocationSerializer(interfaceArena.getWaitingLocation()).serialize());
        creator.get().set("locations.map.spectator", new LocationSerializer(interfaceArena.getSpectatorLocation()).serialize());
        for (Team teams : interfaceArena.getTeams().values()) {
            ConfigurationSection section = creator.get().createSection("locations.teams." + teams.getTeamType().toString().toUpperCase());
            section.set("spawn", new LocationSerializer(teams.getSpawnLocation()).serialize());
            section.set("shop", new LocationSerializer(teams.getShopLocation()).serialize());
            section.set("upgrade", new LocationSerializer(teams.getUpgradeLocation()).serialize());
            section.set("bedlocation", new LocationSerializer(teams.getBedLocation()).serialize());
            section.set("borders", teams.getBorder().toString());
            section.set("team-generators", teams.getTeamGenerators().stream().map(result -> new LocationSerializer(result).serialize()).collect(Collectors.toList()));
        }
        creator.get().set("locations.generators",
                interfaceArena.getGenerators().stream().map(result -> result.getType().toString().toUpperCase() + " | " + new LocationSerializer(result.getLocation()).serialize()).collect(Collectors.toList()));

        creator.get().set("security.border", interfaceArena.getBorders().toString());
        creator.get().set("security.protected", interfaceArena.getCantConstruct().stream().map(CubeId::toString).collect(Collectors.toList()));

        arenas.putIfAbsent(interfaceArena.getArenaName(), interfaceArena);
        creator.save();
        Utils.copyFiles(new File(interfaceArena.getWorldName()), new File(Core.getInstance().getDataFolder(), "backup/" + interfaceArena.getWorldName()));
    }

    public static boolean hasArena(String worldName) {
        return arenas.values().stream().anyMatch(arena -> arena.getWorldName().equalsIgnoreCase(worldName));
    }


    public static Arena getArena(String name) {
        return arenas.getOrDefault(name, null);
    }

    public static HashMap<String, Arena> arenas = Maps.newHashMap();
}
