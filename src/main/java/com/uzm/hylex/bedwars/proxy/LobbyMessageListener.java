package com.uzm.hylex.bedwars.proxy;


import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.uzm.hylex.core.api.HylexPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;



public class LobbyMessageListener implements org.bukkit.plugin.messaging.PluginMessageListener {

  @Override
  public void onPluginMessageReceived(String channel, Player receiver, byte[] msg) {
    if (channel.equals("hylex-core")) {
      ByteArrayDataInput in = ByteStreams.newDataInput(msg);

      String subChannel = in.readUTF();
      if (subChannel.equals("SendPlayerLobby")) {
        try {
          String name = in.readUTF();
          String type = in.readUTF();

          if (Bukkit.getPlayerExact(name) != null) {
            Player player = Bukkit.getPlayerExact(name);
            if (type.equalsIgnoreCase("bedwars")) {
              HylexPlayer hp = HylexPlayer.getByPlayer(player);
              if (hp != null) {
                ServerItem.getServerItem("lobby").connect(hp);

              }

            }
          }



        } catch (Exception ignored) {}
      }

    }
  }

}
