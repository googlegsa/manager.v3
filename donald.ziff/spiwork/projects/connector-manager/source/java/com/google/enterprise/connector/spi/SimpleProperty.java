package com.google.enterprise.connector.spi;

import java.util.Iterator;
import java.util.List;

public class SimpleProperty implements Property {
  
  String name;
  List values;
  Iterator iterator;
  Value value;
  
  public SimpleProperty(String name, List values) {
    this.name = name;
    this.values = values;
    this.iterator = null;
    this.value = null;
  }

  public String getPropertyName() {
    return name;
  }

  public Value getValue() {
    if (value == null) {
      throw new IllegalStateException();
    }
    return value;
  }

  public boolean nextValue() {
    if (iterator == null) {
      iterator = values.iterator();
    }
    boolean hasNext = iterator.hasNext();
    if (hasNext) {
      value = (Value) iterator.next();
    } else {
      value = null;
    }
    return hasNext;
  }

}
