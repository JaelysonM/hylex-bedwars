package com.uzm.hylex.bedwars.arena.improvements;

import com.uzm.hylex.bedwars.arena.enums.BuyEnums;
import com.uzm.hylex.bedwars.arena.improvements.traps.AlarmTrap;
import com.uzm.hylex.bedwars.arena.improvements.traps.CounterOffensiveTrap;
import com.uzm.hylex.bedwars.arena.improvements.traps.ItsaTrap;
import com.uzm.hylex.bedwars.arena.improvements.traps.MinerFatigueTrap;
import com.uzm.hylex.bedwars.arena.player.ArenaPlayer;
import com.uzm.hylex.bedwars.arena.team.Team;
import com.uzm.hylex.core.spigot.features.Titles;

import java.util.LinkedHashSet;
import java.util.Set;

public abstract class Trap {

  private String icon;
  private BuyEnums coinTrade;

  public Trap(String icon, BuyEnums coinTrade) {
    this.icon = icon;
    this.coinTrade = coinTrade;
  }

  public void onEnter(Team owner, ArenaPlayer ap) {
    owner.setLastTrapped(ap.getPlayer());
    owner.getMembers().forEach(
      aps -> new Titles(aps.getPlayer(), Titles.TitleType.BOTH).setTopMessage("§c§lARMADILHA ATIVADA").setBottomMessage("§fUm jogador caiu na armadilha").send(20, 120, 20));
  }

  public String getIcon() {
    return this.icon;
  }

  public BuyEnums getCoinTrade() {
    return this.coinTrade;
  }

  private static final Set<Trap> TRAPS = new LinkedHashSet<>();

  public static void setupTraps() {
    TRAPS.add(new ItsaTrap());
    TRAPS.add(new AlarmTrap());
    TRAPS.add(new CounterOffensiveTrap());
    TRAPS.add(new MinerFatigueTrap());
  }

  public static Set<Trap> listTraps() {
    return TRAPS;
  }
}
