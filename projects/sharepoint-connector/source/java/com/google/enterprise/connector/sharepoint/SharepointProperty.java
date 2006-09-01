package com.google.enterprise.connector.sharepoint;

import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Value;
import com.google.enterprise.connector.spi.ValueType;

import java.util.ArrayList;
import java.util.Iterator;

public class SharepointProperty implements Property {

  String name;
  SharepointValue value;
  ArrayList values = new ArrayList();

  public SharepointProperty(String name, Object val) {
    this.name = name;
    this.value = new SharepointValue(value);
  }

  public SharepointProperty(String name, Object val, ValueType type) {
    this.name = name;
    this.value = new SharepointValue(value, type);
  }

  public String getName() throws RepositoryException {
    // TODO Auto-generated method stub
    return name;
  }

  public Value getValue() throws RepositoryException {
    // TODO Auto-generated method stub
    return value;
  }

  public Iterator getValues() throws RepositoryException {
    // TODO Auto-generated method stub
    return values.iterator();
  }
}
