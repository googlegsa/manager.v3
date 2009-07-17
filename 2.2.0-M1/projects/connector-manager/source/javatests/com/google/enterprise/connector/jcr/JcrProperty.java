// Copyright 2006-2009 Google Inc.  All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

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
  private Iterator <javax.jcr.Value> iterator;


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
    return (iterator.hasNext()) ? toSpiValue(iterator.next()) : null;
  }

}
