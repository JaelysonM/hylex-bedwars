package com.uzm.hylex.bedwars.commands;

import com.google.common.collect.Lists;
import com.uzm.hylex.bedwars.arena.player.ArenaPlayer;
import com.uzm.hylex.core.api.HylexPlayer;
import com.uzm.hylex.core.api.interfaces.Enums;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class StartCommand implements CommandExecutor {

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

    if (!(sender instanceof Player)) {
      sender.sendMessage("§fHey brother, stop do it! You cannot execute commands.");
      return true;
    }

    if (!sender.hasPermission("hylex.staff")) {
      sender.sendMessage("§b[Hylex] §cSem §c§npermissão §cpara executar esse comando.");
      return true;
    }

    Player player = (Player) sender;
    HylexPlayer hp = HylexPlayer.getByPlayer(player);
    if (hp != null) {
      ArenaPlayer ap = (ArenaPlayer) hp.getArenaPlayer();
      if (ap != null && ap.getArena() != null) {
        if (ap.getArena().getState() == Enums.ArenaState.IN_WAITING || ap.getArena().getState() == Enums.ArenaState.PREPARE || ap.getArena()
          .getState() == Enums.ArenaState.STARTING) {
          ap.getArena().start();
          ap.getArena().getMainTask().setTime(60 * 6);
          player.sendMessage("§aVocê forçou o início da arena.");
          return true;
        }

        player.sendMessage("§cEsta partida já se encontra em progresso.");
      }
    }
    return true;
  }

  public static ArrayList<String> getInvoke() {
    return Lists.newArrayList("start");
  }
}
