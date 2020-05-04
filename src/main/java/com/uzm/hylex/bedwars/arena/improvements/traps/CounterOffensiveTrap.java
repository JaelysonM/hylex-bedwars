package com.uzm.hylex.bedwars.arena.improvements.traps;

import com.uzm.hylex.bedwars.arena.enums.BuyEnums;
import com.uzm.hylex.bedwars.arena.improvements.Trap;
import com.uzm.hylex.bedwars.arena.player.ArenaPlayer;
import com.uzm.hylex.bedwars.arena.team.Team;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class CounterOffensiveTrap extends Trap {

  public CounterOffensiveTrap() {
    super("FEATHER : 1 : display={color}Armadilha Contra Ofensiva : lore=&7Garante Velocidade I e Super Pulo II\n&7por 10 segundos para os aliados\n&7perto de sua base.",
      BuyEnums.DIAMOND);
  }

  @Override
  public void onEnter(Team owner, ArenaPlayer ap) {
    super.onEnter(owner, ap);
    if (!owner.equals(ap.getTeam()) && ap.getCurrentState().isInGame()) {
      owner.removeTrap(this);
      owner.getAlive().forEach(aps -> {
        if (aps.getCurrentState() == ArenaPlayer.CurrentState.IN_GAME && owner.getBorder().contains(aps.getPlayer().getLocation())) {
          Player player = aps.getPlayer();
          player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 0));
          player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 200, 1));
        }
      });
    }
  }
}
