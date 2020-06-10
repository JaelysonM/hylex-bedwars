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
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.text.DecimalFormat;


public class EntityTeamSilverfish extends EntitySilverfish {

  private Arena arena;
  private Team team;

  public EntityTeamSilverfish(Arena arena, Team team) {
    super(((CraftWorld) Bukkit.getWorld(arena.getArenaName())).getHandle());

    this.arena = arena;
    this.team = team;

    NMS.clearPathfinderGoal(this);
    this.goalSelector.a(1, new PathfinderGoalFloat(this));
    this.goalSelector.a(4, new PathfinderGoalMeleeAttack(this, EntityHuman.class, 1.0D, false));

    this.targetSelector.a(1, new PathfinderGoalHurtByTarget(this, false));
    this.targetSelector.a(2, new EntityTeamSilverfish.PathfinderGoalNearestTeamTarget<>(this,EntityInsentient.class,10,false,true,IMonster.e, team));

    this.setCustomName(team.getTeamType().getTagColor() + "Time " + team.getTeamType().getName() );
    this.setCustomNameVisible(true);
  }

  @Override
  public void t_() {
    if (ticksLived >= 20 * 400) {
      dead = true;
      return;
    }
    double offset = 20 * 50 - this.ticksLived;
    this.setCustomName(team.getTeamType().getTagColor() + "Time " + team.getTeamType().getName());
    this.ticksLived++;
    super.t_();
  }

  static class PathfinderGoalNearestTeamTarget<T extends EntityLiving> extends PathfinderGoalNearestAttackableTarget<T> {
    public PathfinderGoalNearestTeamTarget(final EntityCreature entityCreature, Class<T> oclass, int i, boolean flag, boolean flag1, final Predicate<? super T> predicate,
      Team team) {
      super(entityCreature, oclass, i, flag, flag1, predicate);
      this.c = new Predicate<T>() {

        public boolean a(T tO) {
          if (predicate != null && !predicate.apply(tO)) {
            return false;

          } else if (tO instanceof EntityCreeper) {
            return false;
          } else if (tO instanceof EntityNPCPlayer) {
            return false;
          } else {
            if (tO instanceof EntityHuman) {
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
              if ((double) tO.g(entityCreature) > dO) {
                return false;
              }
            }


            if (tO instanceof Player) {
              Player player = (Player) tO;
              HylexPlayer hp = HylexPlayer.getByPlayer(player);
              if(hp !=null) {
                if (hp.getArenaPlayer() !=null) {
                  if (((ArenaPlayer)hp.getArenaPlayer()).getCurrentState().isInGame()) {
                    if (team !=null) {
                      if (((ArenaPlayer)hp.getArenaPlayer()).getTeam() !=null) {
                        if (((ArenaPlayer)hp.getArenaPlayer()).getTeam() != team) {
                          return PathfinderGoalNearestTeamTarget.this.a(tO, false);
                        }
                      }
                    }
                  }
                }
              }
            }
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

  public Team getTeam() {
    return team;
  }

  public void setTeam(Team team) {
    this.team = team;
  }

  public Arena getArena() {
    return arena;
  }
}
