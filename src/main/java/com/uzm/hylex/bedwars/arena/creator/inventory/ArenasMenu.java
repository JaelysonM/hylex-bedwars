package com.uzm.hylex.bedwars.arena.creator.inventory;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.uzm.hylex.bedwars.arena.Arena;
import com.uzm.hylex.bedwars.controllers.ArenaController;
import com.uzm.hylex.core.api.HylexPlayer;
import com.uzm.hylex.core.spigot.inventories.PageablePlayerInventory;
import com.uzm.hylex.core.spigot.items.ItemBuilder;
import com.uzm.hylex.core.spigot.utils.BukkitUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
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
    super(viewer, Bukkit.createInventory(null, 45, "§7Arenas do " + com.uzm.hylex.core.Core.SOCKET_NAME.replace("bedwars-", "")),
      "§7Arenas do " + com.uzm.hylex.core.Core.SOCKET_NAME.replace("bedwars-", ""));

    config(new int[] {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25});

    List<ItemStack> items = Lists.newArrayList();

    for (Arena arenas : ArenaController.getArenas().values()) {
      String value = "";
      switch (arenas.getState()) {
        case IN_GAME:
          value =
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzY0MTY4MmY0MzYwNmM1YzlhZDI2YmM3ZWE4YTMwZWU0NzU0N2M5ZGZkM2M2Y2RhNDllMWMxYTI4MTZjZjBiYSJ9fX0=";
          break;
        case END:
          value =
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWZkZTNiZmNlMmQ4Y2I3MjRkZTg1NTZlNWVjMjFiN2YxNWY1ODQ2ODRhYjc4NTIxNGFkZDE2NGJlNzYyNGIifX19=";
          break;
        case PREPARE:
          value =
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmM0ODg2ZWYzNjJiMmM4MjNhNmFhNjUyNDFjNWM3ZGU3MWM5NGQ4ZWM1ODIyYzUxZTk2OTc2NjQxZjUzZWEzNSJ9fX0==";
          break;
        case IN_WAITING:
          value =
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjhmZmYyMmM2ZTY1NDZkMGQ4ZWI3Zjk3NjMzOTg0MDdkZDJhYjgwZjc0ZmUzZDE2YjEwYTk4M2VjYWYzNDdlIn19fQ====";
          break;
        case STARTING:
          value =
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjJkMTQ1YzkzZTVlYWM0OGE2NjFjNmYyN2ZkYWZmNTkyMmNmNDMzZGQ2MjdiZjIzZWVjMzc4Yjk5NTYxOTcifX19====";
          break;
        default:
          value = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7ImlkIjoiMTkwM2NhNWE3MjgzNDExODk5NjMwYTY5OTM3MTY3NmMiLCJ0eXBlIjoiU0tJTiIsInVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmM5MTVkYjNmYzQwYTc5YjYzYzJjNDUzZjBjNDkwOTgxZTUyMjdjNTAyNzUwMTI4MzI3MjEzODUzM2RlYTUxOSIsInByb2ZpbGVJZCI6IjgwMThhYjAwYjJhZTQ0Y2FhYzliZjYwZWY5MGY0NWU1IiwidGV4dHVyZUlkIjoiMmM5MTVkYjNmYzQwYTc5YjYzYzJjNDUzZjBjNDkwOTgxZTUyMjdjNTAyNzUwMTI4MzI3MjEzODUzM2RlYTUxOSJ9fSwic2tpbiI6eyJpZCI6IjE5MDNjYTVhNzI4MzQxMTg5OTYzMGE2OTkzNzE2NzZjIiwidHlwZSI6IlNLSU4iLCJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzJjOTE1ZGIzZmM0MGE3OWI2M2MyYzQ1M2YwYzQ5MDk4MWU1MjI3YzUwMjc1MDEyODMyNzIxMzg1MzNkZWE1MTkiLCJwcm9maWxlSWQiOiI4MDE4YWIwMGIyYWU0NGNhYWM5YmY2MGVmOTBmNDVlNSIsInRleHR1cmVJZCI6IjJjOTE1ZGIzZmM0MGE3OWI2M2MyYzQ1M2YwYzQ5MDk4MWU1MjI3YzUwMjc1MDEyODMyNzIxMzg1MzNkZWE1MTkifSwiY2FwZSI6bnVsbH0=";
          break;
      }
      ItemStack stack = BukkitUtils.putProfileOnSkull(value, new ItemBuilder(Material.SKULL_ITEM).durability(3).name("§e" + arenas.getArenaName())
        .lore("", " §eInformações da arena:", "", "  §a★ §7Estado atual: §e" + arenas.getState().toString(),
          "  §e★ §7Estado de evento atual: §e" + arenas.getUpgradeState().toString(),
          "  §e● §7Jogadores: §a" + arenas.getArenaPlayers().size() + "/" + arenas.getConfiguration().getMaxPlayers(),
          "  §e→ §7Número de ilhas: §a" + arenas.getConfiguration().getIslands(), "  §e→ §7Modo: §b" + arenas.getConfiguration().getMode(),
          "  §e→ §7Nome do mapa: §b" + arenas.getWorldName(), "", "§e* Clique esquerdo para entrar", "§e* Clique direito para configurar").build());
      items.add(stack);
      attachObject(stack, arenas.getArenaName());
    }

    fill(items.toArray(new ItemStack[] {}), new Object[][] {{42, BukkitUtils.putProfileOnSkull(
      "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzE2MmQ0MmY0ZGJhMzU0ODhmNGY2NmQ2NzM2MzViZmM1NjE5YmRkNTEzZDAyYjRjYzc0ZjA1ZWM4ZTk1NiJ9fX0=",
      new ItemBuilder(Material.SKULL_ITEM).name("§9Página anterior").durability(3).build())}, {43, BukkitUtils.putProfileOnSkull(
      "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjgxMzYzM2JkNjAxNTJkOWRmNTRiM2Q5ZDU3M2E4YmMzNjU0OGI3MmRjMWEzMGZiNGNiOWVjMjU2ZDY4YWUifX19",
      new ItemBuilder(Material.SKULL_ITEM).name("§9Próxima página").durability(3).build())}}, new Object[][] {},
      new Object[] {22, new ItemBuilder(Material.STAINED_GLASS_PANE).durability(14).name("§cNão encontramos nenhuma arena!").build()});

    this.open(viewer, 1);
  }

  public void click(InventoryClickEvent evt) {
    ItemStack item = evt.getCurrentItem();
    if (item.getType() == Material.SKULL_ITEM && item.getItemMeta().getDisplayName().startsWith("§ePágina anterior")) {
      open(getPlayer(), getCurrent() - 1);
    } else if (item.getType() == Material.SKULL_ITEM && item.getItemMeta().getDisplayName().startsWith("§ePágina posterior")) {
      open(getPlayer(), getCurrent() + 1);
    } else {
      HylexPlayer hp = HylexPlayer.getByPlayer(getPlayer());
      if (hp != null) {
        if (item.getType() == Material.SKULL_ITEM && item.getItemMeta().getDisplayName().startsWith("§e")) {
          if (getAttached(item) != null) {
            if (evt.isLeftClick()) {
              evt.getWhoClicked().closeInventory();
              Arena arena = ArenaController.getArena((String) getAttached(item));
              if (hp.getArenaPlayer() != null && arena.equals(hp.getArenaPlayer().getArena())) {
                hp.getPlayer().sendMessage("§cVocê já esta nesse arena.");
                return;
              }

              if (hp.getArenaPlayer() != null) {
                hp.getArenaPlayer().getArena().leave(hp);
              }

              hp.getPlayer().sendMessage("§8Sendo enviado para " + arena.getArenaName() + "...");
              arena.join(hp);
            } else if (evt.isRightClick()) {
              Arena arena = ArenaController.getArena((String) getAttached(item));
              new MainPainel(getPlayer(), arena);
            }
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
        click(evt);
      }
    }
  }
}
