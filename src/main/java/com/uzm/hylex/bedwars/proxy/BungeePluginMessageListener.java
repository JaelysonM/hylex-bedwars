package com.uzm.hylex.bedwars.proxy;


import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import org.bukkit.entity.Player;

import java.net.Socket;

public class BungeePluginMessageListener implements org.bukkit.plugin.messaging.PluginMessageListener {

  @Override
  public void onPluginMessageReceived(String channel, Player receiver, byte[] msg) {
    if (channel.equals("BungeeCord")) {
      ByteArrayDataInput in = ByteStreams.newDataInput(msg);

      String subChannel = in.readUTF();
      if (subChannel.equals("PlayerCount")) {
        try {
          String server = in.readUTF();
          int players = in.readInt();
          if (ServerItem.SERVER_INFO.containsKey(server)) {
            ServerItem.SERVER_INFO.get(server).setPlayers(players);
          } else {
            ServerItem.SERVER_INFO.put(server, new ServerStatus(server, players));
          }
        } catch (Exception ignored) {}
      }
      if (subChannel.equals("ServerIP")) {
        String server = in.readUTF();
        String ip = in.readUTF();
        int port = in.readUnsignedShort();
        boolean connected = false;

        try {
          Socket socket = new Socket(ip, port);
          connected = socket.isConnected();
          socket.close();

        } catch (Exception ignored) {
        }
        if (ServerItem.SERVER_INFO.containsKey(server)) {
          ServerItem.SERVER_INFO.get(server).setUpStatus(connected);
        } else {
          ServerItem.SERVER_INFO.put(server, new ServerStatus(server, connected));
        }
      }
    }
    }

  }
