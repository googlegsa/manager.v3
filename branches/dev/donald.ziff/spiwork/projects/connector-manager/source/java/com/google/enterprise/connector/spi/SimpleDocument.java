package com.google.enterprise.connector.spi;

import java.util.Map;
import java.util.Set;

public class SimpleDocument implements Document {

  private Map properties;

  public SimpleDocument(Map properties) {
    this.properties = properties;
  }

  public Property findProperty(String name) {
    return (Property) properties.get(name);
  }

  public Set getPropertyNames() {
    return properties.keySet();
  }
}
