package com.google.enterprise.connector.spi;

import java.util.Iterator;
import java.util.Map;

public class SimpleDocument implements Document {

  private Map properties;
  private Iterator iterator;
  private Property property;

  public SimpleDocument(Map properties) {
    this.properties = properties;
    this.iterator = null;
    this.property = null;
  }

  public boolean findProperty(String name) {
    if (iterator != null) {
      throw new IllegalStateException();
    }
    property = (Property) properties.get(name);
    return (property != null);
  }

  public Property getProperty() {
    return property;
  }

  public boolean nextProperty() {
    if (iterator == null) {
      iterator = properties.values().iterator();
    }
    boolean hasNext = iterator.hasNext();
    if (hasNext) {
      property = (Property) iterator.next();
    } 
    return hasNext;
  }

}
