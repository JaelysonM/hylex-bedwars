package com.uzm.hylex.bedwars.proxy;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.uzm.hylex.bedwars.Core;
import com.uzm.hylex.bedwars.proxy.balancer.BaseBalancer;
import com.uzm.hylex.bedwars.proxy.balancer.Server;
import com.uzm.hylex.bedwars.proxy.balancer.type.LeastConnection;
import com.uzm.hylex.core.api.HylexPlayer;
import com.uzm.hylex.core.java.util.configuration.ConfigurationCreator;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * @author Maxter
 */
public class ServerItem {

  private String key;
  private BaseBalancer<Server> balancer;

  public ServerItem(String key, BaseBalancer<Server> baseBalancer) {
    this.key = key;
    this.balancer = baseBalancer;
  }

  public void connect(HylexPlayer hp) {
    Player player = hp.getPlayer();

    Server server = balancer.next();
    if (server != null) {
      if (!server.canBeSelected()) {
        if (player.hasPermission("hylex.joinfull")) {
          player.closeInventory();
          hp.save();
          ByteArrayDataOutput out = ByteStreams.newDataOutput();
          out.writeUTF("Connect");
          out.writeUTF(server.getName());
          player.sendPluginMessage(Core.getInstance(), "BungeeCord", out.toByteArray());
          player.sendMessage("§8Sendo enviado para " + server.getName() + "...");
        } else {
          player.sendMessage("§cNos desculpe, mas esse servidor encontra-se cheio no momento, adquira §b§lVIPs §cpara sobrepor essa restrição.");
        }
      } else {
        player.closeInventory();
        hp.save();
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(server.getName());
        player.sendPluginMessage(Core.getInstance(), "BungeeCord", out.toByteArray());
        player.sendMessage("§8Sendo enviado para " + server.getName() + "...");

      }
    } else {
      player.sendMessage("§cNos desculpe, mas não foi possível se conectar a esse servidor.");
      player.playSound(player.getLocation(), Sound.ITEM_PICKUP, 2.0F, 2.0F);
    }
  }

  public String getKey() {
    return this.key;
  }


  public BaseBalancer<Server> getBalancer() {
    return this.balancer;
  }

  private static final List<ServerItem> SERVERS = new ArrayList<>();
  public static final Map<String, ServerStatus> SERVER_INFO = new HashMap<>();

  public static void setupServers() {
    YamlConfiguration config = ConfigurationCreator.find("setup", Core.getInstance()).get();

    ServerItem lobby = new ServerItem("lobby", new LeastConnection<>());
    for (String lobbies : config.getStringList("server-configuration.lobbies")) {
      lobby.getBalancer().add(lobbies.toLowerCase(), new Server(lobbies.toLowerCase(), 100));
    }

    SERVERS.add(lobby);

    new BukkitRunnable() {
      @Override
      public void run() {
        SERVERS.forEach(server -> server.getBalancer().keySet().forEach(ServerItem::writeCount));
      }
    }.runTaskTimerAsynchronously(Core.getInstance(), 0, 40);
  }

  public static Collection<ServerItem> listServers() {
    return SERVERS;
  }

  public static ServerItem getServerItem(String key) {
    return SERVERS.stream().filter(si -> si.getKey().equals(key)).findFirst().orElse(null);
  }

  public static boolean alreadyQuerying(String servername) {
    return SERVERS.stream().anyMatch(si -> si.getBalancer().keySet().contains(servername));
  }

  public static int getServerCount(ServerItem serverItem) {
    return serverItem.getBalancer().getTotalNumber();
  }

  public static int getServerCount(String servername) {
    return SERVER_INFO.get(servername) == null ? 0 : SERVER_INFO.get(servername).getPlayers();
  }

  public static boolean isOnline(String servername) {
    return SERVER_INFO.get(servername) != null && SERVER_INFO.get(servername).isOnline();
  }

  public static void writeCount(String server) {

    if (Bukkit.getOnlinePlayers().isEmpty()) {
      return;
    }

    Player fake = Bukkit.getOnlinePlayers().toArray(new Player[] {})[new Random().nextInt(Bukkit.getOnlinePlayers().size())];
    if (fake == null) {
      return;
    }

    ByteArrayDataOutput out = ByteStreams.newDataOutput();
    out.writeUTF("PlayerCount");
    out.writeUTF(server);

    if (ServerItem.SERVER_INFO.get(server.toLowerCase()) != null) {
      ServerItem.SERVER_INFO.get(server.toLowerCase()).setUpStatus(false);
    } else {
      ServerItem.SERVER_INFO.put(server.toLowerCase(), new ServerStatus(server, false));
    }

    fake.sendPluginMessage(Core.getInstance(), "BungeeCord", out.toByteArray());

    ByteArrayDataOutput out2 = ByteStreams.newDataOutput();
    out2.writeUTF("ServerIP");
    out2.writeUTF(server);

    fake.sendPluginMessage(Core.getInstance(), "BungeeCord", out2.toByteArray());
  }


}
