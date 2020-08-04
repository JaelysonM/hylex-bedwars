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
    return this.islands == 2 ?
      ((this.teamsSize == 20 ? "20v20" : this.teamsSize == 10 ? "10v10" : this.teamsSize == 5 ? "5v5" : teamsSize == 2 ? "2v2" : "1v1")) :
      (this.teamsSize == 1 ? "Solo" : this.teamsSize == 2 ? "Dupla" : this.teamsSize == 3 ? "Trio" : "Squad");
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

  public void setIslands(int islands) {
    this.islands = islands;
  }

  public void setMaxPlayers(int maxPlayers) {
    this.maxPlayers = maxPlayers;
  }

  public void setMinPlayers(int minPlayers) {
    this.minPlayers = minPlayers;
  }

  public void setTeamsSize(int teamsSize) {
    this.teamsSize = teamsSize;
  }
}
