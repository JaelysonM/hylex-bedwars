package com.uzm.hylex.bedwars.arena.management;

import com.uzm.hylex.bedwars.Core;
import com.uzm.hylex.bedwars.arena.Arena;
import com.uzm.hylex.bedwars.arena.generators.Generator;
import com.uzm.hylex.bedwars.arena.team.Team;
import com.uzm.hylex.bedwars.controllers.ArenaController;
import com.uzm.hylex.core.api.interfaces.Enums;
import com.uzm.hylex.services.lan.WebSocket;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.uzm.hylex.core.api.interfaces.Enums.ArenaState.IDLE;

public class ArenaBlocks {

  private Arena arena;
  private Set<Block> placed = new HashSet<>();

  public ArenaBlocks(Arena arena) {
    this.arena = arena;
  }

  public void addBlock(Block block) {
    this.placed.add(block);
  }

  public void removeBlock(Block block) {
    this.placed.remove(block);
  }

  public boolean isPlacedBlock(Block block) {
    return this.placed.contains(block);
  }

  public void clearArena() {
    this.arena.listTeams().forEach(Team::resetTeam);
    this.arena.getGenerators().forEach(Generator::disable);
    this.arena.getMainTask().cancel();
    this.arena.setState(IDLE);
    this.placed.clear();

   long total_ram = Runtime.getRuntime().totalMemory() / 1024 / 1024;
    long used_ram = total_ram - Runtime.getRuntime().freeMemory() / 1024 / 1024;

    if ((used_ram/total_ram)*100 >= 90) {
      if (ArenaController.listArenas().stream().filter(result -> (result.getState() == Enums.ArenaState.IDLE)).count() >= ArenaController.getArenas().size()) {
        JSONObject json = new JSONObject();
        json.put("clientName", "core-" + com.uzm.hylex.core.Core.SOCKET_NAME);
        WebSocket.get("core-" + com.uzm.hylex.core.Core.SOCKET_NAME).getSocket().emit("send-restart-require", json);
        Bukkit.getConsoleSender().sendMessage("Module: Core] a permission to restart");
      }
      return;
    }
    queueReset(this.arena);
  }

  public Arena getArena() {
    return this.arena;
  }

  private static BukkitTask task;
  private static final List<Arena> QUEUE = new ArrayList<>();

  public static void queueReset(Arena arena) {
    if (QUEUE.contains(arena)) {
      return;
    }

    QUEUE.add(arena);
    if (task == null) {
      task = new BukkitRunnable() {
        Arena reseting;

        @Override
        public void run() {
          if (reseting != null) {
            ArenaController.arenas.remove(this.reseting.getArenaName());
            ArenaController.loadArena(this.reseting.getArenaName());
            this.reseting.destroy();
            this.reseting = null;
            return;
          }

          if (!QUEUE.isEmpty()) {
            reseting = QUEUE.get(0);
            QUEUE.remove(0);
          } else {
            cancel();
            task = null;
          }
        }
      }.runTaskTimer(Core.getInstance(), 60, 60);
    }
  }
}
