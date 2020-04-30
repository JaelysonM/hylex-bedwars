package com.uzm.hylex.bedwars.arena.improvements;

import com.uzm.hylex.bedwars.arena.enums.BuyEnums;
import com.uzm.hylex.bedwars.arena.player.ArenaPlayer;
import com.uzm.hylex.bedwars.arena.team.Team;

public abstract class Trap {

  private String name;
  private int price;
  private BuyEnums coinTrade;

  public Trap(String name, int price, BuyEnums coinTrade) {
    this.name = name;
    this.price = price;
    this.coinTrade = coinTrade;
  }

  public void onEnter(Team owner, ArenaPlayer ap) {
    owner.setLastTrapped(ap.getPlayer());
  }

  public String getName() {
    return this.name;
  }

  public int getPrice() {
    return this.price;
  }

  public BuyEnums getCoinTrade() {
    return this.coinTrade;
  }
}
