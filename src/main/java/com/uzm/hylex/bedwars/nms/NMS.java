package com.uzm.hylex.bedwars.nms;

import com.uzm.hylex.bedwars.arena.generators.Generator;
import com.uzm.hylex.bedwars.nms.entity.EntityGenerator;
import com.uzm.hylex.core.nms.reflections.Accessors;
import com.uzm.hylex.core.nms.reflections.acessors.FieldAccessor;
import net.minecraft.server.v1_8_R3.EntityTypes;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;

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
    Location location = generator.getLocation().clone().add(0, 2.4, 0);
    entityGenerator.setPosition(location.getX(), location.getY(), location.getZ());
    if (entityGenerator.world.addEntity(entityGenerator)) {
      ArmorStand armor = entityGenerator.getEntity();
      armor.setHelmet(generator.getType().getBlock());
      return entityGenerator;
    }

    return null;
  }
}
