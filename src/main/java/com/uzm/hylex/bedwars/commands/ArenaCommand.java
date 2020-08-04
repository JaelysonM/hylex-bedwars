package com.uzm.hylex.bedwars.commands;

import com.google.common.collect.Lists;
import com.uzm.hylex.bedwars.arena.Arena;
import com.uzm.hylex.bedwars.arena.creator.inventory.ArenasMenu;
import com.uzm.hylex.bedwars.arena.creator.inventory.MainPainel;
import com.uzm.hylex.bedwars.arena.creator.inventory.WorldsMenu;
import com.uzm.hylex.bedwars.arena.generators.Generator;
import com.uzm.hylex.bedwars.arena.player.ArenaPlayer;
import com.uzm.hylex.bedwars.arena.team.Team;
import com.uzm.hylex.bedwars.controllers.ArenaController;
import com.uzm.hylex.bedwars.proxy.ServerItem;
import com.uzm.hylex.core.api.HylexPlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

import static com.uzm.hylex.core.api.interfaces.Enums.ArenaState.IDLE;

public class ArenaCommand implements CommandExecutor {

  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    if (!(sender instanceof Player)) {
      sender.sendMessage("§fHey brother, stop do it! You cannot execute commands.");
      return true;
    }

    HylexPlayer hylex = HylexPlayer.getByPlayer((Player) sender);
    Player player = (Player) sender;
    if (!player.getPlayer().hasPermission("hylex.bedwars.setup")) {
      player.getPlayer().sendMessage("§b[Hylex] §cSem §c§npermissão §cpara executar esse comando.");
      return true;
    }

    if (label.equalsIgnoreCase("arena")) {

      switch (args.length) {
        case 0:
          help(player, label);
          break;
        case 1:
          switch (args[0].toLowerCase()) {
            case "list":
              new ArenasMenu(player);
              break;
            case "configure":
              if (hylex.getAbstractArena() != null) {
                if (((Arena) hylex.getAbstractArena()).getWorldName() != null) {
                  new MainPainel(player, (Arena) hylex.getAbstractArena());
                } else {
                  new WorldsMenu(player);
                }

              } else {
                player.sendMessage("§6[ArenaCreator] §7Você já não está §cconfigurando §7uma arena §7digite §f/arena create <mini-name> §7para criar um arena");
              }
              break;
            default:
              help(player, label);
              break;

          }
          break;
        case 2:
          switch (args[0].toLowerCase()) {
            case "create":
              if (hylex.getAbstractArena() == null) {
                hylex.setAbstractArena(new Arena(args[1], true));
                new WorldsMenu(player);
              } else {
                player.sendMessage(
                  "§6[ArenaCreator] §7Você já está §ccriando §7uma arena digite §f/arena configure §7para voltar a configuraá-la ou §7clique no em '§cDeletar arena' §7no painel principal. §8(" + hylex
                    .getAbstractArena() + "§8)");
              }
              break;
            case "edit":
              if (ArenaController.getArena(args[1]) != null) {
                hylex.setAbstractArena(ArenaController.getArena(args[1]));
                new MainPainel(player, ArenaController.getArena(args[1]));
              } else {
                player.sendMessage("§6[ArenaCreator] §7Não existe uma arena-mini como o nome §c" + args[1]);
              }
              break;
            case "inative":
              if (ArenaController.getArena(args[1]) != null) {
                Arena arena = ArenaController.getArena(args[1]);
                player.sendMessage("§6[ArenaCreator] §7A arena-mini com o nome §c" + args[1] + " §7agora está inativa.");


                arena.getArenaPlayers().stream().map(a -> ((ArenaPlayer) a).getPlayer()).forEach(players -> {
                  if (players != null) {
                    if (HylexPlayer.getByPlayer(players) !=null)
                    ServerItem.getServerItem("lobby").connect(HylexPlayer.getByPlayer(players));
                    else
                      players.kickPlayer("§cPartida encerrada!");
                  }
                });

                arena.listTeams().forEach(Team::resetTeam);
                arena.getGenerators().forEach(Generator::disable);
                arena.getMainTask().cancel();
                arena.setState(IDLE);
                System.gc();


              }else {
                player.sendMessage("§6[ArenaCreator] §7Não existe uma arena-mini como o nome §c" + args[1]);

              }
              break;
            default:
              help(player, label);
              break;
          }
          break;
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
    player.sendMessage("  §e- §f/" + label + " delete <mini> §7Crie uma arena mini.");
    player.sendMessage("  §e- §f/" + label + " edit <mini> §7Abra o painel para edição da arena.");
    player.sendMessage("  §e- §f/" + label + " inative <mini> §7Deixe uma arena inativa.");
    player.sendMessage("  §e- §f/" + label + " configure §7Abra o painel da arena recentemente criada por você.");
    player.sendMessage("  §e- §f/" + label + " list §7Liste todas as arenas do mega.");
    player.sendMessage("");
  }
}
