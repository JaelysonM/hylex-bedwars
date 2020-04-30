package com.uzm.hylex.bedwars.commands;

import com.google.common.collect.Lists;
import com.uzm.hylex.bedwars.Core;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.logging.Level;

import static com.uzm.hylex.bedwars.utils.VoidChunkGenerator.VOID_CHUNK_GENERATOR;

public class LoadWorldCommand implements CommandExecutor {

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    if (!sender.hasPermission("hylex.bedwars.setup")) {
      sender.sendMessage("§b[Hylex] §cSem §c§npermissão §cpara executar esse comando.");
      return true;
    }

    if (args.length == 0) {
      sender.sendMessage("§cUtilize /loadworld [mundo]");
      return true;
    }

    if (Bukkit.getWorld(args[0]) != null) {
      sender.sendMessage("§cMundo já existente.");
      return true;
    }

    File map = new File(args[0]);
    if (!map.exists() || !map.isDirectory()) {
      sender.sendMessage("§cPasta do Mundo não encontrada.");
      return true;
    }

    try {
      sender.sendMessage("§aCarregando...");
      Bukkit.getScheduler().scheduleSyncDelayedTask(Core.getInstance(), () -> {
        WorldCreator wc = WorldCreator.name(map.getName());
        wc.generateStructures(false);
        wc.generator(VOID_CHUNK_GENERATOR);
        World world = wc.createWorld();
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
        sender.sendMessage("§aMundo carregado com sucesso.");
      });
    } catch (Exception ex) {
      Core.getInstance().getLogger().log(Level.WARNING, "Cannot load world \"" + args[0] + "\"", ex);
      sender.sendMessage("§cNão foi possível carregar o mundo.");
    }
    return true;
  }

  public static ArrayList<String> getInvoke() {
    return Lists.newArrayList("loadworld");
  }
}
