package com.uzm.hylex.bedwars.utils;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.uzm.hylex.bedwars.Core;
import org.bukkit.entity.Player;

public class ProxyUtils {
  public static void sendPartyMembers(Player leader, String mega, String mini) {
    ByteArrayDataOutput out = ByteStreams.newDataOutput();
    out.writeUTF("SendPartyMembers");
    out.writeUTF(leader.getName());
    out.writeUTF(mega);
    out.writeUTF(mini);
    leader.sendPluginMessage(Core.getInstance(), "hylex-core", out.toByteArray());
  }
}
