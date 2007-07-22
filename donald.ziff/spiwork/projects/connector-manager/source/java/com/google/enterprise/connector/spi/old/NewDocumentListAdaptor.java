package com.google.enterprise.connector.spi.old;

import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.DocumentList;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Value;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

public class NewDocumentListAdaptor implements DocumentList, Document, Property {

  private com.google.enterprise.connector.spi.old.TraversalManager oldTraversalManager = null;
  private PropertyMapList propertyMapList = null;
  private Iterator propertyMapListIterator = null;
  private PropertyMap propertyMap = null;
  private com.google.enterprise.connector.spi.old.Property oldProperty = null;
  private Set propertyNames = null;
  private Iterator valueIterator = null;
  private com.google.enterprise.connector.spi.old.Value oldValue = null;

  public NewDocumentListAdaptor(
      PropertyMapList propertyMapList,
      com.google.enterprise.connector.spi.old.TraversalManager oldTraversalManager) {
    this.propertyMapList = propertyMapList;
    this.oldTraversalManager = oldTraversalManager;
  }

  public Document nextDocument() {
    if (propertyMapListIterator == null) {
      try {
        propertyMapListIterator = propertyMapList.iterator();
      } catch (RepositoryException e) {
        throw new IllegalStateException();
      }
    }
    boolean hasNext = propertyMapListIterator.hasNext();
    if (hasNext) {
      propertyMap = (PropertyMap) propertyMapListIterator.next();
      propertyNames = null;
      return this;
    }
    return null;
  }

  public String checkpoint() throws RepositoryException {
    String result = null;
    result = oldTraversalManager.checkpoint(propertyMap);
    return result;
  }

  public Property findProperty(String name) {
    oldProperty = null;
    valueIterator = null;
    try {
      oldProperty = propertyMap.getProperty(name);
    } catch (RepositoryException e) {
      throw new IllegalStateException();
    }
    if (oldProperty != null) {
      return this;
    }
    return null;
  }

  public Set getPropertyNames() throws RepositoryException {
    if (propertyNames == null) {
      propertyNames = new TreeSet();
      for (Iterator iter = propertyMap.getProperties(); iter.hasNext(); ) {
        com.google.enterprise.connector.spi.old.Property oldProp = 
          (com.google.enterprise.connector.spi.old.Property) iter.next();
        propertyNames.add(oldProp.getName());
      }
    }
    return propertyNames;
  }

  public Value nextValue() throws RepositoryException {
    if (valueIterator == null) {
      try {
        valueIterator = oldProperty.getValues();
      } catch (RepositoryException e) {
        throw new IllegalStateException();
      }
    }
    boolean hasNext = valueIterator.hasNext();
    if (hasNext) {
      oldValue = (com.google.enterprise.connector.spi.old.Value) valueIterator
          .next();
      return newValueAdaptor(oldValue); 
    } else {
      valueIterator = null;
      return null;
    }
  }

  private static Value newValueAdaptor(
      com.google.enterprise.connector.spi.old.Value value)
      throws RepositoryException {
    ValueType type = ValueType.STRING;
    type = value.getType();
    if (type == ValueType.BINARY) {
      return Value.getBinaryValue(value.getStream());
    } else if (type == ValueType.BOOLEAN) {
      return Value.getBooleanValue(value.getBoolean());
    } else if (type == ValueType.DATE) {
      return Value.getDateValue(value.getDate());
    } else if (type == ValueType.DOUBLE) {
      return Value.getDoubleValue(value.getDouble());
    } else if (type == ValueType.LONG) {
      return Value.getLongValue(value.getLong());
    } else if (type == ValueType.STRING) {
      return Value.getStringValue(value.getString());
    }
    return null;
  }

}
