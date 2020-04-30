package com.uzm.hylex.bedwars.loaders;

import com.google.common.collect.Maps;
import com.uzm.hylex.bedwars.Core;
import com.uzm.hylex.bedwars.arena.enums.BuyEnums;
import com.uzm.hylex.bedwars.arena.improvements.UpgradeType;
import com.uzm.hylex.bedwars.arena.improvements.Upgrades;
import com.uzm.hylex.core.java.util.ConfigurationCreator;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.HashMap;

public class UpgradesLoader {

  private static HashMap<UpgradeType, Upgrades> upgrades = Maps.newHashMap();


  public UpgradesLoader(Core core) {
    YamlConfiguration config = ConfigurationCreator.find("upgrades", Core.getInstance()).get();
    for (UpgradeType type : UpgradeType.values()) {
      ConfigurationSection section = config.getConfigurationSection("upgrades." + type.toString());
      Upgrades upgrade = new Upgrades(section.getString("name"));
      upgrade.setDescription(section.getStringList("description"));
      upgrade.setCoinTrade(BuyEnums.valueOf(section.getString("coinTrade")));
      section.getStringList("price").forEach(result -> upgrade.setPrices(Integer.parseInt(result.split(" | ")[0]), Integer.parseInt(result.split(" | ")[1])));

      upgrades.put(type, upgrade);
    }
  }

  public static HashMap<UpgradeType, Upgrades> getUpgrades() {
    return upgrades;
  }
}
