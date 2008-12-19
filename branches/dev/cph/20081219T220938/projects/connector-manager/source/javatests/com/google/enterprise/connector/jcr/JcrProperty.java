package com.google.enterprise.connector.jcr;

import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Value;

import java.util.Arrays;
import java.util.Iterator;

import javax.jcr.ValueFormatException;

public class JcrProperty implements Property {

  private javax.jcr.Property property;
  private String name;
  private String alias;
  private Value value = null;
  private Iterator iterator;

  
  public JcrProperty(javax.jcr.Property p) {
    if (p == null) {
      throw new IllegalArgumentException();
    }
    this.property = p;
    this.name = null;
    this.alias = null;
  }

  public JcrProperty(javax.jcr.Property p, String alias) {
    if (p == null) {
      throw new IllegalArgumentException();
    }
    this.property = p;
    this.name = null;
    this.alias = alias;
  }

  public String getPropertyName() throws RepositoryException {
    if (alias != null) {
      return alias;
    }
    if (name == null) {
      try {
        this.name = property.getName();
      } catch (javax.jcr.RepositoryException e) {
        throw new RepositoryException(e);
      }
    }
    return name;
  }

  private static Value toSpiValue(javax.jcr.Value jcrValue)
      throws RepositoryException {
    int jcrType = jcrValue.getType();
    try {
      switch (jcrType) {
      case javax.jcr.PropertyType.BINARY:
        return Value.getBinaryValue(jcrValue.getStream());
      case javax.jcr.PropertyType.BOOLEAN:
        return Value.getBooleanValue(jcrValue.getBoolean());
      case javax.jcr.PropertyType.LONG:
        return Value.getLongValue(jcrValue.getLong());
      case javax.jcr.PropertyType.DOUBLE:
        return Value.getDoubleValue(jcrValue.getDouble());
      case javax.jcr.PropertyType.DATE:
        return Value.getDateValue(jcrValue.getDate());
      case javax.jcr.PropertyType.NAME:
      case javax.jcr.PropertyType.PATH:
      case javax.jcr.PropertyType.REFERENCE:
      case javax.jcr.PropertyType.UNDEFINED:
      case javax.jcr.PropertyType.STRING:
      default:
        return Value.getStringValue(jcrValue.getString());
      }
    } catch (IllegalStateException e) {
      throw new RepositoryException(e);
    } catch (ValueFormatException e) {
      throw new RepositoryException(e);
    } catch (javax.jcr.RepositoryException e) {
      throw new RepositoryException(e);
    }
  }

  public Value nextValue() throws RepositoryException {
    if (iterator == null) {
      try {
        iterator = Arrays.asList(property.getValues()).iterator();
      } catch (ValueFormatException e) {
        throw new RepositoryException(e);
      } catch (javax.jcr.RepositoryException e) {
        throw new RepositoryException(e);
      }
    }
    boolean hasNext = iterator.hasNext();
    if (hasNext) {
      value = toSpiValue((javax.jcr.Value) iterator.next());
    } else {
      value = null;
    }
    return value;
  }

}
