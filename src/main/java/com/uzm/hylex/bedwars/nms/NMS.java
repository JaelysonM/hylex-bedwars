package com.uzm.hylex.bedwars.nms;

import com.uzm.hylex.bedwars.arena.generators.Generator;
import com.uzm.hylex.bedwars.nms.entity.EntityGenerator;
import com.uzm.hylex.core.nms.reflections.Accessors;
import com.uzm.hylex.core.nms.reflections.acessors.FieldAccessor;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.EntityTypes;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_8_R3.WorldSettings;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"unchecked", "rawtypes"})
public class NMS {

  private static final FieldAccessor<Map> CLASS_TO_ID, CLASS_TO_NAME;

  static {
    CLASS_TO_ID = Accessors.getField(EntityTypes.class, "f", Map.class);
    CLASS_TO_NAME = Accessors.getField(EntityTypes.class, "d", Map.class);

    CLASS_TO_ID.get(null).put(EntityGenerator.class, 30);
    CLASS_TO_NAME.get(null).put(EntityGenerator.class, "EntityGenerator");
  }

  public static EntityGenerator createGeneratorEntity(Generator generator) {
    EntityGenerator entityGenerator = new EntityGenerator(generator);
    Location location = generator.getLocation().clone().add(0, 1.5, 0);
    entityGenerator.setPosition(location.getX(), location.getY(), location.getZ());
    if (entityGenerator.world.addEntity(entityGenerator, CreatureSpawnEvent.SpawnReason.CUSTOM)) {
      ArmorStand armor = entityGenerator.getEntity();
      armor.setHelmet(generator.getType().getBlock());
      return entityGenerator;
    }

    return null;
  }

  public static void sendFakeSpectator(Player player) {
    player.setGameMode(GameMode.SPECTATOR);
    EntityPlayer ep = ((CraftPlayer) player).getHandle();
    PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_GAME_MODE, ep);
    FieldAccessor<Object> accessor = Accessors.getField(packet.getClass(), "b");
    List<PacketPlayOutPlayerInfo.PlayerInfoData> infoList = new ArrayList<>();
    infoList.add(packet.new PlayerInfoData(ep.getProfile(), ep.ping, WorldSettings.EnumGamemode.CREATIVE, ep.listName));
    accessor.set(packet, infoList);

    ep.playerConnection.sendPacket(packet);
  }
}
