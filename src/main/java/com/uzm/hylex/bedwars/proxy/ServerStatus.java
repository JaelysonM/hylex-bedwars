package com.uzm.hylex.bedwars.proxy;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.uzm.hylex.bedwars.Core;
import com.uzm.hylex.core.api.HylexPlayer;

public class ServerStatus {
  private String name;
  private int players;

  private boolean up;

  public ServerStatus(String serverName, int players) {
    this.name = serverName;
    this.players = players;
  }

  public ServerStatus(String serverName, boolean up) {
    this.name = serverName;
    this.up = up;
  }


  public String getServerName() {
    return name;
  }

  public int getPlayers() {
    return players;
  }

  public boolean isOnline() {
    return up;
  }

  public void setUpStatus(boolean up) {
    this.up = up;
  }

  public void setPlayers(int players) {
    this.players = players;
  }

  public void connect(HylexPlayer player) {
    String server = getServerName();
    if (server != null) {
      if (player != null) {
        player.getPlayer().closeInventory();
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(server);
        player.getPlayer().sendPluginMessage(Core.getInstance(), "BungeeCord", out.toByteArray());
      }
    }
  }
}
