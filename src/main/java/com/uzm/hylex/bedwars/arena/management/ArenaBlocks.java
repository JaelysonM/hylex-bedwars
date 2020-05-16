package com.uzm.hylex.bedwars.arena.management;

import com.uzm.hylex.bedwars.arena.Arena;
import com.uzm.hylex.bedwars.arena.generators.Generator;
import com.uzm.hylex.bedwars.arena.team.Team;
import com.uzm.hylex.bedwars.controllers.ArenaController;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;

import java.util.HashSet;
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
    this.arena.getMainTask().cancel();
    this.arena.setState(IDLE);
    this.placed.clear();
    this.arena.listTeams().forEach(Team::resetTeam);
    this.arena.getGenerators().forEach(Generator::disable);
    Bukkit.unloadWorld(this.arena.getArenaName(), false);
    if (ArenaController.listArenas().stream().filter(result -> result.getState() == IDLE).count() >= ArenaController.getArenas().size()) {
      Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "restart");
    }
  }

  public Arena getArena() {
    return this.arena;
  }
}
