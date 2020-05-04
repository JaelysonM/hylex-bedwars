package com.uzm.hylex.bedwars.arena.shop;

import com.google.common.collect.ImmutableList;
import com.uzm.hylex.bedwars.arena.improvements.Trap;

import java.util.ArrayList;
import java.util.List;

public class Shop {

  private static List<ShopCategory> CATEGORIES = new ArrayList<>();

  public static void setupShop() {
    CATEGORIES.add(new ShopCategory("blocks"));
    CATEGORIES.add(new ShopCategory("melee"));
    CATEGORIES.add(new ShopCategory("armors"));
    CATEGORIES.add(new ShopCategory("tools"));
    CATEGORIES.add(new ShopCategory("ranged"));
    CATEGORIES.add(new ShopCategory("potions"));
    CATEGORIES.add(new ShopCategory("utility"));
    Trap.setupTraps();
  }

  public static int getCategoryId(ShopCategory search) {
    if (search == null) {
      return 0;
    }

    int id = 1;
    for (ShopCategory category : listCategories()) {
      if (category.equals(search)) {
        break;
      }

      id++;
    }

    return id;
  }

  public static ShopCategory getCategoryById(int id) {
    int find = 1;
    for (ShopCategory category : listCategories()) {
      if (find == id) {
        return category;
      }

      find++;
    }

    return null;
  }

  public static List<ShopCategory> listCategories() {
    return ImmutableList.copyOf(CATEGORIES);
  }
}
