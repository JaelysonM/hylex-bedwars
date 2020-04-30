package com.uzm.hylex.bedwars.arena.creator.inventory;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
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

public class WorldsMenu extends PageablePlayerInventory {

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

  public WorldsMenu(Player viewer) {
    super(viewer, Bukkit.createInventory(null, 45, "§7Primeiro selecione um mundo"), "§7Primeiro selecione um mundo");
    HylexPlayer hp = HylexPlayer.get(getPlayer());
    if (hp != null && hp.getAbstractArena() != null) {
      config((new int[] {12, 13, 14, 21, 22, 23}));
      List<ItemStack> items = Lists.newArrayList();
      for (World world : viewer.getServer().getWorlds()) {
        if (!world.getName().toLowerCase().contains("world")) {
          if (!ArenaController.hasArena(world.getName())) {
            items.add(BukkitUtils.putProfileOnSkull(WORLD, new ItemBuilder(Material.SKULL_ITEM).durability(3).name("§bMundo §7" + world.getName())
              .lore("", "§eInformações:", "", " §a↪ §7Número de entidades: " + world.getEntities().size(),
                " §b↪ §7Tamanho: " + new FilesSize(world.getWorldFolder()).getSizeFormat(), " §e↪ §7UID: " + world.getUID(), "", "§bClique para escolher esse mapa.").build()));

          }
        }
      }

      fill(items.toArray(new ItemStack[] {}),
        new Object[][] {{19, new ItemBuilder(Material.ARROW).name("§ePágina anterior").build()}, {25, new ItemBuilder(Material.ARROW).name("§ePágina posterior").build()}},
        new Object[][] {{BukkitUtils.putProfileOnSkull(SELECT, new ItemBuilder(Material.SKULL_ITEM).durability(3).name("§aSeleção de um mundo")
          .lore("", "§7Escolha um mundo para ser criado", "§7na §amini-arena " + hp.getAbstractArena().getArenaName() + "§7.", "", " §e* Foram encontrados: §f" + items.size())
          .build()), 4},


          {new ItemBuilder(Material.STAINED_CLAY).name("§cCancelar criação").durability(14)
            .lore("", "§7Você ainda não selecionou um mundo.", "", " §cClique aqui para cancelar essa operação.").build(), 40}}, new Object[] {22,
          BukkitUtils.putProfileOnSkull(NOT_FOUND,
            new ItemBuilder(Material.SKULL_ITEM).durability(3).lore("§cNão encontrado").lore("§7Não encontramos nenhum", "§7mundo possível para criar uma mini-arena.").build())});
      open(getPlayer(), 1);
    }


  }

  public void click(Inventory inv, ItemStack item, int slot) {
    if (item.getType() == Material.SKULL_ITEM && item.getItemMeta().getDisplayName().startsWith("§9Página anterior")) {
      open(getPlayer(), getCurrent() - 1);
    } else if (item.getType() == Material.SKULL_ITEM && item.getItemMeta().getDisplayName().startsWith("§9Próxima página")) {
      open(getPlayer(), getCurrent() + 1);
    } else if (item.hasItemMeta()) {
      if (item.getItemMeta().hasDisplayName()) {
        if (item.getItemMeta().getDisplayName().equalsIgnoreCase("§aProsseguir com configuração")) {
          HylexPlayer hp = HylexPlayer.get(getPlayer());
          if (hp != null && hp.getAbstractArena() != null) {
            if (hp.getPlayer().getWorld().getName().equals(hp.getAbstractArena().getWorldName())) {
              new MainPainel(getPlayer(), hp.getAbstractArena());
            } else {
              hp.setAux(true);
              getPlayer().teleport(Bukkit.getWorld(hp.getAbstractArena().getWorldName()).getSpawnLocation());
              getPlayer().setAllowFlight(true);
              getPlayer().setFlying(true);
            }
          }
        } else if (item.getItemMeta().getDisplayName().equalsIgnoreCase("§cCancelar criação")) {
          getPlayer().closeInventory();
          HylexPlayer hp = HylexPlayer.get(getPlayer());
          if (hp != null && hp.getAbstractArena() != null) {
            getPlayer().sendMessage("§c* Você cancelou a criação da arena " + hp.getAbstractArena() + "§.");
            getPlayer().playSound(getPlayer().getLocation(), Sound.CREEPER_HISS, 1.5F, 1.5F);
            hp.setAbstractArena(null);

          }
        } else if (item.getItemMeta().getDisplayName().startsWith("§bMundo §7") && inv.getItem(40).getItemMeta().getDisplayName().equalsIgnoreCase("§cCancelar criação")) {
          HylexPlayer hp = HylexPlayer.get(getPlayer());
          if (hp != null && hp.getAbstractArena() != null) {
            getPlayer().playSound(getPlayer().getLocation(), Sound.ORB_PICKUP, 2.0F, 2.0F);
            String worldName = item.getItemMeta().getDisplayName().split("§bMundo §7")[1];
            hp.getAbstractArena().setWorldName(worldName);
            inv.setItem(slot, new ItemBuilder(item.clone()).name("§aMundo §7" + worldName).updateLoreLine(7, "§aMundo selecionado").build(true));
            getPages().forEach(invs -> invs.setItem(40, new ItemBuilder(Material.STAINED_CLAY).durability(5).name("§aProsseguir com configuração")
              .lore("", "§7Você selecionou o mundo: §a" + item.getItemMeta().getDisplayName().split("§bMundo §7")[1], "", " §bClique aqui para continuar a operação.").build()));

          }
        }
      }
    }

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
