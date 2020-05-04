package com.uzm.hylex.bedwars.loaders;

import com.uzm.hylex.bedwars.Core;
import com.uzm.hylex.bedwars.arena.shop.Shop;
import com.uzm.hylex.bedwars.controllers.ArenaController;
import com.uzm.hylex.bedwars.controllers.MatchmakingController;
import com.uzm.hylex.bedwars.proxy.ServerItem;
import com.uzm.hylex.core.java.util.ConfigurationCreator;
import com.uzm.hylex.core.java.util.JavaReflections;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class PluginLoader {

  private PluginManager pm = Bukkit.getServer().getPluginManager();
  private Core core;

  public PluginLoader(Core core) {
    this.core = core;
    loadConfigurations();

    this.registerCommands();
    this.registerListeners();

    Shop.setupShop();
    UpgradesLoader.setupUpgrades();

    ArenaController.loadArenas();

    MatchmakingController.setupMatchmaking();

    ServerItem.setupServers();
  }

  public void loadConfigurations() {
    new ConfigurationCreator(core, "upgrades", "");
    new ConfigurationCreator(core, "itemshop", "");
  }

  @SuppressWarnings("unchecked")
  public void registerCommands() {
    long registered = 0;
    List<Class<?>> classes = JavaReflections.getClasses("com.uzm.hylex.bedwars.commands", getCore());

    try {
      for (Class<?> c : classes) {
        Method handshake = JavaReflections.getMethod(c, "getInvoke");
        ArrayList<String> list = (ArrayList<String>) handshake.invoke(new ArrayList<String>());

        for (String r : list) {
          getCore().getCommand(r).setExecutor((CommandExecutor) c.newInstance());
        }

        registered++;
      }
    } catch (Exception e) {
      System.err.println("Probally An error occurred while trying to register some commands.");
      e.printStackTrace();
    }

    Bukkit.getConsoleSender().sendMessage("§b[Hylex - BedWars] §7We're registered §f(" + registered + "/" + classes.size() + ") §7commands.");
  }

  public void registerListeners() {
    long registered = 0;
    List<Class<?>> classes = JavaReflections.getClasses("com.uzm.hylex.bedwars.listeners", getCore());

    try {
      for (Class<?> c : classes) {
        pm.registerEvents((Listener) c.newInstance(), getCore());
        registered++;
      }
    } catch (Exception e) {
      System.err.println("Probally An error occurred while trying to register some listeners  ");
      e.printStackTrace();
    }

    Bukkit.getConsoleSender().sendMessage("§b[Hylex - BedWars] §7We're registered §f(" + registered + "/" + classes.size() + ") §7listeners.");
  }

  public Core getCore() {
    return core;
  }
}
