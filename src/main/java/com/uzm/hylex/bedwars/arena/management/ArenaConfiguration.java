package com.uzm.hylex.bedwars.arena.management;

public class ArenaConfiguration {

  private int maxPlayers;
  private int minPlayers;
  private int islands;
  private int teamsSize;

  public ArenaConfiguration(int maxPlayers, int minPlayers, int islands, int teamsSize) {
    this.maxPlayers = maxPlayers;
    this.minPlayers = minPlayers;
    this.islands = islands;
    this.teamsSize = teamsSize;
  }

  public String getMode() {
    return this.teamsSize == 1 ? "SOLO" : this.teamsSize == 2 ? "DUPLA" : this.teamsSize == 3 ? "TRIO" : "SQUAD";
  }

  public int getIslands() {
    return this.islands;
  }

  public int getTeamsSize() {
    return this.teamsSize;
  }

  public int getMaxPlayers() {
    return this.maxPlayers;
  }

  public int getMinPlayers() {
    return this.minPlayers;
  }
}
