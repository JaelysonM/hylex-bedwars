package com.uzm.hylex.bedwars.arena.team;

import com.google.common.collect.ImmutableList;
import com.uzm.hylex.bedwars.Core;
import com.uzm.hylex.bedwars.arena.improvements.Trap;
import com.uzm.hylex.bedwars.arena.improvements.UpgradeType;
import com.uzm.hylex.bedwars.arena.player.ArenaPlayer;
import com.uzm.hylex.core.api.Group;
import com.uzm.hylex.core.api.HylexPlayer;
import com.uzm.hylex.core.libraries.holograms.HologramLibrary;
import com.uzm.hylex.core.libraries.holograms.api.Hologram;
import com.uzm.hylex.core.libraries.npclib.NPCLibrary;
import com.uzm.hylex.core.libraries.npclib.api.NPC;
import com.uzm.hylex.core.libraries.npclib.trait.LookClose;
import com.uzm.hylex.core.spigot.items.ItemStackUtils;
import com.uzm.hylex.core.spigot.utils.BukkitUtils;
import com.uzm.hylex.core.utils.CubeId;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.uzm.hylex.bedwars.arena.improvements.UpgradeType.HEAL_POOL;

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

  private NPC npcShop;
  private NPC npcUpgrades;
  private Hologram hologramShop;
  private Hologram hologramUpgrades;

  private List<Location> teamGenerators = new ArrayList<>();
  private List<Trap> traps = new ArrayList<>();
  private Map<UpgradeType, Integer> upgrades = new HashMap<>();

  private double iron = 1.0D;
  private double gold = 6.0D;
  private double emerald = 15.0D;

  private Player lastTrapped;
  private long lastTrappedTime;


  public enum Sitation {
    WAITING,
    STANDING,
    BROKEN_BED,
    ELIMINATED
  }

  public Team(Teams teamType) {
    this.teamType = teamType;
    setSitation(Sitation.WAITING);
  }

  private org.bukkit.scoreboard.Team team;

  public void registerTeam(String prefix) {
    this.team = Bukkit.getScoreboardManager().getMainScoreboard().getTeam(this.teamType.getOrder() + prefix);
    if (this.team == null) {
      this.team = Bukkit.getScoreboardManager().getMainScoreboard().registerNewTeam(this.teamType.getOrder() + prefix);
      this.team.setPrefix(this.teamType.getScoreboardName() + " ");
      this.team.setSuffix("");
    }

    this.members.forEach(ap -> {
      if (!this.team.hasPlayer(ap.getPlayer())) {
        this.team.addPlayer(ap.getPlayer());
      }
    });
  }

  public org.bukkit.scoreboard.Team getTeam() {
    return this.team;
  }

  public void breakBed() {
    this.setSitation(Sitation.BROKEN_BED);
    BukkitUtils.getBedNeighbor(this.bedLocation.getBlock()).breakNaturally(new ItemStack(Material.AIR));
    BukkitUtils.getBedNeighbor(this.bedLocation.clone().add(1,0,0).getBlock()).breakNaturally(new ItemStack(Material.AIR));
    BukkitUtils.getBedNeighbor(this.bedLocation.clone().add(0,0,1).getBlock()).breakNaturally(new ItemStack(Material.AIR));
    BukkitUtils.getBedNeighbor(this.bedLocation.clone().add(-1,0,0).getBlock()).breakNaturally(new ItemStack(Material.AIR));
    BukkitUtils.getBedNeighbor(this.bedLocation.clone().add(0,0,1).getBlock()).breakNaturally(new ItemStack(Material.AIR));
    this.bedLocation.getBlock().breakNaturally(new ItemStack(Material.AIR));
    this.alive.stream().filter(Objects::nonNull).filter(ap -> ap.getPlayer() !=null).forEach(ap -> {
      HylexPlayer hp = HylexPlayer.getByPlayer(ap.getPlayer());
      if (hp != null) {
        hp.getBedWarsStatistics().addLong("bedsLost", "global");
        hp.getBedWarsStatistics().addLong("bedsLost", ap.getArena().getConfiguration().getMode().toLowerCase());
      }
    });
  }

  public void setLastTrapped(Player player) {
    this.lastTrappedTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(90);
    this.lastTrapped = player;
  }

  public void disableGravity() {
    this.npcShop.data().set(NPC.GRAVITY, false);
    this.npcUpgrades.data().set(NPC.GRAVITY, false);
  }

  public void enableHolograms() {
    this.hologramShop = HologramLibrary.createHologram(getShopLocation().add(0, 0.5, 0), true, "§b§lCLIQUE DIREITO", "§e§lITENS", "§e§lLOJA DE");
    this.npcShop = NPCLibrary.createNPC(EntityType.PLAYER, UUID.randomUUID().toString().replace("-", ""));
    this.npcShop.data().set("SHOP", "item");
    this.npcShop.data().set(NPC.GRAVITY, true);
    this.npcShop.data().set(NPC.PROFILE_NPC_SKIN, true);
    this.npcShop.data().set(NPC.HIDE_BY_TEAMS_KEY, true);
    LookClose lookClose = new LookClose( this.npcShop);
    lookClose.lookClose(true);
    this.npcShop.addTrait(lookClose);

    this.npcShop.spawn(getShopLocation());

    this.hologramUpgrades = HologramLibrary.createHologram(getUpgradeLocation().add(0, 0.5, 0), true, "§b§lCLIQUE DIREITO", "§e§lMELHORIAS", "§e§lLOJA DE");
    this.npcUpgrades = NPCLibrary.createNPC(EntityType.PLAYER, UUID.randomUUID().toString().replace("-", ""));
    this.npcUpgrades.data().set("SHOP", "upgrade");
    this.npcUpgrades.data().set(NPC.GRAVITY, true);
    this.npcUpgrades.data().set(NPC.PROFILE_NPC_SKIN, true);
    this.npcUpgrades.data().set(NPC.HIDE_BY_TEAMS_KEY, true);
    this.npcUpgrades.spawn(getUpgradeLocation());
    LookClose lookClose2 = new LookClose( this.npcUpgrades);
    lookClose2.lookClose(true);
    this.npcUpgrades.addTrait(lookClose2);
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
    if (this.npcShop != null) {
      this.npcShop.destroy();
      this.npcShop = null;
    }
    if (this.npcUpgrades != null) {
      this.npcUpgrades.destroy();
      this.npcUpgrades = null;
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

  public void evolve(UpgradeType upgrade) {
    this.upgrades.put(upgrade, this.getTier(upgrade) + 1);
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
    return this.alive;
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
    if (this.sitation == Sitation.BROKEN_BED) {
      return false;
    }

    Block teamBed = this.bedLocation.getBlock();
    Block neighbor = BukkitUtils.getBedNeighbor(teamBed);
    return neighbor.equals(breakBlock) || teamBed.equals(breakBlock);
  }

  public List<Trap> getTraps() {
    return ImmutableList.copyOf(this.traps);
  }

  public CubeId getBorder() {
    return this.border;
  }

  public void addMember(ArenaPlayer player, boolean notify) {
    if (notify) {
      this.members.stream().map(ArenaPlayer::getPlayer)
        .forEach(result -> result.sendMessage("§7[§a!§7] " + Group.getColored(player.getPlayer()) + " §7entrou no seu time. (" + getTeamType().getDisplayName() + "§7)"));
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

  public void tick() {
    if (this.hasUpgrade(HEAL_POOL)) {
      Bukkit.getScheduler().runTaskAsynchronously(Core.getInstance(), () -> {
        for (int i = 0; i < 300; i++) {
          Location l = getBorder().getRandomLocation();
          l.getWorld().spigot().playEffect(l, Effect.HAPPY_VILLAGER);
        }
      });

      this.alive.forEach(ap -> {
        Player player = ap.getPlayer();
        if (getBorder().contains(player.getLocation())) {
          if (!player.hasPotionEffect(PotionEffectType.REGENERATION)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 0));
          }
        } else if (player.hasPotionEffect(PotionEffectType.REGENERATION)) {
          player.removePotionEffect(PotionEffectType.REGENERATION);
        }
      });
    }

    this.tickGenerator(true);
  }

  private void tickGenerator(boolean upgrade) {
    if (this.iron == 0.0D) {
      this.iron = 1.5D;
      for (Location locs : getTeamGenerators()) {
        if (ItemStackUtils.getAmountOfItem(Material.IRON_INGOT, locs) >= 64) {
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
      gold = 6.5D;
      for (Location locs : getTeamGenerators()) {
        if (ItemStackUtils.getAmountOfItem(Material.GOLD_INGOT, locs) >= 48) {
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
        emerald = 30.0D;
        for (Location locs : getTeamGenerators()) {
          if (ItemStackUtils.getAmountOfItem(Material.EMERALD, locs) >= 32) {
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
