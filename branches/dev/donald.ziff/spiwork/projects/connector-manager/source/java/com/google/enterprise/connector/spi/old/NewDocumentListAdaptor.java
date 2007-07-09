package com.google.enterprise.connector.spi.old;

import com.google.enterprise.connector.spi.DocumentList;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Value;

import java.util.Iterator;

public class NewDocumentListAdaptor implements DocumentList {

  private com.google.enterprise.connector.spi.old.TraversalManager oldTraversalManager = null;
  private PropertyMapList propertyMapList = null;
  private Iterator propertyMapListIterator = null;
  private PropertyMap propertyMap = null;
  private Property property = null;
  private Iterator propertyIterator = null;
  private Iterator valueIterator = null;
  private com.google.enterprise.connector.spi.old.Value oldValue = null;

  public NewDocumentListAdaptor(
      PropertyMapList propertyMapList,
      com.google.enterprise.connector.spi.old.TraversalManager oldTraversalManager) {
    this.propertyMapList = propertyMapList;
    this.oldTraversalManager = oldTraversalManager;
  }

  /**
   * This constructor is only intended for Pusher tests
   * 
   * @param propertyMap
   */
  public NewDocumentListAdaptor(PropertyMap propertyMap) {
    this.propertyMap = propertyMap;
  }

  public boolean nextDocument() {
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
      propertyIterator = null;
    }
    return hasNext;
  }

  public String checkpoint() throws RepositoryException {
    String result = null;
    result = oldTraversalManager.checkpoint(propertyMap);
    return result;
  }

  public boolean findProperty(String name) {
    property = null;
    valueIterator = null;
    try {
      property = propertyMap.getProperty(name);
    } catch (RepositoryException e) {
      throw new IllegalStateException();
    }
    return (property != null);
  }

  public boolean nextProperty() {
    property = null;
    valueIterator = null;
    if (propertyIterator == null) {
      try {
        propertyIterator = propertyMap.getProperties();
      } catch (RepositoryException e) {
        throw new IllegalStateException();
      }
    }
    boolean hasNext = propertyIterator.hasNext();
    if (hasNext) {
      property = (Property) propertyIterator.next();
    } else {
      propertyIterator = null;
    }
    return hasNext;
  }

  public String getPropertyName() throws RepositoryException {
    if (property == null) {
      return null;
    }
    return property.getName();
  }

  public boolean nextValue() {
    if (valueIterator == null) {
      try {
        valueIterator = property.getValues();
      } catch (RepositoryException e) {
        throw new IllegalStateException();
      }
    }
    boolean hasNext = valueIterator.hasNext();
    if (hasNext) {
      oldValue = (com.google.enterprise.connector.spi.old.Value) valueIterator
          .next();
    } else {
      valueIterator = null;
    }
    return hasNext;
  }

  public Value getValue() throws RepositoryException {
    if (oldValue == null) {
      return null;
    }
    return newValueAdaptor(oldValue);
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
