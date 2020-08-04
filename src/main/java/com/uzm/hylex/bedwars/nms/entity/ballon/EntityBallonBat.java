package com.uzm.hylex.bedwars.nms.entity.ballon;

import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;

public class EntityBallonBat extends EntityBat  {

  public EntityBallonBat(Location location, EntityBallonLeash leash) {
    super(((CraftWorld) location.getWorld()).getHandle());

    super.setInvisible(true);
    this.setLeashHolder(leash, true);

    this.setPosition(location.getX(), location.getY(), location.getZ());

  }

  public void kill() {
    this.dead = true;
  }

  @Override
  public void t_() {}

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
