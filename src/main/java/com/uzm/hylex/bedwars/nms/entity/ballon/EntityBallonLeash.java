package com.uzm.hylex.bedwars.nms.entity.ballon;

import net.minecraft.server.v1_8_R3.DamageSource;
import net.minecraft.server.v1_8_R3.EntityLeash;
import net.minecraft.server.v1_8_R3.ItemStack;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;


public class EntityBallonLeash extends EntityLeash  {

  public EntityBallonLeash(Location location) {
    super(((CraftWorld) location.getWorld()).getHandle());

    this.setPosition(location.getX(), location.getY(), location.getZ());
  }
  public void kill() {
    this.dead = true;
  }

  @Override
  public void t_() {}

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
