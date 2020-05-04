package com.uzm.hylex.bedwars.proxy.balancer;

import com.uzm.hylex.bedwars.proxy.ServerItem;
import com.uzm.hylex.bedwars.proxy.balancer.elements.LoadBalancerObject;
import com.uzm.hylex.bedwars.proxy.balancer.elements.NumberConnection;

/**
 * @author Maxter
 */
public class Server implements LoadBalancerObject, NumberConnection {

  private String name;
  private int max;

  public Server(String name, int max) {
    this.name = name;
    this.max = max;
  }

  public String getName() {
    return this.name;
  }

  @Override
  public int getActualNumber() {
    return ServerItem.getServerCount(this.name);
  }



  @Override
  public int getMaxNumber() {
    return this.max;
  }

  @Override
  public boolean canBeSelected() {
    return this.getActualNumber() < this.max;
  }

  @Override
  public boolean isUp() {
    return ServerItem.isOnline(this.name);
  }
}
