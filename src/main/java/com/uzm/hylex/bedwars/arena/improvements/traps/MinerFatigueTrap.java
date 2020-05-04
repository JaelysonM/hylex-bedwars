package com.uzm.hylex.bedwars.arena.improvements.traps;

import com.uzm.hylex.bedwars.arena.enums.BuyEnums;
import com.uzm.hylex.bedwars.arena.improvements.Trap;
import com.uzm.hylex.bedwars.arena.player.ArenaPlayer;
import com.uzm.hylex.bedwars.arena.team.Team;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class MinerFatigueTrap extends Trap {

  public MinerFatigueTrap() {
    super("IRON_PICKAXE : 1 : flags=all : display={color}Armadilha de Cansaço! : lore=&7Inflige Fadiga I por 10\n&7segundos.", BuyEnums.DIAMOND);
  }

  @Override
  public void onEnter(Team owner, ArenaPlayer ap) {
    super.onEnter(owner, ap);
    if (!owner.equals(ap.getTeam()) && ap.getCurrentState().isInGame()) {
      owner.removeTrap(this);
      Player player = ap.getPlayer();
      player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 200, 0));
    }
  }
}
