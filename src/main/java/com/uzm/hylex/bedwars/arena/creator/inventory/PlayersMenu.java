package com.uzm.hylex.bedwars.arena.creator.inventory;

import com.uzm.hylex.bedwars.Core;
import com.uzm.hylex.bedwars.arena.Arena;
import com.uzm.hylex.bedwars.arena.player.ArenaPlayer;
import com.uzm.hylex.core.api.Group;
import com.uzm.hylex.core.api.HylexPlayer;
import com.uzm.hylex.core.api.interfaces.IArenaPlayer;
import com.uzm.hylex.core.controllers.FakeController;
import com.uzm.hylex.core.spigot.inventories.GlobalUpdatableInventory;
import com.uzm.hylex.core.spigot.items.ItemBuilder;
import com.uzm.hylex.core.spigot.utils.BukkitUtils;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.text.DecimalFormat;


public class PlayersMenu extends GlobalUpdatableInventory {

  private Arena arena;

  public PlayersMenu(String name, int rows, Arena arena) {
    super(name, rows);

    this.arena=arena;
    register(Core.getInstance());
  }

  @EventHandler
  public void onInventoryClick(InventoryClickEvent evt) {
    if (evt.getInventory().equals(this.getInventory())) {
      evt.setCancelled(true);
      Player player = (Player) evt.getWhoClicked();

      ItemStack currentItem = evt.getCurrentItem();
      if (currentItem != null && currentItem.hasItemMeta() && currentItem.getItemMeta().hasDisplayName()) {
          if (getAttached(evt.getSlot()) != null) {
            Player target = (Player) getAttached(evt.getSlot());
            if (target != null) {
              player.teleport(target.getLocation().add(0, 1, 0));
              player.closeInventory();
            }
          }

      }
    }
  }
  private static final DecimalFormat TRACKING_FORMAT = new DecimalFormat("###.#");
  @Override
  public void update() {
    int slot = 10;
    getInventory().clear();
    for (IArenaPlayer apls : arena.getPlayingPlayers()) {
      if (slot == 17) {
        break;
      }
      ArenaPlayer ap = (ArenaPlayer) apls;

      if (ap.getPlayer() != null) {
        if (HylexPlayer.getByPlayer(ap.getPlayer()) != null) {
          if (ap.getCurrentState().isInGame() && ap.getTeam() != null) {
            if (FakeController.has(ap.getPlayer().getName())) {
              setItem(slot, BukkitUtils.putProfileOnSkull(ap.getPlayer(),
                new ItemBuilder(Material.SKULL_ITEM).durability(3).name(Group.NORMAL.getDisplay() + FakeController.getFake(ap.getPlayer().getName()))
                  .lore("", "§7Vida: §c" + TRACKING_FORMAT.format(ap.getPlayer().getHealth()+ ((CraftPlayer) ap.getPlayer()).getHandle().getAbsorptionHearts())+ " HP", "§7Time: §c" + ap.getTeam().getTeamType().getDisplayName(), "", "§bClique para teleportar.").build()));
            } else {
              setItem(slot, BukkitUtils.putProfileOnSkull(ap.getPlayer(),
                new ItemBuilder(Material.SKULL_ITEM).durability(3).name(HylexPlayer.getByPlayer(ap.getPlayer()).getGroup().getDisplay() + ap.getPlayer().getName())
                  .lore("", "§7Vida: §c" + TRACKING_FORMAT.format(ap.getPlayer().getHealth()+ ((CraftPlayer) ap.getPlayer()).getHandle().getAbsorptionHearts()) + " HP", "§7Time: §c" + ap.getTeam().getTeamType().getDisplayName(), "", "§bClique para teleportar.").build()));

            }
            attachObject(slot, ap.getPlayer());
            slot++;

          }
        }
      }

    }

  }
}
