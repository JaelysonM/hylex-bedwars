package com.uzm.hylex.bedwars.proxy.balancer;

import com.uzm.hylex.bedwars.proxy.balancer.elements.LoadBalancerObject;

import java.util.*;

/**
 * @author Maxter
 */
public abstract class BaseBalancer<T extends LoadBalancerObject> implements LoadBalancer<T> {

  private Map<String, T> objects;
  protected List<T> nextObj;

  public BaseBalancer() {
    objects = new HashMap<>();
    nextObj = new ArrayList<>();
  }

  public BaseBalancer(Map<String, T> map) {
    addAll(map);
  }

  public void add(String id, T obj) {
    objects.put(id, obj);
    update();
  }

  public T get(String id) {
    return objects.get(id);
  }

  public void remove(String id) {
    objects.remove(id);
    update();
  }

  public void addAll(Map<String, T> map) {
    if (objects != null)
      objects.clear();
    objects = map;
    update();
  }

  public List<T> getList() {
    return nextObj;
  }

  public Set<String> keySet() {
    return objects.keySet();
  }

  public void update() {
    if (nextObj != null)
      nextObj.clear();
    nextObj = new ArrayList<>();
    nextObj.addAll(objects.values());
  }

  public abstract int getTotalNumber();
}
