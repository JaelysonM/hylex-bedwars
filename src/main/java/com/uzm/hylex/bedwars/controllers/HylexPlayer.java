package com.uzm.hylex.bedwars.controllers;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.uzm.hylex.bedwars.Core;
import com.uzm.hylex.bedwars.arena.Arena;
import com.uzm.hylex.bedwars.arena.player.ArenaPlayer;
import com.uzm.hylex.core.controllers.TagController;
import com.uzm.hylex.core.spigot.features.TabColor;
import com.uzm.hylex.services.lan.WebSocket;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HylexPlayer {

  private String name;
  private Player player;
  private String id;
  private Group group;
  private ArenaPlayer arenaPlayer;

  private Arena abstractArena;
  private boolean accountLoaded = false;
  private boolean aux = false;

  private Object auxiler;

  private Map<Player, Long> lastHit = new HashMap<>();
  private HashMap<Integer,Location >temporaryLocation = Maps.newHashMap();

  public static ArrayList<Player> staff = Lists.newArrayList();
  public static HashMap<String, HylexPlayer> datas = new HashMap<>();

  public HylexPlayer(Player player) {
    this.name = player.getName();
    this.player = player;

    this.group = Group.getPlayerGroup(player);

    setArenaPlayer(new ArenaPlayer(player,null));
  }

  public void destroy() {
    this.name = null;
    this.player = null;
    this.id = null;
    this.group = null;
    this.abstractArena = null;
    temporaryLocation.clear();
    this.temporaryLocation = null;
    this.lastHit.clear();
    this.lastHit = null;
    if (this.arenaPlayer != null) {
      this.arenaPlayer.destroy();
      this.arenaPlayer = null;
    }
  }

  public void setHit(Player player) {
    this.lastHit.put(player, System.currentTimeMillis() + 8000);
  }

  public void setupHotBar() {
    // SETAR HOTBAR
  }

  public void setupPlayer() {
    player.setLevel(-100);
    player.setHealthScaled(true);
    player.setHealthScale(2);
    player.setHealth(20);
    player.setFoodLevel(100);
    player.closeInventory();
    player.getInventory().clear();
    player.setGameMode(GameMode.ADVENTURE);
    player.teleport(player.getWorld().getSpawnLocation());
    player.setAllowFlight(true);
    player.getActivePotionEffects().clear();

    if (!player.hasPermission("hylex.fly"))
      player.setFlying(false);



    TagController.create(player);
    TagController tag = TagController.get(player);

    tag.setPrefix(group.getDisplay());
    tag.setOrder(group.getOrder());
    tag.update();
    player.setDisplayName(group.getDisplay() + player.getName());

    new TabColor(player).setFooter("\n §b§lHYLEX \n    §7Seja bem-vindo §E" + player.getName() + "§7." + "\n")
      .setBottom("\n §7Seu grupo é: " + group.getName() + "\n§7Você está em: §fbedwars-" + Core.getLoader().getServerName() + "\n\n§b§nhylex.net§r ").send();

  }

  public boolean isAccountLoaded() {
    return this.accountLoaded;
  }

  public void accountLoad() {
    this.accountLoaded = true;
  }

  public String getName() {
    return this.name;
  }

  public Player getPlayer() {
    return this.player;
  }

  public void setName(String nick) {
    this.name = nick;
  }

  public void setID(String id) {
    this.id = id;
  }

  public String getID() {
    return this.id;
  }

  public void setPlayer(Player p) {
    player = p;
  }

  public Group getGroup() {
    return this.group;
  }

  public ArenaPlayer getArenaPlayer() {
    return this.arenaPlayer;
  }

  public List<HylexPlayer> getLastHitters() {
    List<HylexPlayer> hitters = this.lastHit.entrySet().stream()
      .filter(entry -> get(entry.getKey()) != null)
      .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
      .map(entry -> get(entry.getKey()))
      .collect(Collectors.toList());
    this.lastHit.clear();
    return hitters;
  }

  public void setArenaPlayer(ArenaPlayer arenaPlayer) {
    if (this.arenaPlayer != null) {
      this.arenaPlayer.destroy();
    }
    this.arenaPlayer = arenaPlayer;
  }

  public void requestLoad() {
    JSONObject json = new JSONObject();
    json.put("require", "user-info");
    JSONObject array = new JSONObject();
    array.put("uuid", getPlayer().getUniqueId());
    array.put("nickname", getPlayer().getName());
    json.put("bodyDefault", array);

    WebSocket.get("bedwars-" + Core.getLoader().getServerName()).getSocket().emit("require-bedwarsprofile", json);
  }

  public void save() {
    if (!isAccountLoaded()) {
      return;
    }
    JSONObject json = new JSONObject();
    json.put("id", getID());

    /*
     * Send socket.io request
     */
    WebSocket.get("bedwars-" + Core.getLoader().getServerName()).getSocket().emit("save-bedwarsprofile", json);
  }

  public void setAbstractArena(Arena abstractArena) {
    this.abstractArena = abstractArena;
  }

  public Arena getAbstractArena() {
    return abstractArena;
  }

  public void setTemporaryLocation(HashMap<Integer,Location> temporaryLocation) {
    this.temporaryLocation = temporaryLocation;
  }

  public HashMap<Integer,Location> getTemporaryLocation() {
    return temporaryLocation;
  }

  public void setAux(boolean aux) {
    this.aux = aux;
  }

  public boolean getAux() {
    return aux;
  }

  public void setAuxiler(Object auxiler) {
    this.auxiler = auxiler;
  }

  public Object getAuxiler() {
    return auxiler;
  }

  public static HylexPlayer create(Player player) {
    datas.computeIfAbsent(player.getUniqueId().toString(), list -> new HylexPlayer(player));
    if (!staff.contains(player) && player.hasPermission("hylex.staff")) {
      staff.add(player);
    }
    return get(player);
  }

  public static HylexPlayer remove(Player p) {
    staff.remove(p);
    return datas.remove(p.getUniqueId().toString());
  }

  public static HylexPlayer get(Player p) {
    return datas.getOrDefault(p.getUniqueId().toString(), null);
  }

  public static HylexPlayer get(String uuid) {
    return datas.getOrDefault(uuid, null);
  }

  public static List<HylexPlayer> getDatas() {
    return new ArrayList<>(datas.values());
  }


  public enum Group {

    HYLEX("§bHylex", "§b[Hylex] ", "*", "a", true),
    GERENTE("§4Gerente", "§4[Gerente] ", "hylex.group.gerente", "b", true),
    ADMIN("§9Admin", "§9[Admin] ", "hylex.group.admin", "c", true),
    DESENVOLVEDOR("§6Desenvolvedor", "§6[Desenvolvedor] ", "hylex.group.dev", "d", true),
    MODERADOR("§2Moderador", "§2[Moderador]", "hylex.group.mod", "e", true),
    AJUDANTE("§eAjudante", "§e[Ajudante] ", "hylex.group.ajd", "f", true),
    PASSE("§3Passe", "§3[Passe] ", "hylex.group.pass", "g"),
    STREAMER("§5Streamer", "§5[Streamer] ", "hylex.group.streamer", "h", true),
    MINIYT("§cMiniYT", "§c[MiniYT] ", "hylex.group.miniyt", "i", true),
    NORMAL("§7Normal", "§7", "hylex.group.normal", "j");

    private String color;
    private String display;
    private String permission;
    private String order;
    private String name;
    private boolean alwaysVisible;

    Group(String name, String display, String permission, String order, boolean visible) {
      this.display = display;
      this.order = order;
      this.permission = permission;
      this.name = name;
      this.alwaysVisible = visible;
      this.color = this.name.substring(0, 2);
    }

    Group(String name, String display, String permission, String order) {
      this.display = display;
      this.order = order;
      this.permission = permission;
      this.name = name;
      this.alwaysVisible = false;
    }

    public String getColor() {
      return this.color;
    }

    public String getDisplay() {
      return this.display;
    }

    public boolean isAlwaysVisible() {
      return this.alwaysVisible;
    }

    public String getPermission() {
      return this.permission;
    }

    public String getOrder() {
      return this.order;
    }

    public String getName() {
      return this.name;
    }

    public static String getColored(Player player) {
      String color = "§7";

      HylexPlayer hp = HylexPlayer.get(player);
      if (hp != null) {
        color = hp.getGroup().getColor();
      }

      return color + player.getName();
    }

    public static Group getPlayerGroup(Player player) {
      for (Group group : Group.values()) {
        if (player.hasPermission(group.getPermission())) {
          return group;
        }
      }

      return NORMAL;
    }
  }
}
