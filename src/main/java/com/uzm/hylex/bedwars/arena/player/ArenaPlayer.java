package com.uzm.hylex.bedwars.arena.player;

import com.uzm.hylex.bedwars.Core;
import com.uzm.hylex.bedwars.arena.Arena;
import com.uzm.hylex.bedwars.arena.team.Team;
import com.uzm.hylex.core.api.interfaces.IArenaPlayer;
import com.uzm.hylex.core.nms.NMS;
import com.uzm.hylex.core.spigot.features.Titles;
import com.uzm.hylex.core.spigot.items.ItemBuilder;
import com.uzm.hylex.core.spigot.scoreboards.AsyncScoreboard;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Objects;

import static com.uzm.hylex.bedwars.arena.improvements.UpgradeType.*;

public class ArenaPlayer implements IArenaPlayer {

  private Arena arena;
  private Player player;
  private CurrentState currentState;
  private Team team;
  private AsyncScoreboard scoreboard;
  private ArenaEquipment equipment;
  private int beds_broken;
  private int final_kills;
  private int kills;

  public ArenaPlayer(Player player, Arena arena) {
    this.arena = arena;
    this.player = player;
    this.currentState = CurrentState.WAITING;
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
    this.scoreboard.delete();
    this.scoreboard = null;
    this.equipment = null;
  }

  public void update() {
    switch (getCurrentState()) {
      case DEAD:
        if (player != null) {
          NMS.sendTitle(player, Titles.TitleType.BOTH, "§fVocê foi eliminado!", "§c§lDERROTA", 10, 60, 0);
          player.setMaxHealth(20.0D);
          player.setHealth(20.0D);
          player.setNoDamageTicks(20 * 5);
        }
      case SPECTATING:
        if (getArena() != null) {
          getArena().getArenaPlayers().stream().filter(Objects::nonNull).map(a -> (ArenaPlayer) a).forEach(ap -> {
            Player players = ap.getPlayer();
            if (players != null) {
              player.showPlayer(players);
              if (ap.getCurrentState().isInGame()) {
                players.hidePlayer(player);
              } else {
                players.showPlayer(player);
              }
            }
          });
        }

        org.bukkit.scoreboard.Team team = player.getScoreboard().getPlayerTeam(player);
        if (team != null) {
          team.removePlayer(player);
        }
        if (!Core.team.hasPlayer(player)) {
          Core.team.addPlayer(player);
        }

        player.setGameMode(GameMode.ADVENTURE);
        player.setAllowFlight(true);

        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0));

        player.getInventory().clear();
        player.getInventory().setArmorContents(new ItemStack[4]);

        player.getInventory().setItem(1, new ItemBuilder(Material.PAPER).name("§bJogar Novamente").lore("§7Clique para se conectar a outra sala.").build());

        player.getInventory().setItem(8, new ItemBuilder(Material.BED).name("§cVoltar ao Lobby").lore("§7Clique para voltar ao Lobby.").build());

        player.updateInventory();
        break;
      case RESPAWNING:
        player.getInventory().clear();
        player.getInventory().setArmorContents(new ItemStack[4]);

        com.uzm.hylex.bedwars.nms.NMS.sendFakeSpectator(player);
        new BukkitRunnable() {
          int count = 5;

          @Override
          public void run() {
            if (player == null) {
              cancel();
              return;
            }

            if (count == 0) {
              cancel();
              if (player.isOnline()) {
                setCurrentState(CurrentState.IN_GAME);
                update();
                NMS.sendTitle(player, Titles.TitleType.BOTH, "", "", 0, 0, 0);
                player.teleport(getTeam().getSpawnLocation());

                getArena().getArenaPlayers().stream().map(a -> ((ArenaPlayer) a).getPlayer()).forEach(players -> players.showPlayer(player));
              }
              return;
            }

            if (player.isOnline()) {
              NMS.sendTitle(player, Titles.TitleType.BOTH, "§fRespawnando em " + count + " segundo" + (count > 1 ? "s" : "") + "!", "§c§lVOCÊ MORREU", 0, 20, 0);
            }

            count--;
          }
        }.runTaskTimer(Core.getInstance(), 0, 20);
        break;
      case IN_GAME:
        player.setGameMode(GameMode.SURVIVAL);
        equip();
        refresh();
        player.setNoDamageTicks(60);
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
    if (this.team != null) {
      this.equipment = new ArenaEquipment(this);
    }
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

  public void addKills() {
    this.kills++;
  }

  public void addBedBroken() {
    this.beds_broken++;
  }

  public void addFinalKill() {
    this.final_kills++;
  }

  public int getKills() {
    return this.kills;
  }

  public int getBedsBroken() {
    return this.beds_broken;
  }

  public int getFinalKills() {
    return this.final_kills;
  }

  public enum CurrentState {
    WAITING("Aguardando", false, false),
    IN_GAME("Em jogo", true, false),
    RESPAWNING("Renascendo", true, true),
    DEAD("Eliminado", false, true),
    SPECTATING("Assitindo", false, true);

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
      this.isSpectating = isSpectating;

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
