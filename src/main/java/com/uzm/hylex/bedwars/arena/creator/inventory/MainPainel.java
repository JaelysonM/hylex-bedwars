package com.uzm.hylex.bedwars.arena.creator.inventory;

import com.uzm.hylex.bedwars.arena.Arena;
import com.uzm.hylex.bedwars.arena.team.Team;
import com.uzm.hylex.bedwars.controllers.ArenaController;
import com.uzm.hylex.core.Core;
import com.uzm.hylex.core.api.HylexPlayer;
import com.uzm.hylex.core.spigot.inventories.PlayerMenu;
import com.uzm.hylex.core.spigot.items.ItemBuilder;
import com.uzm.hylex.core.utils.BukkitUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class MainPainel extends PlayerMenu {

  @EventHandler
  public void onInventoryClick(InventoryClickEvent evt) {
    if (evt.getInventory().equals(this.getInventory())) {
      evt.setCancelled(true);

      if (evt.getWhoClicked().equals(this.player)) {
        ItemStack item = evt.getCurrentItem();
        if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
          String display = item.getItemMeta().getDisplayName();


          if (display.equalsIgnoreCase("§b✉ §7Configurações essenciais")) {
            new SetupMenu(player, (Arena) getAttached(0));
          } else if (display.equalsIgnoreCase("§c♜ §7Localizações")) {
            player.getInventory().clear();
            player.updateInventory();

            player.getInventory().setItem(4, new ItemBuilder(Material.NETHER_STAR).name("§aSetar > §8Local de espera  §7(Clique para alterar)").build());

            player.getInventory().setItem(6, BukkitUtils.putProfileOnSkull(
              "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTZjYzQ4NmMyYmUxY2I5ZGZjYjJlNTNkZDlhM2U5YTg4M2JmYWRiMjdjYjk1NmYxODk2ZDYwMmI0MDY3In19fQ=",
              new ItemBuilder(Material.SKULL_ITEM).durability(3).name("§cAbrir configurações").build()));
            player.getInventory().setItem(7, BukkitUtils.putProfileOnSkull(
              "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjJkMTQ1YzkzZTVlYWM0OGE2NjFjNmYyN2ZkYWZmNTkyMmNmNDMzZGQ2MjdiZjIzZWVjMzc4Yjk5NTYxOTcifX19",
              new ItemBuilder(Material.SKULL_ITEM).durability(3).name("§aSalvar alterações").build()));

            player.sendMessage("§aYAY!! Você ganhou os itens para setar a localizações da arena.");
            player.closeInventory();
          } else if (display.equalsIgnoreCase("§a✎ §7Configurações do times")) {
            new TeamsMenu(getPlayer());
          } else if (display.equalsIgnoreCase("§e⎕ §7Bordas e proteção")) {
            player.getInventory().clear();
            player.updateInventory();

            player.getInventory().setItem(2, new ItemBuilder(Material.WOOD_AXE).name("§aSetar posições §7(Esquerdo: Localização #1/Direito: Localização #2)").build());
            player.getInventory().setItem(4, new ItemBuilder(Material.GLASS).name("§bBordas da arena §7(Clique para alterar)").build());
            player.getInventory().setItem(6, BukkitUtils.putProfileOnSkull(
              "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTZjYzQ4NmMyYmUxY2I5ZGZjYjJlNTNkZDlhM2U5YTg4M2JmYWRiMjdjYjk1NmYxODk2ZDYwMmI0MDY3In19fQ=",
              new ItemBuilder(Material.SKULL_ITEM).durability(3).name("§cAbrir configurações").build()));
            player.getInventory().setItem(7, BukkitUtils.putProfileOnSkull(
              "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjJkMTQ1YzkzZTVlYWM0OGE2NjFjNmYyN2ZkYWZmNTkyMmNmNDMzZGQ2MjdiZjIzZWVjMzc4Yjk5NTYxOTcifX19",
              new ItemBuilder(Material.SKULL_ITEM).durability(3).name("§aSalvar alterações").build()));

            player.sendMessage("§aYAY!! Você ganhou os itens para configurar as bordas e as áreas protegidas.");
            player.closeInventory();
          } else if (display.equalsIgnoreCase("§a♺ §7Geradores globais")) {
            player.getInventory().clear();
            player.updateInventory();

            player.getInventory().setItem(4, new ItemBuilder(Material.DIAMOND_AXE).name("§aCriar um gerador §8(Tipo definido de acordo com o bloco clicado)").build());
            player.getInventory().setItem(6, BukkitUtils.putProfileOnSkull(
              "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTZjYzQ4NmMyYmUxY2I5ZGZjYjJlNTNkZDlhM2U5YTg4M2JmYWRiMjdjYjk1NmYxODk2ZDYwMmI0MDY3In19fQ=",
              new ItemBuilder(Material.SKULL_ITEM).durability(3).name("§cAbrir configurações").build()));
            player.getInventory().setItem(7, BukkitUtils.putProfileOnSkull(
              "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjJkMTQ1YzkzZTVlYWM0OGE2NjFjNmYyN2ZkYWZmNTkyMmNmNDMzZGQ2MjdiZjIzZWVjMzc4Yjk5NTYxOTcifX19",
              new ItemBuilder(Material.SKULL_ITEM).durability(3).name("§aSalvar alterações").build()));

            player.sendMessage("§aYAY!! Você ganhou os itens para criar/setar os geradores globais.");
            player.closeInventory();
          } else if (display.equalsIgnoreCase("§aSalvar arena")) {
            if (ArenaController.getArena(((Arena)getAttached(0)).getArenaName()) == null) {
              player.getInventory().clear();
              long delay = System.currentTimeMillis();
              ArenaController.saveArena((Arena) HylexPlayer.getByPlayer(player).getAbstractArena());
              player.sendMessage(
                "§aYAY!! Você criou a arena " + HylexPlayer.getByPlayer(player).getAbstractArena().getArenaName() + "§8(" + (System.currentTimeMillis() - delay) + " ms)§a.");

              HylexPlayer.getByPlayer(player.getPlayer()).setAbstractArena(null);

              player.closeInventory();
            } else {
              Arena arena = (Arena) getAttached(0);
              if (arena != null) {
                long delay = System.currentTimeMillis();
                ArenaController.saveExistentArena(arena);
                player.sendMessage("§aYAY!! Você salvou a arena " + arena.getArenaName() + "§8(" + (System.currentTimeMillis() - delay) + " ms)§a.");
                player.closeInventory();;
              } else {
                player.sendMessage("§cNâo foi possível salvar esta arena.");
                player.closeInventory();;
              }

            }
          }
        }
      }
    }
  }


  public MainPainel(Player player, Arena arena) {
    super(player, "§7Configuração " + arena.getArenaName(), 5);
    attachObject(0, arena);
    int teamCFS = (int) arena.listTeams().stream().filter(Team::isTotallyConfigured).count();
    setItem(4, new ItemBuilder(Material.PAPER).name("§bVisão geral da arena")
      .lore("", "§eInformações:", "", " §a✚ Configurações essenciais: ", "", "  §a⤷ §7Máximo de jogadores §f" + arena.getConfiguration().getMaxPlayers(),
        "  §a⤷ §7Mínimo de jogadores §f" + arena.getConfiguration().getMinPlayers(), "  §a⤷ §7Quantidade de jogadores por time §f" + arena.getConfiguration().getTeamsSize(),
        "  §a⤷ §7Quantidade de ilhas §f" + arena.getConfiguration().getIslands(), "  §b⤷ §7Quantidade de geradores globais §f" + arena.getGenerators().size(), "",
        " §b⦿ Bordas e proteção: ", "  §a⤷ §7Borda principal criada: " + (arena.getBorders() != null ? "§a§l✔" : "§c§l✖"),
        "  §a⤷ §7Quantidade áreas protegidas: §b" + arena.getCantConstruct().size(), "", " §c⚑ Localizações do mapa: ",
        "  §a⤷ §7Local de espera criado: " + (arena.getWaitingLocation() != null ? "§a§l✔" : "§c§l✖"),
        "  §a⤷ §7Local de espectar criado: " + (arena.getSpectatorLocation() != null ? "§a§l✔" : "§c§l✖")

      ).build());

    setItem(19, new ItemBuilder(Material.BANNER).durability(1).name("§a✎ §7Configurações do times").lore("", "§7Clique aqui para configurar o times disponíveis.", "",
      " §6- §7Progresso de configuração: §a" + teamCFS + "/" + arena.getConfiguration().getIslands() + " §8" + (teamCFS / arena.getConfiguration().getIslands()) * 100 + "%", "",
      "§e* Clique para aqui para ver os times disponíveis").build());

    setItem(22, new ItemBuilder(Material.COMPASS).name("§c♜ §7Localizações")
      .lore("", "§7Clique aqui para setar as localizações da arena.", " ", " §6- §7Local de espera: " + (arena.getWaitingLocation() != null ? "§a§l✔" : "§c§l✖"),
        " §6- §7Local do espectador: " + (arena.getSpectatorLocation() != null ? "§a§l✔" : "§c§l✖"), "  §8(Você ganhará itens para configurar)", "",
        "§e* Clique para aqui para configurar").build());


    setItem(20, new ItemBuilder(Material.BEACON).name("§e⎕ §7Bordas e proteção")
      .lore("", "§7Clique aqui para configurar o bordas e proteções .", "", " §6- §7Borda principal: " + (arena.getBorders() != null ? "§a§l✔" : "§c§l✖"),
        " §6- §7Número de áreas protegidas: §b" + arena.getCantConstruct().size(), "  §8(Você ganhará itens para configurar)", "", "§e* Clique para aqui para configurar").build());


    setItem(24, new ItemBuilder(Material.EMERALD_BLOCK).name("§a♺ §7Geradores globais")
      .lore("", "§7Clique criar e setar as localizações dos geradores globais (§aEsmeralda§7, §bDiamante).", "",
        " §6- §7Quantidade de geradores criados: " + arena.getGenerators().size(), "  §8(Você ganhará itens para configurar)", "", "§e* Clique para aqui para configurar").build());

    setItem(25,
      new ItemBuilder(Material.ANVIL).name("§b✉ §7Configurações essenciais").lore("", "§7Clique alterar as configurações essenciais.", "", "§e* Clique para aqui para configurar")
        .build());


    setItem(43, BukkitUtils.putProfileOnSkull(
      "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmEyNGEyYjZiNGI1YTkyZDdhODJhMzczZmU1ZjZiYjY2ODcyZWFkNjZjMTI2ZjgyZTg4NjQxNzNjZDc4M2EifX19=",
      new ItemBuilder(Material.SKULL_ITEM).durability(3).name("§cDeletar arena")
        .lore("§7Você deletará todas a informações dessa arena", "§7se ela ainda não estiver criada o 'abstractArena'", "§7será resetado", "",
          "§e* Clique aqui para deletar/resetar").build()));
    if (arena.isFullyConfigured(teamCFS)) {
      setItem(44, BukkitUtils.putProfileOnSkull(
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjJkMTQ1YzkzZTVlYWM0OGE2NjFjNmYyN2ZkYWZmNTkyMmNmNDMzZGQ2MjdiZjIzZWVjMzc4Yjk5NTYxOTcifX19",
        new ItemBuilder(Material.SKULL_ITEM).durability(3).name("§aSalvar arena")
          .lore("§7Você salvará suas alterações", "§7feitas na arena §b" + arena.getArenaName() + ", §7se ela", "§7não existir será criada normalmente.", "",
            "§e* Clique para aqui para salvar/criar").build()));
    } else {
      setItem(44, BukkitUtils.putProfileOnSkull(
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjJkMTQ1YzkzZTVlYWM0OGE2NjFjNmYyN2ZkYWZmNTkyMmNmNDMzZGQ2MjdiZjIzZWVjMzc4Yjk5NTYxOTcifX19",
        new ItemBuilder(Material.SKULL_ITEM).durability(3).name("§cSalvar arena").lore("§7")
          .lore("§7Ainda faltam algumas configurações nessa", "§7arena logo impossibilitando a(o) sua(eu) criação/salvamente.", "",
            "§c* Por favor, revise sua configuração na §b'Visão Geral'§c.").build()));
    }
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
