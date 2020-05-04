package com.uzm.hylex.bedwars.proxy.balancer;

import com.uzm.hylex.bedwars.proxy.balancer.elements.LoadBalancerObject;

/**
 * @author Maxter
 */
public interface LoadBalancer<T extends LoadBalancerObject> {

  T next();
}
