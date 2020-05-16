package com.uzm.hylex.bedwars.protocol;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.uzm.hylex.bedwars.Core;
import org.bukkit.GameMode;

public class CameraAdapter extends PacketAdapter {

  public CameraAdapter() {
    super(params().plugin(Core.getInstance()).types(PacketType.Play.Server.CAMERA));
  }

  @Override
  public void onPacketSending(PacketEvent evt) {
    if (evt.getPacket().getType() == PacketType.Play.Server.CAMERA && evt.getPlayer().getGameMode() == GameMode.SPECTATOR) {
      evt.setCancelled(true);
    }
  }
}
