package com.uzm.hylex.bedwars.nms.entity;

import com.uzm.hylex.bedwars.arena.generators.Generator;
import com.uzm.hylex.core.libraries.holograms.api.HologramLine;
import com.uzm.hylex.core.nms.interfaces.IArmorStand;
import com.uzm.hylex.core.nms.reflections.Accessors;
import com.uzm.hylex.core.nms.reflections.acessors.FieldAccessor;
import com.uzm.hylex.core.nms.versions.v_1_8_R3.utils.NullBoundingBox;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftArmorStand;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.ArmorStand;

public class EntityGenerator extends EntityArmorStand implements IArmorStand {

  private static final FieldAccessor<Integer> BI = Accessors.getField(EntityArmorStand.class, "bi", Integer.class);

  private static double square(double num) {
    return num * num;
  }

  private Generator generator;

  public EntityGenerator(Generator generator) {
    super(((CraftWorld) generator.getLocation().getWorld()).getHandle());

    this.setArms(false);
    this.setBasePlate(true);
    this.setInvisible(true);
    this.setGravity(false);
    this.setSmall(true);

    a(new NullBoundingBox());
    BI.set(this, Integer.MAX_VALUE);
  }

  public boolean isInvulnerable(DamageSource source) {
    return true;
  }

  @Override
  public void setCustomName(String s) {}

  @Override
  public void setCustomNameVisible(boolean flag) {}

  @Override
  public void t_() {
    if (this.generator == null) {
      this.dead = true;
    }

    this.ticksLived = 0;
    super.t_();
    if (!this.dead) {
      this.generator.tick();
    }
  }

  public void makeSound(String sound, float f1, float f2) {}

  @Override
  public int getId() {
    return super.getId();
  }

  @Override
  public void setName(String text) {
    if (text != null && text.length() > 300) {
      text = text.substring(0, 300);
    }

    super.setCustomName(text == null ? "" : text);
    super.setCustomNameVisible(text != null && !text.isEmpty());
  }

  @Override
  public void killEntity() {
    super.die();
  }

  @Override
  public HologramLine getLine() {
    return null;
  }

  @Override
  public ArmorStand getEntity() {
    return (ArmorStand) getBukkitEntity();
  }

  @Override
  public CraftEntity getBukkitEntity() {
    if (bukkitEntity == null) {
      bukkitEntity = new CraftStand(this);
    }

    return super.getBukkitEntity();
  }

  @Override
  public void setLocation(double x, double y, double z) {
    super.setPosition(x, y, z);

    PacketPlayOutEntityTeleport teleportPacket =
      new PacketPlayOutEntityTeleport(getId(), MathHelper.floor(this.locX * 32.0D), MathHelper.floor(this.locY * 32.0D), MathHelper.floor(this.locZ * 32.0D),
        (byte) (int) (this.yaw * 256.0F / 360.0F), (byte) (int) (this.pitch * 256.0F / 360.0F), this.onGround);

    for (EntityHuman obj : world.players) {
      if (obj instanceof EntityPlayer) {
        EntityPlayer nmsPlayer = (EntityPlayer) obj;

        double distanceSquared = square(nmsPlayer.locX - this.locX) + square(nmsPlayer.locZ - this.locZ);
        if (distanceSquared < 8192.0 && nmsPlayer.playerConnection != null) {
          nmsPlayer.playerConnection.sendPacket(teleportPacket);
        }
      }
    }
  }

  @Override
  public boolean isDead() {
    return dead;
  }

  static class CraftStand extends CraftArmorStand implements IArmorStand {

    public CraftStand(EntityGenerator entity) {
      super(entity.world.getServer(), entity);
    }

    @Override
    public int getId() {
      return entity.getId();
    }

    @Override
    public void setName(String text) {
      ((EntityGenerator) entity).setName(text);
    }

    @Override
    public void killEntity() {
      ((EntityGenerator) entity).killEntity();
    }

    @Override
    public HologramLine getLine() {
      return ((EntityGenerator) entity).getLine();
    }

    @Override
    public ArmorStand getEntity() {
      return this;
    }

    @Override
    public void setLocation(double x, double y, double z) {
      ((EntityGenerator) entity).setLocation(x, y, z);
    }
  }
}
