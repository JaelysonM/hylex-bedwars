package com.uzm.hylex.bedwars.arena.improvements.traps;

import com.uzm.hylex.bedwars.arena.enums.BuyEnums;
import com.uzm.hylex.bedwars.arena.improvements.Trap;
import com.uzm.hylex.bedwars.arena.player.ArenaPlayer;
import com.uzm.hylex.bedwars.arena.team.Team;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ItsaTrap extends Trap  {

  public ItsaTrap() {
    super("É uma armadilha!", 5, BuyEnums.DIAMOND);
  }

  @Override
  public void onEnter(Team owner, ArenaPlayer ap) {
    super.onEnter(owner, ap);
    if (!owner.equals(ap.getTeam()) && ap.getCurrentState().isInGame()) {
      owner.removeTrap(this);
      Player player = ap.getPlayer();
      player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 0));
      player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 1));
    }
  }
}
