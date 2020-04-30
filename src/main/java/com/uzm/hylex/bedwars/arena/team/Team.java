package com.uzm.hylex.bedwars.arena.team;

import com.uzm.hylex.bedwars.arena.improvements.Trap;
import com.uzm.hylex.bedwars.arena.improvements.UpgradeType;
import com.uzm.hylex.bedwars.arena.player.ArenaPlayer;
import com.uzm.hylex.bedwars.controllers.HylexPlayer;
import com.uzm.hylex.bedwars.utils.BukkitUtils;
import com.uzm.hylex.bedwars.utils.CubeId;
import com.uzm.hylex.bedwars.utils.Utils;
import com.uzm.hylex.core.libraries.holograms.HologramLibrary;
import com.uzm.hylex.core.libraries.holograms.api.Hologram;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class Team {

  private List<ArenaPlayer> members = new ArrayList<>();
  private List<ArenaPlayer> alive = new ArrayList<>();
  private Location spawnLocation;
  private Location upgradeLocation;
  private Location shopLocation;
  private Location bedLocation;
  private Sitation sitation;
  private Teams teamType;

  private CubeId border;

  private Hologram hologramShop;
  private Hologram hologramUpgrades;

  private List<Location> teamGenerators = new ArrayList<>();
  private List<Trap> traps = new ArrayList<>();
  private Map<UpgradeType, Integer> upgrades = new HashMap<>();

  private double iron = 1.5D;
  private double gold = 6.0D;
  private double emerald = 0.7;

  private Player lastTrapped;
  private long lastTrappedTime;


  public enum Sitation {
    WAITING,
    STADING,
    BROKEN_BED,
    ELIMINATED
  }

  public Team(Teams teamType) {
    this.teamType = teamType;
    setSitation(Sitation.WAITING);
  }

  public void setLastTrapped(Player player) {
    this.lastTrapped = player;
    this.lastTrappedTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(90);
  }

  public void enableHolograms() {
    this.hologramUpgrades = HologramLibrary.createHologram(getUpgradeLocation().add(0, 0.5, 0), true, "§e§lLOJA DE", "§e§lMELHORIAS");
    this.hologramShop = HologramLibrary.createHologram(getUpgradeLocation().add(0, 0.5, 0), true, "§e§lLOJA DE", "§e§lITENS");
  }

  public void disableHolograms() {
    if (this.hologramShop != null) {
      HologramLibrary.removeHologram(this.hologramShop);
      this.hologramShop = null;
    }
    if (this.hologramUpgrades != null) {
      HologramLibrary.removeHologram(this.hologramUpgrades);
      this.hologramUpgrades = null;
    }
  }

  public void resetTeam() {
    this.lastTrapped = null;
    this.traps.clear();
    this.upgrades.clear();
    this.members.clear();
    this.alive.clear();
    setSitation(Sitation.WAITING);
    disableHolograms();
  }

  public void equip() {
    this.alive.forEach(ArenaPlayer::equip);
  }

  public void refreshPlayers() {
    this.alive.forEach(ArenaPlayer::refresh);
  }

  public void evolve(UpgradeType upgrade) {
    this.upgrades.put(upgrade, this.getTier(upgrade));
  }

  public void setSpawnLocation(Location location) {
    this.spawnLocation = location;
  }

  public Location getSpawnLocation() {
    return spawnLocation;
  }

  public void setUpgradeLocation(Location location) {
    this.upgradeLocation = location;
  }

  public Location getUpgradeLocation() {
    return upgradeLocation;
  }

  public void setShopLocation(Location location) {
    this.shopLocation = location;
  }

  public void setBorder(CubeId border) {
    this.border = border;
  }

  public Location getShopLocation() {
    return shopLocation;
  }

  public List<ArenaPlayer> getMembers() {
    return this.members;
  }

  public List<ArenaPlayer> getAlive() {
    return alive;
  }

  public Sitation getSitation() {
    return sitation;
  }

  public void setSitation(Sitation sitation) {
    this.sitation = sitation;
  }

  public int getTier(UpgradeType upgrade) {
    return this.upgrades.getOrDefault(upgrade, 0);
  }

  public boolean hasUpgrade(UpgradeType upgrade) {
    return this.getTier(upgrade) != 0;
  }

  public List<Location> getTeamGenerators() {
    return this.teamGenerators;
  }

  public void setTeamGenerators(List<Location> teamGenerators) {
    this.teamGenerators = teamGenerators;
  }

  public Teams getTeamType() {
    return teamType;
  }

  public Location getBedLocation() {
    return bedLocation;
  }

  public Player getLastTrapped() {
    if (this.lastTrappedTime < System.currentTimeMillis()) {
      this.lastTrapped = null;
    }
    return this.lastTrapped;
  }

  public void setBedLocation(Location bedLocation) {
    this.bedLocation = bedLocation;
  }

  public boolean isBed(Block breakBlock) {
    Block teamBed = this.bedLocation.getBlock();
    Block neighbor = BukkitUtils.getBedNeighbor(teamBed);
    return neighbor.equals(breakBlock) || teamBed.equals(breakBlock);
  }

  public List<Trap> getTraps() {
    return this.traps;
  }

  public CubeId getBorder() {
    return this.border;
  }

  public void addMember(ArenaPlayer player, boolean notify) {
    if (notify) {
      this.members.stream().map(ArenaPlayer::getPlayer).forEach(
        result -> result.sendMessage("§7[§a!§7] " + HylexPlayer.Group.getColored(player.getPlayer()) + " §7entrou no seu time. (" + getTeamType().getDisplayName() + "§7)"));
    }
    if (!this.members.contains(player)) {
      this.members.add(player);
    }
  }

  public boolean isTotallyConfigured() {
    return spawnLocation != null && shopLocation != null && bedLocation != null && teamGenerators.size() > 0 && border != null;
  }

  public void addTrap(Trap trap) {
    this.traps.add(trap);
  }

  public void removeTrap(Trap trap) {
    this.traps.remove(trap);
  }

  public void addAlives() {
    this.alive.addAll(this.members);
  }

  private boolean tick;

  public void tickGenerator() {
    this.tickGenerator(true);
  }

  private void tickGenerator(boolean upgrade) {
    if (!getAlive().isEmpty()) {
      return;
    }

    if (this.iron == 0.0D) {
      this.iron = 1.5D;
      for (Location locs : getTeamGenerators()) {
        if (Utils.getAmountOfItem(Material.IRON_INGOT, locs) >= 64) {
          break;
        }
        Item item = locs.getWorld().dropItem(locs, new ItemStack(Material.IRON_INGOT));
        item.setPickupDelay(0);
        item.setVelocity(new Vector());
      }
    } else {
      iron -= 0.5;
    }

    if (gold == 0.0D) {
      gold = 6.0D;
      for (Location locs : getTeamGenerators()) {
        if (Utils.getAmountOfItem(Material.IRON_INGOT, locs) >= 48) {
          break;
        }
        Item item = locs.getWorld().dropItem(locs, new ItemStack(Material.GOLD_INGOT));
        item.setPickupDelay(0);
        item.setVelocity(new Vector());
      }
    } else {
      gold -= 0.5;
    }

    int level = getTier(UpgradeType.IRON_FORGE);
    if (level > 2) {
      if (emerald == 0.0D) {
        emerald = 15.0D;
        for (Location locs : getTeamGenerators()) {
          if (Utils.getAmountOfItem(Material.IRON_INGOT, locs) >= 32) {
            break;
          }
          Item item = locs.getWorld().dropItem(locs, new ItemStack(Material.EMERALD));
          item.setPickupDelay(0);
          item.setVelocity(new Vector());
        }
      } else {
        emerald -= 0.5;
      }
    }

    if (upgrade && level > 0) {
      if (level == 1) {
        // 50% uma vez sim outra não
        this.tick = !this.tick;
        if (this.tick) {
          this.tickGenerator(false);
        }
      } else if (level == 2 || level == 3) {
        // 100%
        this.tickGenerator(false);
      } else if (level == 4) {
        // 200%
        this.tickGenerator(false);
        this.tickGenerator(false);
      }
    }
  }
}
