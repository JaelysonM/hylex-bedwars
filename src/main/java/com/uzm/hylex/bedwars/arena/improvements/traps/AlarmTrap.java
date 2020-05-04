package com.uzm.hylex.bedwars.arena.improvements.traps;

import com.uzm.hylex.bedwars.arena.enums.BuyEnums;
import com.uzm.hylex.bedwars.arena.improvements.Trap;
import com.uzm.hylex.bedwars.arena.player.ArenaPlayer;
import com.uzm.hylex.bedwars.arena.team.Team;
import org.bukkit.potion.PotionEffectType;

public class AlarmTrap extends Trap {

  public AlarmTrap() {
    super("REDSTONE_TORCH_ON : 1 : display={color}Alarme : lore=&7Revela jogadores invisíveis que\n&7entrarem em sua base.", BuyEnums.DIAMOND);
  }

  @Override
  public void onEnter(Team owner, ArenaPlayer ap) {
    super.onEnter(owner, ap);
    if (!owner.equals(ap.getTeam()) && ap.getCurrentState().isInGame()) {
      owner.removeTrap(this);
      ap.getPlayer().removePotionEffect(PotionEffectType.INVISIBILITY);
    }
  }
}
