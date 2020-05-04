package com.uzm.hylex.bedwars.proxy.balancer.type;

import com.uzm.hylex.bedwars.proxy.balancer.BaseBalancer;
import com.uzm.hylex.bedwars.proxy.balancer.elements.LoadBalancerObject;
import com.uzm.hylex.bedwars.proxy.balancer.elements.NumberConnection;

/**
 * @author Maxter
 */
public class MostConnection<T extends LoadBalancerObject & NumberConnection> extends BaseBalancer<T> {

  @Override
  public T next() {
    T obj = null;
    if (nextObj != null) {
      if (!nextObj.isEmpty()) {
        for (T item : nextObj) {
          if (!item.canBeSelected()) {
            continue;
          }
          if (!item.isUp()) {
            continue;
          }

          if (obj == null) {
            obj = item;
            continue;
          }

          if (obj.getActualNumber() < item.getActualNumber()) {
            obj = item;
          }
        }
      }
    }

    return obj;
  }

  @Override
  public int getTotalNumber() {
    int number = 0;
    for (T item : nextObj) {
      number += item.getActualNumber();
    }
    return number;
  }
}
