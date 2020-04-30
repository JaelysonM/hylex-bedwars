package com.uzm.hylex.bedwars.arena.improvements;

import com.uzm.hylex.bedwars.arena.enums.BuyEnums;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Upgrades {

  private Pattern pattern = Pattern.compile("([<])([0-9])([>])");

  private Pattern patternTier = Pattern.compile("([{])([r0-9])([}])");


  private String name;
  private List<String> description;
  private Integer[] price = new Integer[300];
  private BuyEnums coinTrade;

  public Upgrades(String name) {
    this.name = name;
  }

  public void setDescription(List<String> description) {
    description.replaceAll(result -> {
      Matcher matcher = pattern.matcher(result);
      return matcher.find() ? result.replace("&", "§").replace(matcher.group(), String.valueOf(price[Integer.parseInt(matcher.group(2))])) : result.replace("&", "§");
    });
  }

  public void setPrices(int tier, int price) {
    this.price[tier] = price;
  }

  public List<String> getDescription() {
    return description;
  }

  public List<String> getUpdatableDescription(int currentTier) {
    List<String> updatable = new ArrayList<>(description);

    updatable.replaceAll(result -> {
      Matcher matcher = patternTier.matcher(result);
      return matcher.find() ? result.replace(matcher.group(), currentTier >= Integer.parseInt(matcher.group(2)) ? "§8§m" : "§a") : result;
    });
    return updatable;
  }

  public String getName() {
    return name;
  }

  public int getPrice(int tier) {
    return price[tier];
  }


  public int getMaxTier() {
    return price.length;
  }

  public void setCoinTrade(BuyEnums coinTrade) {
    this.coinTrade = coinTrade;
  }

  public BuyEnums getCoinTrade() {
    return coinTrade;
  }
}
