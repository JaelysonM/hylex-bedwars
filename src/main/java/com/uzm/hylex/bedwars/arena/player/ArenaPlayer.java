package com.uzm.hylex.bedwars.arena.player;

import com.uzm.hylex.bedwars.Core;
import com.uzm.hylex.bedwars.arena.Arena;
import com.uzm.hylex.bedwars.arena.team.Team;
import com.uzm.hylex.bedwars.controllers.HylexPlayerController;
import com.uzm.hylex.core.api.HylexPlayer;
import com.uzm.hylex.core.api.interfaces.IArenaPlayer;
import com.uzm.hylex.core.java.util.StringUtils;
import com.uzm.hylex.core.nms.NMS;
import com.uzm.hylex.core.spigot.features.Titles;
import com.uzm.hylex.core.spigot.items.ItemBuilder;
import com.uzm.hylex.core.spigot.scoreboards.AsyncScoreboard;
import com.uzm.hylex.core.spigot.scoreboards.scroller.Scroller;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

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

  private int coinsEarned = 0;
  private int expEarned = 0;

  private long startedTime;

  public ArenaPlayer(HylexPlayer player, Arena arena) {
    this.arena = arena;
    this.player = player.getPlayer();
    this.currentState = CurrentState.WAITING;
    /*Scroller  scroller = new Scroller("§6§lBEDWARS", "§6", "§c", "§f", true, Scroller.ScrollType.FORWARD, "§c§lBEDWARS",
      "§f§lBEDWARS","§c§lBEDWARS");
      */

    this.setScoreboard(new AsyncScoreboard(player.getPlayer()));
  }

  public void setScoreboard(AsyncScoreboard scoreboard) {
    this.scoreboard = scoreboard;
    this.scoreboard.updateTitle("§6§lBEDWARS");
  }

  public void destroy() {
    this.arena = null;
    this.player = null;
    this.currentState = null;
    this.team = null;
    this.scoreboard.delete();
    this.scoreboard = null;
    this.equipment = null;
    this.coinsEarned = 0;
    this.final_kills = 0;
    this.beds_broken = 0;
    this.kills = 0;
  }

  public void update() {
    if (getPlayer() != null) {
      switch (getCurrentState()) {
        case DEAD:
          NMS.sendTitle(getPlayer(), Titles.TitleType.BOTH, "§fVocê foi eliminado!", "§c§lDERROTA", 10, 60, 0);
          getPlayer().setMaxHealth(20.0D);
          getPlayer().setHealth(20.0D);
          getPlayer().setNoDamageTicks(20 * 5);

          break;
        case SPECTATING:
          if (getArena() != null) {
            getArena().getArenaPlayers().stream().filter(Objects::nonNull).map(a -> (ArenaPlayer) a).forEach(ap -> {
              Player players = ap.getPlayer();
              if (players != null) {
                getPlayer().showPlayer(players);
                if (ap.getCurrentState().isInGame()) {
                  players.hidePlayer(getPlayer());
                } else {
                  players.showPlayer(getPlayer());
                }
              }
            });
          }
          if (player.getScoreboard() != null) {
            org.bukkit.scoreboard.Team team = player.getScoreboard().getPlayerTeam(getPlayer());
            if (team != null) {
              team.removePlayer(getPlayer());
            }
            if (!Core.team.hasPlayer(getPlayer())) {
              Core.team.addPlayer(getPlayer());
            }

          }

          getPlayer().setHealth(20.0D);
          getPlayer().setGameMode(GameMode.ADVENTURE);
          getPlayer().setAllowFlight(true);

          getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0));

          getPlayer().getInventory().clear();
          getPlayer().getInventory().setArmorContents(new ItemStack[4]);

          getPlayer().getInventory().setItem(7, new ItemBuilder(Material.PAPER).name("§bJogar Novamente").lore("§7Clique para se conectar a outra sala.").build());

          getPlayer().getInventory().setItem(8, new ItemBuilder(Material.BED).name("§cVoltar ao Lobby").lore("§7Clique para voltar ao Lobby.").build());

          getPlayer().updateInventory();
          break;
        case RESPAWNING:
          getPlayer().getInventory().clear();
          getPlayer().getInventory().setArmorContents(new ItemStack[4]);

          com.uzm.hylex.bedwars.nms.NMS.sendFakeSpectator(getPlayer());
          new BukkitRunnable() {
            int count = 5;

            @Override
            public void run() {
              if (getPlayer() == null) {
                cancel();
                return;
              }

              if (count == 0) {
                cancel();
                if (getPlayer() != null && getPlayer().isOnline()) {
                  setCurrentState(CurrentState.IN_GAME);
                  update();
                  NMS.sendTitle(player, Titles.TitleType.BOTH, "", "", 0, 0, 0);
                  getPlayer().teleport(getTeam().getSpawnLocation());
                  getPlayer().sendMessage("§eVocê renasceu!");

                  getArena().getArenaPlayers().stream().map(a -> ((ArenaPlayer) a).getPlayer()).forEach(players -> players.showPlayer(player));
                }
                return;
              }

              if (getPlayer().isOnline()) {
                NMS.sendTitle(player, Titles.TitleType.BOTH, "§fRespawnando em " + count + " segundo" + (count > 1 ? "s" : "") + "!", "§c§lVOCÊ MORREU", 0, 20, 0);
              }

              count--;
            }
          }.runTaskTimer(Core.getInstance(), 0, 20);
          break;
        case IN_GAME:
          if (getPlayer() != null) {
            getPlayer().setGameMode(GameMode.SURVIVAL);
            equip();
            refresh();
            getPlayer().setNoDamageTicks(60);
          }
          break;
      }
    }

  }

  public void rewardSumary() {
    if (getPlayer() != null) {
      HylexPlayer hp = HylexPlayer.getByPlayer(getPlayer());
      long level = HylexPlayerController.getLevel(hp);
      long exp = HylexPlayerController.getExp(hp);
      getPlayer().sendMessage("§a§m--------------------------------------------------");
      getPlayer().sendMessage(StringUtils.center("§f§nSumário de ganhos§r", 60));
      getPlayer().sendMessage("");
      getPlayer().sendMessage(" §e* §7Você recebeu ");
      getPlayer().sendMessage("  §f• §6" + StringUtils.formatNumber((hp.getBedWarsStatistics().getLong("coins", "global") - getCoinsEarned())) + " Bedwars Coins");
      getPlayer().sendMessage("");
      getPlayer().sendMessage(StringUtils.center("§BExperiência do BedWars", 60));
      getPlayer().sendMessage("      §bNível " + level + "                                            §bNível " + (level + 1));
      getPlayer().sendMessage(StringUtils.center(StringUtils.progressDataBar(exp, 5000, 34), 55).replace("&", "§"));
      getPlayer().sendMessage(StringUtils.center("§b" + StringUtils.formatNumber(exp) + " §7/ §a5000 §7(" + StringUtils.formatNumber(((float) exp / 5000.0F) * 100.0F) + "%)", 70));
      getPlayer().sendMessage("    ");
      getPlayer().sendMessage("§7Você ganhou §b" + StringUtils.formatNumber((hp.getBedWarsStatistics().getLong("exp", "global") - getExpEarned())) + " de experiência do BedWars");
      getPlayer().sendMessage("    ");
      getPlayer().sendMessage("§a§m--------------------------------------------------");
    }
  }

  public void setStartedTime(long startedTime) {
    this.startedTime = startedTime;
  }

  public long getStartedTime() {
    return startedTime;
  }

  public void equip() {
    this.equipment.refresh();
  }

  public void refresh() {
    if (this.team.hasUpgrade(MANIAC_MINER)) {
      getPlayer().removePotionEffect(PotionEffectType.FAST_DIGGING);
      getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, Integer.MAX_VALUE, this.team.getTier(MANIAC_MINER) - 1));
    }

    if (this.team.hasUpgrade(SHARPENED_SWORDS)) {
      for (int i = 0; i < getPlayer().getInventory().getSize(); i++) {
        ItemStack item = getPlayer().getInventory().getItem(i);
        if (item != null && (item.getType().name().contains("_SWORD") || item.getType().name().contains("_AXE"))) {
          if (item.containsEnchantment(Enchantment.DAMAGE_ALL)) {
            item.removeEnchantment(Enchantment.DAMAGE_ALL);
          }

          item.addEnchantment(Enchantment.DAMAGE_ALL, this.team.getTier(SHARPENED_SWORDS));
          getPlayer().getInventory().setItem(i, item);
          getPlayer().updateInventory();
        }
      }
    }

    if (this.team.hasUpgrade(REINFORCED_ARMOR)) {
      ItemStack[] items = getPlayer().getInventory().getArmorContents();
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

  public void setCoinsEarned(int coinsEarned) {
    this.coinsEarned = coinsEarned;
  }

  public void setExpEarned(int expEarned) {
    this.expEarned = expEarned;
  }

  public int getCoinsEarned() {
    return coinsEarned;
  }

  public int getExpEarned() {
    return expEarned;
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
