// Copyright (C) 2006 Google Inc.
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

package com.google.enterprise.connector.jcradaptor;

import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Value;
import com.google.enterprise.connector.spi.ValueType;

import java.io.InputStream;
import java.util.Calendar;

import javax.jcr.ValueFormatException;

/**
 * Implements connector.SPI.Value using a javax.jcr.Value 
 */
public class SpiValueFromJcr implements Value {

  javax.jcr.Value value = null;

  public SpiValueFromJcr(javax.jcr.Value value) {
    this.value = value;
  }

  public String getString() throws IllegalArgumentException,
      IllegalStateException, RepositoryException {
    String result = null;
    try {
      result = value.getString();
    } catch (ValueFormatException e) {
      throw new IllegalArgumentException();
    } catch (javax.jcr.RepositoryException e) {
      throw new RepositoryException(e);
    }
    return result;
  }

  public InputStream getStream() throws IllegalStateException,
      RepositoryException {
    InputStream result = null;
    try {
      result = value.getStream();
    } catch (javax.jcr.RepositoryException e) {
      throw new RepositoryException(e);
    }
    return result;
  }

  public long getLong() throws IllegalArgumentException, IllegalStateException,
      RepositoryException {
    long result = 0;
    try {
      result = value.getLong();
    } catch (ValueFormatException e) {
      throw new IllegalArgumentException();
    } catch (javax.jcr.RepositoryException e) {
      throw new RepositoryException(e);
    }
    return result;
  }

  public double getDouble() throws IllegalArgumentException,
      IllegalStateException, RepositoryException {
    double result = 0;
    try {
      result = value.getDouble();
    } catch (ValueFormatException e) {
      throw new IllegalArgumentException();
    } catch (javax.jcr.RepositoryException e) {
      throw new RepositoryException(e);
    }
    return result;
  }

  public Calendar getDate() throws IllegalArgumentException,
      IllegalStateException, RepositoryException {
    Calendar result = null;
    try {
      result = value.getDate();
    } catch (ValueFormatException e) {
      throw new IllegalArgumentException();
    } catch (javax.jcr.RepositoryException e) {
      throw new RepositoryException(e);
    }
    return result;
  }

  public boolean getBoolean() throws IllegalArgumentException,
      IllegalStateException, RepositoryException {
    boolean result = false;
    try {
      result = value.getBoolean();
    } catch (ValueFormatException e) {
      throw new IllegalArgumentException();
    } catch (javax.jcr.RepositoryException e) {
      throw new RepositoryException(e);
    }
    return result;
  }

  public ValueType getType() throws RepositoryException {
    int jcrType = value.getType();
    switch (jcrType) {
    case javax.jcr.PropertyType.STRING:
      return ValueType.STRING;
    case javax.jcr.PropertyType.BINARY:
      return ValueType.BINARY;
    case javax.jcr.PropertyType.BOOLEAN:
      return ValueType.BOOLEAN;
    case javax.jcr.PropertyType.LONG:
      return ValueType.LONG;
    case javax.jcr.PropertyType.DOUBLE:
      return ValueType.DOUBLE;
    case javax.jcr.PropertyType.DATE:
      return ValueType.DATE;
    case javax.jcr.PropertyType.NAME:
      return ValueType.STRING;
    case javax.jcr.PropertyType.PATH:
      return ValueType.STRING;
    case javax.jcr.PropertyType.REFERENCE:
      return ValueType.STRING;
    case javax.jcr.PropertyType.UNDEFINED:
      return ValueType.STRING;
    default:
      throw new IllegalArgumentException("unknown type: " + jcrType);
    }
  }
}
