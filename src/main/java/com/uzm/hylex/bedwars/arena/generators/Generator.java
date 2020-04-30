package com.uzm.hylex.bedwars.arena.generators;

import com.uzm.hylex.bedwars.arena.Arena;
import com.uzm.hylex.bedwars.nms.NMS;
import com.uzm.hylex.bedwars.nms.entity.EntityGenerator;
import com.uzm.hylex.bedwars.utils.Utils;
import com.uzm.hylex.core.libraries.holograms.HologramLibrary;
import com.uzm.hylex.core.libraries.holograms.api.Hologram;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import static com.uzm.hylex.bedwars.arena.generators.Generator.Type.DIAMOND;
import static com.uzm.hylex.bedwars.arena.generators.Generator.Type.EMERALD;

public class Generator {

  public enum Type {
    EMERALD,
    DIAMOND;

    public String getName() {
      if (this == EMERALD) {
        return "§2§lESMERALDA";
      }

      return "§b§lDIAMANTE";
    }

    public Material getItem() {
      return Material.matchMaterial(this.name());
    }

    public ItemStack getBlock() {
      return new ItemStack(Material.matchMaterial(this.name() + "_BLOCK"));
    }
  }


  private Type type;
  private int countdown;
  private Location location;

  private Arena arena;
  private EntityGenerator block;
  private Hologram hologram;

  public Generator(Arena arena, Type type, Location location) {
    this.arena = arena;
    this.type = type;
    this.location = location;
  }

  private int tick = 0;
  private boolean floatLoop = true;

  public void enable() {
    this.disable();
    this.block = NMS.createGeneratorEntity(this);
    this.hologram = HologramLibrary.createHologram(location, "§aSpawnando em §c" + this.countdown + " §asegundos", this.type.getName(), "§aNível " + StringUtils.repeat("I", 1));
  }

  public void disable() {
    this.countdown = type == DIAMOND ? arena.getUpgradeState().getDiamondDelay() : arena.getUpgradeState().getEmeraldDelay();
    if (this.block != null) {
      this.block.killEntity();
      this.block = null;
    }
    if (this.hologram != null) {
      HologramLibrary.removeHologram(this.hologram);
      this.hologram = null;
    }
  }

  public void tick() {
    ArmorStand armorStand = this.block.getEntity();
    Location location = armorStand.getLocation();
    if (!this.floatLoop) {
      location.add(0, 0.01, 0);
      location.setYaw((location.getYaw() + 7.5F));

      if (armorStand.getLocation().getY() > (0.25 + (this.location.getY() + 2.4))) {
        this.floatLoop = true;
      }
    } else {
      location.subtract(0, 0.01, 0);
      location.setYaw((location.getYaw() - 7.5F));

      if (armorStand.getLocation().getY() < (-0.25 + (this.location.getY() + 2.4))) {
        this.floatLoop = false;
      }
    }

    armorStand.teleport(location);

    if (this.tick == 20) {
      this.tick = 0;
      this.hologram.updateLine(1, "§aSpawnando em §c" + getDelay() + " §asegundos");
      this.hologram.updateLine(3, "§aNível " + StringUtils.repeat("I", getTier()));
      if (this.countdown == 0) {
        if (Utils.getAmountOfItem(this.type.getItem(), this.location) < 4) {
          Item item = this.location.getWorld().dropItem(this.location, new ItemStack(this.type.getItem()));
          item.setPickupDelay(0);
          item.setVelocity(new Vector());
        }
        this.countdown = type == DIAMOND ? arena.getUpgradeState().getDiamondDelay() : arena.getUpgradeState().getEmeraldDelay();
      }

      this.countdown--;
      return;
    }

    this.tick++;
  }


  public int getTier() {
    return getType() == EMERALD ? arena.getUpgradeState().getEmeraldLevel() : arena.getUpgradeState().getDiamondLevel();
  }

  public int getDelay() {
    return getType() == EMERALD ? arena.getUpgradeState().getEmeraldDelay() : arena.getUpgradeState().getDiamondDelay();
  }


  public Location getLocation() {
    return location;
  }

  public Type getType() {
    return type;
  }
}
