package com.uzm.hylex.bedwars.arena.management;

import com.uzm.hylex.bedwars.arena.Arena;
import com.uzm.hylex.bedwars.arena.generators.Generator;
import com.uzm.hylex.bedwars.arena.team.Team;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;

import java.util.HashSet;
import java.util.Set;

import static com.uzm.hylex.bedwars.arena.enums.ArenaEnums.ArenaState.END;

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
    this.arena.setState(END);

    this.placed.clear();
    this.arena.getGenerators().forEach(Generator::disable);
    this.arena.getTeams().values().forEach(Team::resetTeam);
    Bukkit.unloadWorld(this.arena.getWorldName(), false);
  }

  public Arena getArena() {
    return this.arena;
  }
}
