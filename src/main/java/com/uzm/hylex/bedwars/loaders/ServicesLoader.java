package com.uzm.hylex.bedwars.loaders;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.uzm.hylex.bedwars.Core;
import com.uzm.hylex.bedwars.controllers.HylexPlayer;
import com.uzm.hylex.core.java.util.ConfigurationCreator;
import com.uzm.hylex.services.lan.WebSocket;
import org.json.JSONObject;

public class ServicesLoader {

  public ServicesLoader(PluginLoader loader) {
    WebSocket socket =
      WebSocket.create("bedwars-" + loader.getServerName(), ConfigurationCreator.find("setup", loader.getCore()).get().getString("server-configuration.services-address"));
    socket.addQueryParam("?server=" + "bedwars-" + loader.getServerName());

    socket.build();
    socket.sendHeaders("Authorization", "00f1ff268656703e14faf2d05");
    socket.connect().setup();

    buildEvents(loader);
  }

  private void buildEvents(PluginLoader loader) {
    WebSocket socket = WebSocket.get("bedwars-" + loader.getServerName());

    socket.getSocket().on("bedwarsprofile-callback", args -> {
      if (!(args[0] instanceof JSONObject)) {
        return;
      }
      JsonObject response = new JsonParser().parse(args[0].toString()).getAsJsonObject();

      HylexPlayer player = HylexPlayer.get(response.get("uuid").getAsString());

      if (player != null) {
        player.setName(response.get("nickname").getAsString());
        player.setID(response.get("_id").getAsString());
        player.accountLoad();
        player.setupHotBar();
      } else {
        System.err.println("[Hylex - Socket.io] A conta enviada com o UUID não está registrada ou não foi carregada | Code: " + "Invalid Account");
      }
    });
  }
}
