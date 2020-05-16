package com.uzm.hylex.bedwars.loaders;

import com.google.common.collect.ImmutableList;
import com.uzm.hylex.bedwars.Core;
import com.uzm.hylex.bedwars.arena.enums.BuyEnums;
import com.uzm.hylex.bedwars.arena.improvements.UpgradeType;
import com.uzm.hylex.bedwars.arena.improvements.Upgrades;
import com.uzm.hylex.core.java.util.configuration.ConfigurationCreator;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class UpgradesLoader {

  private static HashMap<UpgradeType, Upgrades> upgrades = new LinkedHashMap<>();

  public static void setupUpgrades() {
    YamlConfiguration config = ConfigurationCreator.find("upgrades", Core.getInstance()).get();
    for (UpgradeType type : UpgradeType.values()) {
      ConfigurationSection section = config.getConfigurationSection("upgrades." + type.toString());
      Upgrades upgrade = new Upgrades(type);
      upgrade.setPrices(section.getIntegerList("price"));
      upgrade.setDescription(section.getStringList("description"));
      upgrade.setCoinTrade(BuyEnums.valueOf(section.getString("coinTrade")));

      upgrades.put(type, upgrade);
    }
  }

  public static Collection<Upgrades> listUpgrades() {
    return ImmutableList.copyOf(upgrades.values());
  }
}
