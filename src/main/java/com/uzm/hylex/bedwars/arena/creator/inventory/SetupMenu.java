package com.uzm.hylex.bedwars.arena.creator.inventory;

import com.uzm.hylex.bedwars.arena.Arena;
import com.uzm.hylex.core.Core;
import com.uzm.hylex.core.spigot.inventories.PlayerMenu;
import com.uzm.hylex.core.spigot.items.ItemBuilder;
import com.uzm.hylex.core.spigot.utils.BukkitUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class SetupMenu extends PlayerMenu {

  @EventHandler
  public void onInventoryClick(InventoryClickEvent evt) {
    if (evt.getInventory().equals(this.getInventory())) {
      evt.setCancelled(true);

      if (evt.getWhoClicked().equals(this.player)) {
        ItemStack item = evt.getCurrentItem();
        if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
          Arena arena = (Arena) getAttached(0);
          if (evt.getSlot() == 0) {
            new MainPainel(getPlayer(), arena);
            return;
          }
          String display = item.getItemMeta().getDisplayName();
          if (display.equalsIgnoreCase("§e+1")) {
            if (getAttached(evt.getSlot()) != null) {
              ItemStack itemStack = evt.getInventory().getItem((int) getAttached(evt.getSlot()));
              int amount = itemStack.getAmount();
              if (itemStack.getItemMeta().getDisplayName().equalsIgnoreCase("§aQuantidade de ilhas")) {
                if (amount + 1 <= 8) {
                  evt.getInventory().setItem((int) getAttached(evt.getSlot()), new ItemBuilder(evt.getInventory().getItem((int) getAttached(evt.getSlot())).clone()).amount(amount + 1).build());
                  arena.getConfiguration().setIslands(amount + 1);
                }
              } else if (itemStack.getItemMeta().getDisplayName().equalsIgnoreCase("§bTamanho dos times")) {
                evt.getInventory().setItem((int) getAttached(evt.getSlot()), new ItemBuilder(evt.getInventory().getItem((int) getAttached(evt.getSlot())).clone()).amount(amount + 1).build());
                ((Arena) getAttached(0)).getConfiguration().setTeamsSize(amount + 1);
              } else if (itemStack.getItemMeta().getDisplayName().equalsIgnoreCase("§aQuantidade mínima para iniciar")) {
                if (amount + 1 <= (arena.getConfiguration().getMaxPlayers())) {
                  evt.getInventory().setItem((int) getAttached(evt.getSlot()), new ItemBuilder(evt.getInventory().getItem((int) getAttached(evt.getSlot())).clone()).amount(amount + 1).build());
                  arena.getConfiguration().setMinPlayers(amount + 1);
                }
              } else if (itemStack.getItemMeta().getDisplayName().equalsIgnoreCase("§cQuantidade máxima de jogadores")) {
                if (amount + 1 <= (arena.getConfiguration().getIslands() * arena.getConfiguration().getTeamsSize())) {
                  evt.getInventory().setItem((int) getAttached(evt.getSlot()), new ItemBuilder(evt.getInventory().getItem((int) getAttached(evt.getSlot())).clone()).amount(amount + 1).build());
                  arena.getConfiguration().setMaxPlayers(amount + 1);
                }
              }
            }
          } else if (display.equalsIgnoreCase("§e-1")) {
            if (getAttached(evt.getSlot()) != null) {
              ItemStack itemStack = evt.getInventory().getItem((int) getAttached(evt.getSlot()));
              int amount = itemStack.getAmount();
              if (itemStack.getItemMeta().getDisplayName().equalsIgnoreCase("§aQuantidade de ilhas")) {
                if (amount - 1 >= 1) {
                  evt.getInventory().setItem((int) getAttached(evt.getSlot()), new ItemBuilder(evt.getInventory().getItem((int) getAttached(evt.getSlot())).clone()).amount(amount - 1).build());
                  arena.getConfiguration().setIslands(amount - 1);
                }
              } else if (itemStack.getItemMeta().getDisplayName().equalsIgnoreCase("§bTamanho dos times")) {
                if (amount - 1 >= 1) {
                  evt.getInventory().setItem((int) getAttached(evt.getSlot()), new ItemBuilder(evt.getInventory().getItem((int) getAttached(evt.getSlot())).clone()).amount(amount - 1).build());
                  arena.getConfiguration().setTeamsSize(amount - 1);
                }
              } else if (itemStack.getItemMeta().getDisplayName().equalsIgnoreCase("§aQuantidade mínima para iniciar")) {
                if (amount - 1 >= 1) {
                  evt.getInventory().setItem((int) getAttached(evt.getSlot()), new ItemBuilder(evt.getInventory().getItem((int) getAttached(evt.getSlot())).clone()).amount(amount - 1).build());
                  arena.getConfiguration().setMinPlayers(amount - 1);
                }
              } else if (itemStack.getItemMeta().getDisplayName().equalsIgnoreCase("§cQuantidade máxima de jogadores")) {
                if (amount - 1 >= 1) {
                  evt.getInventory().setItem((int) getAttached(evt.getSlot()), new ItemBuilder(evt.getInventory().getItem((int) getAttached(evt.getSlot())).clone()).amount(amount - 1).build());
                  arena.getConfiguration().setMaxPlayers(amount - 1);
                }
              }
            }
          }
        }
      }
    }
  }


  public SetupMenu(Player player, Arena arena) {
    super(player, "§7Configuração " + arena.getArenaName(), 5);

    setItem(0, BukkitUtils.putProfileOnSkull(
      "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmQ2OWUwNmU1ZGFkZmQ4NGU1ZjNkMWMyMTA2M2YyNTUzYjJmYTk0NWVlMWQ0ZDcxNTJmZGM1NDI1YmMxMmE5In19fQ==",
      new ItemBuilder(Material.SKULL_ITEM).durability(3).name("§aVoltar para o painel inicial").build()));
    attachObject(0, arena);
    setItem(10, BukkitUtils.putProfileOnSkull(
      "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWQyNGRmYWYxZWUxN2Q3M2VlZWMyNDIyMTU4Y2EzM2FkMTg3ZWU3MjdhYmI3OTZmMjEzMmRlZGZkMDFmYzQ5ZSJ9fX0=",
      new ItemBuilder(Material.SKULL_ITEM).durability(3).name("§e+1").build()));
    attachObject(10, 19);
    setItem(12, BukkitUtils.putProfileOnSkull(
      "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWQyNGRmYWYxZWUxN2Q3M2VlZWMyNDIyMTU4Y2EzM2FkMTg3ZWU3MjdhYmI3OTZmMjEzMmRlZGZkMDFmYzQ5ZSJ9fX0=",
      new ItemBuilder(Material.SKULL_ITEM).durability(3).name("§e+1").build()));
    attachObject(12, 21);
    setItem(14, BukkitUtils.putProfileOnSkull(
      "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWQyNGRmYWYxZWUxN2Q3M2VlZWMyNDIyMTU4Y2EzM2FkMTg3ZWU3MjdhYmI3OTZmMjEzMmRlZGZkMDFmYzQ5ZSJ9fX0=",
      new ItemBuilder(Material.SKULL_ITEM).durability(3).name("§e+1").build()));
    attachObject(14, 23);
    setItem(16, BukkitUtils.putProfileOnSkull(
      "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWQyNGRmYWYxZWUxN2Q3M2VlZWMyNDIyMTU4Y2EzM2FkMTg3ZWU3MjdhYmI3OTZmMjEzMmRlZGZkMDFmYzQ5ZSJ9fX0=",
      new ItemBuilder(Material.SKULL_ITEM).durability(3).name("§e+1").build()));
    attachObject(16, 25);

    setItem(28, BukkitUtils.putProfileOnSkull(
      "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWQyNGRmYWYxZWUxN2Q3M2VlZWMyNDIyMTU4Y2EzM2FkMTg3ZWU3MjdhYmI3OTZmMjEzMmRlZGZkMDFmYzQ5ZSJ9fX0=",
      new ItemBuilder(Material.SKULL_ITEM).durability(3).name("§e-1").build()));
    attachObject(28, 19);

    setItem(30, BukkitUtils.putProfileOnSkull(
      "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWQyNGRmYWYxZWUxN2Q3M2VlZWMyNDIyMTU4Y2EzM2FkMTg3ZWU3MjdhYmI3OTZmMjEzMmRlZGZkMDFmYzQ5ZSJ9fX0=",
      new ItemBuilder(Material.SKULL_ITEM).durability(3).name("§e-1").build()));
    attachObject(30, 21);

    setItem(32, BukkitUtils.putProfileOnSkull(
      "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWQyNGRmYWYxZWUxN2Q3M2VlZWMyNDIyMTU4Y2EzM2FkMTg3ZWU3MjdhYmI3OTZmMjEzMmRlZGZkMDFmYzQ5ZSJ9fX0=",
      new ItemBuilder(Material.SKULL_ITEM).durability(3).name("§e-1").build()));
    attachObject(32, 23);

    setItem(34, BukkitUtils.putProfileOnSkull(
      "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWQyNGRmYWYxZWUxN2Q3M2VlZWMyNDIyMTU4Y2EzM2FkMTg3ZWU3MjdhYmI3OTZmMjEzMmRlZGZkMDFmYzQ5ZSJ9fX0=",
      new ItemBuilder(Material.SKULL_ITEM).durability(3).name("§e-1").build()));
    attachObject(34, 25);

    setItem(19, new ItemBuilder(Material.GRASS).amount(arena.getConfiguration().getIslands()).name("§aQuantidade de ilhas").build());
    setItem(21, new ItemBuilder(Material.ANVIL).amount(arena.getConfiguration().getTeamsSize()).name("§bTamanho dos times").build());
    setItem(23, new ItemBuilder(Material.MAP).amount(arena.getConfiguration().getMinPlayers()).name("§aQuantidade mínima para iniciar").build());
    setItem(25, new ItemBuilder(Material.BARRIER).amount(arena.getConfiguration().getMaxPlayers()).name("§cQuantidade máxima de jogadores").build());

    this.register(Core.getInstance());
    open();
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent evt) {
    HandlerList.unregisterAll(this);
  }

  @EventHandler
  public void onInventoryClose(InventoryCloseEvent evt) {
    if (evt.getInventory().equals(this.getInventory())) {
      HandlerList.unregisterAll(this);
    }
  }
}
