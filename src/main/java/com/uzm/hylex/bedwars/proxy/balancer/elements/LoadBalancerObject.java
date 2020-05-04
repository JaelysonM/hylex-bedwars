package com.uzm.hylex.bedwars.proxy.balancer.elements;

/**
 * @author Maxter
 */
public interface LoadBalancerObject {

  boolean canBeSelected();

  boolean isUp();
}
