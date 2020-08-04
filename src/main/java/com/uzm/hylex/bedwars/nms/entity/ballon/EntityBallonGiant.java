package com.uzm.hylex.bedwars.nms.entity.ballon;

import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;

public class EntityBallonGiant extends EntityGiantZombie {

  private org.bukkit.inventory.ItemStack[] frames;

  private int animationTick;


  private Location location;

  public EntityBallonGiant(Location location, int animationTick, org.bukkit.inventory.ItemStack... items) {
    super(((CraftWorld) location.getWorld()).getHandle());
    super.setInvisible(true);

    this.location = location.clone();
    this.setPosition(location.getX(), location.getY(), location.getZ());
    this.frames = items;

    this.animationTick=animationTick;

    this.setEquipment(0, CraftItemStack.asNMSCopy(frames[0]));

  }

  public void kill() {
    this.dead = true;
  }

  private int frame = 0;

  @Override
  public void t_() {
    this.motY = 0.0;
    this.setPosition(location.getX(), location.getY(), location.getZ());
    if (this.frames == null) {
      this.kill();
      return;
    }

    if (this.frame >= this.frames.length) {
      this.frame = 0;
    }

    super.t_();
    if (MinecraftServer.currentTick % 10 == 0) {
      this.setEquipment(0, CraftItemStack.asNMSCopy(frames[frame++]));
    }
  }

  @Override
  public void makeSound(String s, float f, float f1) {}

  @Override
  protected boolean a(EntityHuman entityhuman) {
    return false;
  }

  @Override
  public boolean isInvulnerable(DamageSource damagesource) {
    return true;
  }

  @Override
  public void setCustomName(String s) {}

  @Override
  public void setCustomNameVisible(boolean flag) {}

  @Override
  public boolean d(int i, ItemStack itemstack) {
    return false;
  }

  @Override
  public void die() {}

  @Override
  public boolean damageEntity(DamageSource damagesource, float f) {
    return false;
  }

  @Override
  public void setInvisible(boolean flag) {}

  public void a(NBTTagCompound nbttagcompound) {}

  public void b(NBTTagCompound nbttagcompound) {}

  public boolean c(NBTTagCompound nbttagcompound) {
    return false;
  }

  public boolean d(NBTTagCompound nbttagcompound) {
    return false;
  }

  public void e(NBTTagCompound nbttagcompound) {}

  public void f(NBTTagCompound nbttagcompound) {}
}
