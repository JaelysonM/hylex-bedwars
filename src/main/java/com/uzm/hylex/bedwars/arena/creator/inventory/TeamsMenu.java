package com.uzm.hylex.bedwars.arena.creator.inventory;

import com.google.common.collect.Lists;
import com.uzm.hylex.bedwars.arena.Arena;
import com.uzm.hylex.bedwars.arena.team.Team;
import com.uzm.hylex.bedwars.arena.team.Teams;
import com.uzm.hylex.core.api.HylexPlayer;
import com.uzm.hylex.core.spigot.inventories.PageablePlayerInventory;
import com.uzm.hylex.core.spigot.items.ItemBuilder;
import com.uzm.hylex.core.utils.BukkitUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class TeamsMenu extends PageablePlayerInventory {

  public TeamsMenu(Player viewer) {
    super(viewer, Bukkit.createInventory(null, 36, "§7Configure os times"), "§7Configure os times");
    HylexPlayer hp = HylexPlayer.getByPlayer(viewer);

    if (hp != null && hp.getAbstractArena() != null) {
      config(new int[] {11, 12, 13, 14, 15, 21, 22, 23});
      List<ItemStack> items = Lists.newArrayList();

      Arena arena = (Arena) hp.getAbstractArena();
      for (int i = 0; i < arena.getConfiguration().getIslands(); i++) {
        Teams team = Teams.values()[i];
        Team t = arena.getTeams().get(team);
        boolean hasCreated = arena.getTeams().get(team).isTotallyConfigured();
        items.add(new ItemBuilder(hasCreated ? Material.WOOL : Material.STAINED_GLASS).durability(team.getColor().getData())
          .name((hasCreated ? "§a✔" : "§c✖") + " §7Configuração do time: " + team.getDisplayName())
          .lore("", " §c⚑ Localizações do time: ", "", "  §a⤷ §7Local de spawn: " + (t.getSpawnLocation() != null ? "§a§l✔" : "§c§l✖"),
            "  §a⤷ §7Local da cama: " + (t.getBedLocation() != null ? "§a§l✔" : "§c§l✖"),
            "  §a⤷ §7Local da do NPC de Melhorias: " + (t.getUpgradeLocation() != null ? "§a§l✔" : "§c§l✖"),
            "  §a⤷ §7Local da do NPC de Loja: " + (t.getShopLocation() != null ? "§a§l✔" : "§c§l✖"), "", " §b⦿ Bordas e geradores: ",
            "  §a⤷ §7Borda da ilha criada: " + (t.getBorder() != null ? "§a§l✔" : "§c§l✖"), "  §a⤷ §7Quantidade de geradores criados: §b" + t.getTeamGenerators().size(), "",
            "§e* Clique para configurar").build());
      }


      fill(items.toArray(new ItemStack[] {}),
        new Object[][] {{new ItemBuilder(Material.ARROW).name("§ePágina anterior").build(), 34}, {new ItemBuilder(Material.ARROW).name("§ePágina posterior").build(), 34}},
        new Object[][] {{BukkitUtils.putProfileOnSkull(
          "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmIwZjZlOGFmNDZhYzZmYWY4ODkxNDE5MWFiNjZmMjYxZDY3MjZhNzk5OWM2MzdjZjJlNDE1OWZlMWZjNDc3In19fQ==",
          new ItemBuilder(Material.SKULL_ITEM).durability(3).name("§bVoltar").build()), 0},}, new Object[] {22, new ItemStack(Material.AIR)});
      open(getPlayer(), 1);
    }
  }

  public void click(Inventory inv, ItemStack item, int slot) {
    Player player = getPlayer();
    if (item.getType() == Material.SKULL_ITEM && item.getItemMeta().getDisplayName().startsWith("§9Página anterior")) {
      open(getPlayer(), getCurrent() - 1);
    } else if (item.getType() == Material.SKULL_ITEM && item.getItemMeta().getDisplayName().startsWith("§9Próxima página")) {
      open(getPlayer(), getCurrent() + 1);
    } else if (item.getType() == Material.SKULL_ITEM && item.getItemMeta().getDisplayName().startsWith("§bVoltar")) {
      new MainPainel(getPlayer(), (Arena) HylexPlayer.getByPlayer(player).getAbstractArena());
    } else if ((item.getType() == Material.STAINED_GLASS || item.getType() == Material.WOOL) && item.getItemMeta().getDisplayName().contains("§7Configuração do time: ")) {
      Teams team = Teams.getByData(item.getDurability());
      player.getInventory().clear();
      player.updateInventory();

      HylexPlayer.getByPlayer(player).setAuxiler(team);

      assert team != null;
      player.getInventory().setItem(1, new ItemBuilder(Material.WOOD_AXE).name("§aSetar posições da borda da ilha §7(Esquerdo: Localização #1/Direito: Localização #2)").build());
      player.getInventory().setItem(2, new ItemBuilder(Material.DROPPER).name("§eAdicionar gerador do time").build());
      player.getInventory().setItem(3, new ItemBuilder(Material.COMPASS).name("§aSetar NPCs §7(Esquerdo: Altere o tipo/Direito: Loja/Direito+Shift: Melhorias)").build());


      player.getInventory().setItem(4,
        new ItemBuilder(Material.STAINED_GLASS_PANE).durability(item.getDurability()).name("§aConfigurando o time: " + team.getDisplayName() + " §7(Clique para alterar)").build());

      player.getInventory().setItem(6, BukkitUtils.putProfileOnSkull(
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTZjYzQ4NmMyYmUxY2I5ZGZjYjJlNTNkZDlhM2U5YTg4M2JmYWRiMjdjYjk1NmYxODk2ZDYwMmI0MDY3In19fQ=",
        new ItemBuilder(Material.SKULL_ITEM).durability(3).name("§cAbrir configurações").build()));
      player.getInventory().setItem(7, BukkitUtils.putProfileOnSkull(
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjJkMTQ1YzkzZTVlYWM0OGE2NjFjNmYyN2ZkYWZmNTkyMmNmNDMzZGQ2MjdiZjIzZWVjMzc4Yjk5NTYxOTcifX19",
        new ItemBuilder(Material.SKULL_ITEM).durability(3).name("§aSalvar alterações").build()));
      player.closeInventory();

      player.sendMessage("§aYAY!! Você ganhou os itens para configurar o time: " + team.getDisplayName() + "§a.");
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
