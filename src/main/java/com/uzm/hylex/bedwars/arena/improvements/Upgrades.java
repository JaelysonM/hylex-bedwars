package com.uzm.hylex.bedwars.arena.improvements;

import com.uzm.hylex.bedwars.arena.enums.BuyEnums;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Upgrades {

  private Pattern pattern = Pattern.compile("([<])([0-9])([>])");
  private Pattern patternTier = Pattern.compile("([{])([0-9])([}])");

  private UpgradeType type;
  private List<String> description;
  private List<Integer> prices;
  private BuyEnums coinTrade;

  public Upgrades(UpgradeType type) {
    this.type = type;
  }

  public void setDescription(List<String> description) {
    this.description = description;
    this.description.replaceAll(result -> {
      result = result.replace("&", "§");
      Matcher matcher = pattern.matcher(result);
      if (matcher.find()) {
        int index = Integer.parseInt(matcher.group(2)) - 1;
        int value = index < prices.size() ? prices.get(index) : 0;
        result = result.replace("&", "§").replace(matcher.group(), String.valueOf(value));
      }

      return result;
    });
  }

  public void setPrices(List<Integer> prices) {
    this.prices = prices;
  }

  public UpgradeType getType() {
    return this.type;
  }

  public List<String> getUpdatableDescription(int currentTier, boolean canBuy) {
    List<String> updatable = new ArrayList<>(this.description);
    updatable.replaceAll(result -> {
      Matcher matcher = patternTier.matcher(result);
      return matcher.find() ? result.replace(matcher.group(), currentTier >= Integer.parseInt(matcher.group(2)) ? "§m" : canBuy ? "" : "") : result;
    });
    return updatable;
  }


  public int getPrice(int tier) {
    return this.prices.get(tier - 1);
  }

  public int getMaxTier() {
    return this.prices.size();
  }

  public void setCoinTrade(BuyEnums coinTrade) {
    this.coinTrade = coinTrade;
  }

  public BuyEnums getCoinTrade() {
    return coinTrade;
  }
}
