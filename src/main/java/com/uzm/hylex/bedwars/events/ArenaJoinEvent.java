package com.uzm.hylex.bedwars.events;

import com.uzm.hylex.bedwars.arena.Arena;
import com.uzm.hylex.bedwars.arena.player.ArenaPlayer;
import com.uzm.hylex.bedwars.controllers.HylexPlayer;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;



public class ArenaJoinEvent extends Event implements Cancellable {

  private ArenaPlayer ap;
  private HylexPlayer hp;
  private Arena arena;
  private boolean canceled;

  private static final HandlerList handlers = new HandlerList();



  public ArenaJoinEvent(HylexPlayer player , Arena arena) {
    this.hp=player;
    this.arena=arena;
  }

  public HandlerList getHandlers() {
    return handlers;
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }

  @Override
  public boolean isCancelled() {
    return canceled;
  }

  @Override
  public void setCancelled(boolean b) {
    this.canceled = b;
  }

  public ArenaPlayer getArenaPlayer() {
    return ap;
  }

  public HylexPlayer getHylexPlayer() {
    return hp;
  }

  public Arena getArena() {
    return arena;
  }
}
