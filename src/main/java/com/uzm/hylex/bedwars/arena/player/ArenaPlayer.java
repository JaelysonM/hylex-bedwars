package com.uzm.hylex.bedwars.arena.player;

import com.uzm.hylex.bedwars.arena.Arena;
import com.uzm.hylex.bedwars.arena.team.Team;
import com.uzm.hylex.core.spigot.scoreboards.AsyncScoreboard;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import static com.uzm.hylex.bedwars.arena.improvements.UpgradeType.*;

public class ArenaPlayer {

  private Arena arena;
  private Player player;
  private CurrentState currentState;
  private Team team;
  private AsyncScoreboard scoreboard;
  private ArenaEquipment equipment;

  public ArenaPlayer(Player player, Arena arena) {
    this.arena = arena;
    this.player = player;
    this.setScoreboard(new AsyncScoreboard(player));
  }

  public void setScoreboard(AsyncScoreboard scoreboard) {
    this.scoreboard = scoreboard;
    this.scoreboard.updateTitle("§b§lBED WARS");
  }

  public void destroy() {
    this.arena = null;
    this.player = null;
    this.currentState = null;
    this.team = null;
    this.scoreboard = null;
    this.equipment = null;
  }

  public void update() {
    switch (getCurrentState()) {
      case DEAD:
        if (getTeam() != null) {
          // VOU RMORREU BLABLA
          // ITENS PARA ESPECTAR
        }

        break;
      case SPECTATING:
        ///APENAS ASSITINDO
        break;
      case RESPAWNING:
        ///START RESPAWN TASk
        break;
      case IN_GAME:
        refresh();
        break;
    }
  }

  public void equip() {
    this.equipment.refresh();
  }

  public void refresh() {
    if (this.team.hasUpgrade(MANIAC_MINER)) {
      player.removePotionEffect(PotionEffectType.FAST_DIGGING);
      player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, Integer.MAX_VALUE, this.team.getTier(MANIAC_MINER) - 1));
    }

    if (this.team.hasUpgrade(SHARPENED_SWORDS)) {
      for (int i = 0; i < player.getInventory().getSize(); i++) {
        ItemStack item = player.getInventory().getItem(i);
        if (item != null && (item.getType().name().contains("_SWORD") || item.getType().name().contains("_AXE"))) {
          if (item.containsEnchantment(Enchantment.DAMAGE_ALL)) {
            item.removeEnchantment(Enchantment.DAMAGE_ALL);
          }

          item.addEnchantment(Enchantment.DAMAGE_ALL, this.team.getTier(SHARPENED_SWORDS));
          player.getInventory().setItem(i, item);
          player.updateInventory();
        }
      }
    }

    if (this.team.hasUpgrade(REINFORCED_ARMOR)) {
      ItemStack[] items = player.getInventory().getArmorContents();
      for (ItemStack item : items) {
        if (item != null && item.getType() != Material.AIR) {
          if (item.containsEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL)) {
            item.removeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL);
          }

          item.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, this.team.getTier(REINFORCED_ARMOR));
        }
      }
    }
  }

  public void setTeam(Team team) {
    this.team = team;
    if (this.equipment != null) {
      this.equipment.destroy();
    }
    this.equipment = new ArenaEquipment(this);
  }

  public void setCurrentState(CurrentState currentState) {
    this.currentState = currentState;
  }

  public Player getPlayer() {
    return this.player;
  }

  public Arena getArena() {
    return this.arena;
  }

  public AsyncScoreboard getScoreboard() {
    return this.scoreboard;
  }

  public Team getTeam() {
    return this.team;
  }

  public ArenaEquipment getEquipment() {
    return this.equipment;
  }

  public CurrentState getCurrentState() {
    return this.currentState;
  }

  public enum CurrentState {
    IN_GAME("Em jogo", true,false),
    DEAD("Eliminado",false, true),
    SPECTATING("Assitindo",false ,true),
    RESPAWNING("Renascendo", true,true);

    private String name;

    private boolean isSpectating;
    private boolean isInGame;

    CurrentState(String name) {
      this.name = name;
    }

    CurrentState(String name, boolean isInGame) {
      this.name = name;
      this.isInGame = isInGame;
    }
    CurrentState(String name, boolean isInGame, boolean isSpectating) {
      this.name = name;
      this.isInGame = isInGame;
      this.isSpectating=isSpectating;

    }

    public String toString() {
      return this.name;
    }

    public boolean isInGame() {
      return this.isInGame;
    }

    public boolean isSpectating() {
      return isSpectating;
    }
  }
}
