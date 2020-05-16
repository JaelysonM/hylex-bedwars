package com.uzm.hylex.bedwars.nms.entity;

import net.minecraft.server.v1_8_R3.*;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Explosive;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ExplosionPrimeEvent;


public class EntityBedWarsFireball extends EntityLargeFireball {

  public EntityBedWarsFireball(Player shooter) {
    super(((CraftWorld) shooter.getWorld()).getHandle());
    this.shooter = ((CraftPlayer) shooter).getHandle();
  }

  public void t_() {
    if (this.ticksLived >= 200) {
      this.dead = true;
      return;
    }
    this.ticksLived++;
    this.motX*=5;
    this.motY*=5;
    this.motZ*=5;
    move(motX,motY,motZ);
    super.t_();
  }


  protected void a(MovingObjectPosition movingobjectposition) {
    if (!this.world.isClientSide) {
      if (movingobjectposition.entity != null) {
        movingobjectposition.entity.damageEntity(DamageSource.fireball(this, this.shooter), 6.0F);
        this.a(this.shooter, movingobjectposition.entity);
      }

      boolean flag = this.world.getGameRules().getBoolean("mobGriefing");
      ExplosionPrimeEvent event = new ExplosionPrimeEvent((Explosive) CraftEntity.getEntity(this.world.getServer(), this));
      this.world.getServer().getPluginManager().callEvent(event);
      if (!event.isCancelled()) {
        this.world.createExplosion(this, this.locX, this.locY, this.locZ, event.getRadius(), event.getFire(), flag);
      }

      this.die();
    }
  }
}
