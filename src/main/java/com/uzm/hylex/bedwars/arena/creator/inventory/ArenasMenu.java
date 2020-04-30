package com.uzm.hylex.bedwars.arena.creator.inventory;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.uzm.hylex.bedwars.Core;
import com.uzm.hylex.bedwars.controllers.ArenaController;
import com.uzm.hylex.bedwars.controllers.HylexPlayer;
import com.uzm.hylex.bedwars.utils.BukkitUtils;
import com.uzm.hylex.core.java.util.FilesSize;
import com.uzm.hylex.core.spigot.inventories.PageablePlayerInventory;
import com.uzm.hylex.core.spigot.items.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public class ArenasMenu extends PageablePlayerInventory {

  private static final GameProfile WORLD, SELECT, NOT_FOUND;

  static {
    WORLD = new GameProfile(UUID.randomUUID(), null);
    WORLD.getProperties().put("textures", new Property("textures",
      "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjFkZDRmZTRhNDI5YWJkNjY1ZGZkYjNlMjEzMjFkNmVmYTZhNmI1ZTdiOTU2ZGI5YzVkNTljOWVmYWIyNSJ9fX0="));
    SELECT = new GameProfile(UUID.randomUUID(), null);
    SELECT.getProperties().put("textures", new Property("textures",
      "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWI3Y2U2ODNkMDg2OGFhNDM3OGFlYjYwY2FhNWVhODA1OTZiY2ZmZGFiNmI1YWYyZDEyNTk1ODM3YTg0ODUzIn19fQ=="));
    NOT_FOUND = new GameProfile(UUID.randomUUID(), null);
    NOT_FOUND.getProperties().put("textures", new Property("textures",
      "ewogICJ0aW1lc3RhbXAiIDogMTU4ODIwNzE1NzIxNywKICAicHJvZmlsZUlkIiA6ICJkZTU3MWExMDJjYjg0ODgwOGZlN2M5ZjQ0OTZlY2RhZCIsCiAgInByb2ZpbGVOYW1lIiA6ICJNSEZfTWluZXNraW4iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjc2Y2IwMDU4ZGY0NDMwMDQ1NWUwMTA1ZjdjMDIzZmViOTJiNzA0OTBjMzQyNDhkYTQ0NmIwMWM4MWI3MmI0ZCIKICAgIH0KICB9Cn0"));
  }

  public ArenasMenu(Player viewer) {
    super(viewer, Bukkit.createInventory(null, 45, "§7Arenas do " + Core.getLoader().getServerName()), "§7Arenas do " + Core.getLoader().getServerName());
    // TODO Menu de arenas com stado e informações



  }

  public void click(Inventory inv, ItemStack item, int slot) {
    if (item.getType() == Material.SKULL_ITEM && item.getItemMeta().getDisplayName().startsWith("§9Página anterior")) {
      open(getPlayer(), getCurrent() - 1);
    } else if (item.getType() == Material.SKULL_ITEM && item.getItemMeta().getDisplayName().startsWith("§9Próxima página")) {
      open(getPlayer(), getCurrent() + 1);
    }

    // TODO Menu de arenas com stado e informações

  }


  @EventHandler
  public void onClick(InventoryClickEvent evt) {
    if (exists(evt.getInventory())) {
      evt.setCancelled(true);
      ItemStack item = evt.getCurrentItem();

      Player player = (Player) evt.getWhoClicked();
      player.updateInventory();
      if (item != null && item.hasItemMeta() && player.equals(getPlayer())) {
        click(evt.getInventory(), item, evt.getSlot());
      }
    }
  }
}
