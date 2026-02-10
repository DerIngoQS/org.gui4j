package org.gui4j.core;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class Gui4jTypeManager implements Serializable {
  private static final long serialVersionUID = 1L;
  private final Map<Class<?>, Object> typeMap;

  public Gui4jTypeManager() {
    typeMap = new HashMap<Class<?>, Object>();
  }

  public Gui4jTypeManager(Map<? extends Class<?>, ?> m) {
    typeMap = new HashMap<Class<?>, Object>();
    add(m);
  }

  public int size() {
    return typeMap.size();
  }

  public void add(Map<? extends Class<?>, ?> map) {
    for (Iterator<? extends Map.Entry<? extends Class<?>, ?>> it = map.entrySet().iterator();
        it.hasNext(); ) {
      Map.Entry<? extends Class<?>, ?> entry = it.next();
      Class<?> c = entry.getKey();
      Object instance = entry.getValue();
      add(c, instance);
    }
  }

  public void add(Class<?> type, Object instance) {
    assert !typeMap.containsKey(type);
    // XXX: JS->Kay ;-) ---  Ordentliche Fehlermeldung erzeugen
    typeMap.put(type, instance);
  }

  public Object getExact(Class<?> classType) {
    return typeMap.get(classType);
  }

  public Object get(Class<?> classType) {
    Class<?> lastClass = null;
    Object lastObject = null;
    for (Iterator<Map.Entry<Class<?>, Object>> it = typeMap.entrySet().iterator(); it.hasNext(); ) {
      Map.Entry<Class<?>, Object> entry = it.next();
      Class<?> c = entry.getKey();
      Object object = entry.getValue();
      if (c.isAssignableFrom(classType)) {
        if (lastClass == null || lastClass.isAssignableFrom(c)) {
          lastClass = c;
          lastObject = object;
        }
      }
    }
    return lastObject;
  }
}
