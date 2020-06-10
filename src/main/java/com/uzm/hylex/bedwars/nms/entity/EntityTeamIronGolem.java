package com.uzm.hylex.bedwars.nms.entity;

import com.google.common.base.Predicate;
import com.uzm.hylex.bedwars.arena.Arena;
import com.uzm.hylex.bedwars.arena.player.ArenaPlayer;
import com.uzm.hylex.bedwars.arena.team.Team;
import com.uzm.hylex.bedwars.nms.NMS;
import com.uzm.hylex.core.api.HylexPlayer;
import com.uzm.hylex.core.nms.versions.v_1_8_R3.entity.EntityNPCPlayer;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityTargetEvent;

import javax.annotation.Nullable;
import java.text.DecimalFormat;



public class EntityTeamIronGolem extends EntityIronGolem {


  private Arena arena;
  private Team team;
  private static final DecimalFormat DECIMAL_FORMAT_2 = new DecimalFormat("##.##");
  private Entity targetE;

  public EntityTeamIronGolem(Arena arena, Team team) {
    super(((CraftWorld) Bukkit.getWorld(arena.getArenaName())).getHandle());

    this.arena = arena;
    this.team = team;

    NMS.clearPathfinderGoal(this);
    //((Navigation) this.getNavigation()).a(true);
    this.goalSelector.a(1, new PathfinderGoalMeleeAttack(this, 1.0D, false));
    this.goalSelector.a(2, new PathfinderGoalMoveTowardsTarget(this, 0.9D, 32.0F));
    this.goalSelector.a(3, new PathfinderGoalMoveTowardsRestriction(this, 1.0D));
    this.goalSelector.a(4, new PathfinderGoalRandomLookaround(this));
    // this.targetSelector.a(0, new EntityTeamIronGolem.PathfinderGoalNearestTeamTarget<>(this, EntityInsentient.class, 10, false, true, null, team));

    this.setCustomName(team.getTeamType().getTagColor() + "Time " + team.getTeamType().getName() + " §7- §e50s");
    this.setCustomNameVisible(true);
  }

  @Override
  public void t_() {
    if (ticksLived >= 20 * 150) {
      dead = true;
      return;
    }
    double offset = 20 * 150 - this.ticksLived;
    this.setCustomName(team.getTeamType().getTagColor() + "Time " + team.getTeamType().getName() + " §7- §e" + DECIMAL_FORMAT_2.format(offset / 20) + "s");
    this.ticksLived++;
    if (targetE !=null) {
      if (targetE.getBukkitEntity().getLocation().distance(getBukkitEntity().getLocation()) > 30 || !targetE.isAlive()) {
        targetE = null;
      }
    }

    if (this.targetE == null) {
      Player target = this.getBukkitEntity().getNearbyEntities(10, 10, 10).stream().filter(e -> e instanceof Player).map(e -> (Player) e)
        .filter(player -> HylexPlayer.getByPlayer(player) != null).filter(player -> HylexPlayer.getByPlayer(player).getArenaPlayer() != null)
        .filter(ap -> ((ArenaPlayer) HylexPlayer.getByPlayer(ap).getArenaPlayer()).getCurrentState().isInGame())
        .filter(ap -> ((ArenaPlayer) HylexPlayer.getByPlayer(ap).getArenaPlayer()).getTeam() != null)
        .filter(ap -> ((ArenaPlayer) HylexPlayer.getByPlayer(ap).getArenaPlayer()).getTeam() != getTeam()).findFirst().orElse(null);
      if (target != null)
        setGoalTarget(((CraftLivingEntity) target).getHandle(), EntityTargetEvent.TargetReason.CUSTOM, false);
    }
    super.t_();
  }


/*
  static class PathfinderGoalNearestTeamTarget<T extends EntityLiving> extends PathfinderGoalNearestAttackableTarget<T> {
    public PathfinderGoalNearestTeamTarget(final EntityCreature entityCreature, Class<T> oclass, int i, boolean flag, boolean flag1, final Predicate<? super T> predicate,
      Team team) {
      super(entityCreature, oclass, i, flag, flag1, predicate);
      this.c = new Predicate<T>() {

        public boolean a(T tO) {
          if (tO instanceof EntityCreeper) {
            return false;
          } else if (tO instanceof EntityNPCPlayer) {
            return false;
          } else {
            System.out.println(tO);
            if (tO instanceof EntityPlayer) {
              double dO = PathfinderGoalNearestTeamTarget.this.f();

              if (tO.isSneaking()) {
                dO *= 0.800000011920929D;
              }
              if (tO.isInvisible()) {
                float f = ((EntityHuman) tO).bY();
                if (f < 0.1F) {
                  f = 0.1F;
                }
                dO *= (double) (0.7F * f);
              }
              /*
              if ((double) tO.g(entityCreature) > dO) {
                return false;
              }

              System.out.println("C");
              Player player = ((EntityPlayer) tO).getBukkitEntity().getPlayer();
              HylexPlayer hp = HylexPlayer.getByPlayer(player);
              if (hp != null) {
                if (hp.getArenaPlayer() != null) {
                  if (((ArenaPlayer) hp.getArenaPlayer()).getCurrentState().isInGame()) {
                    if (team != null) {
                      if (((ArenaPlayer) hp.getArenaPlayer()).getTeam() != null) {
                        if (((ArenaPlayer) hp.getArenaPlayer()).getTeam() != team) {
                          System.out.println("D");
                          return PathfinderGoalNearestTeamTarget.this.a(tO, false);

                        }
                      }
                    }
                  }
                }
              }

              }
         //   }
            return false;
          }

        }

        @Override
        public boolean apply(@Nullable T t) {
          return a(t);
        }
      };

    }
  }

*/

  public Team getTeam() {
    return team;
  }

  public void setTeam(Team team) {
    this.team = team;
  }

  public Arena getArena() {
    return arena;
  }

  static class PathfinderGoalNearestTeamTarget<T extends EntityLiving> extends PathfinderGoalNearestAttackableTarget<T> {
    public PathfinderGoalNearestTeamTarget(final EntityCreature entitycreature, Class<T> oclass, int i, boolean flag, boolean flag1, final Predicate<? super T> predicate,
      Team team) {
      super(entitycreature, oclass, i, flag, flag1, predicate);
      this.c = new Predicate() {
        public boolean a(T t0) {
          if (t0 instanceof EntityCreeper) {
            return false;
          } else {
            System.out.println(t0);
            if (t0 instanceof EntityHuman) {

              double d0 = PathfinderGoalNearestTeamTarget.this.f();
              if (t0.isSneaking()) {
                d0 *= 0.800000011920929D;
              }

              if (t0.isInvisible()) {
                float f = ((EntityHuman) t0).bY();
                if (f < 0.1F) {
                  f = 0.1F;
                }

                d0 *= (double) (0.7F * f);
              }

              if ((double) t0.g(entitycreature) > d0) {
                return false;
              }
            }

            return PathfinderGoalNearestTeamTarget.this.a(t0, false);
          }
        }

        public boolean apply(Object object) {
          return this.a((T) object);
        }
      };
    }
  }
}
