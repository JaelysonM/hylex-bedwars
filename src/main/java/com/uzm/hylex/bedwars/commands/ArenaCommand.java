package com.uzm.hylex.bedwars.commands;

import com.google.common.collect.Lists;
import com.uzm.hylex.bedwars.arena.Arena;
import com.uzm.hylex.bedwars.arena.creator.inventory.WorldsMenu;
import com.uzm.hylex.bedwars.controllers.HylexPlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class ArenaCommand implements CommandExecutor {

  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    if (!(sender instanceof Player)) {
      sender.sendMessage("§fHey brother, stop do it! You cannot execute commands.");
      return true;
    }

    HylexPlayer hylex = HylexPlayer.get((Player) sender);
    Player player = (Player) sender;
    if (!player.getPlayer().hasPermission("hylex.bedwars.setup")) {
      player.getPlayer().sendMessage("§b[Hylex] §cSem §c§npermissão §cpara executar esse comando.");
      return true;
    }
    if (label.equalsIgnoreCase("arena")) {
      if (args.length == 0) {
        help(player, label);
        return true;
      }

      if (args.length == 2) {
        if ("create".equals(args[0].toLowerCase())) {
          String mini = args[1];
          hylex.setAbstractArena(new Arena(mini, true));
          new WorldsMenu(player);
        } else {
          help(player, label);
        }
      }
    }

    return false;
  }

  public static ArrayList<String> getInvoke() {
    return Lists.newArrayList("arena");
  }

  public void help(Player player, String label) {
    player.sendMessage("");
    player.sendMessage("   §eAjuda do comando §f'" + label + "'");
    player.sendMessage("");
    player.sendMessage("  §e- §f/" + label + " create <mini> §7Crie uma arena mini.");
    player.sendMessage("  §e- §f/" + label + " list §7Liste todas as arenas do mega.");
    player.sendMessage("");
  }
}
