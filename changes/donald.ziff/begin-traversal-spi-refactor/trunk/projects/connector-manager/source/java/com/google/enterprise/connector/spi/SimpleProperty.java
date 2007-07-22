package com.google.enterprise.connector.spi;

import java.util.Iterator;
import java.util.List;

public class SimpleProperty implements Property {

  List values;
  Iterator iterator;

  public SimpleProperty(List values) {
    this.values = values;
    this.iterator = null;
  }

  public Value nextValue() {
    if (iterator == null) {
      iterator = values.iterator();
    }
    return (iterator.hasNext()) ? (Value) iterator.next() : null;
  }
}
