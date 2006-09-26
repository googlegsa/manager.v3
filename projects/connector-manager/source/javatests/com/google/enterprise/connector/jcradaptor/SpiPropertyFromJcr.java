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

import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Value;

import java.util.Iterator;

/**
 * Google SPI Property adaptor using JCR property. Part of an implementation of
 * the complete Google connector SPI using JCR. This class is a fairly
 * delegation of similar calls to the appropriate JCR calls.
 */
public class SpiPropertyFromJcr implements Property {

  private javax.jcr.Property property;
  private final String name;

  public SpiPropertyFromJcr(javax.jcr.Property p) {
    if (p == null) {
      throw new IllegalArgumentException();
    }
    this.property = p;
    try {
      this.name = property.getName();
    } catch (javax.jcr.RepositoryException e) {
      throw new IllegalArgumentException();
    }
  }

  public SpiPropertyFromJcr(javax.jcr.Property p, String alias) {
    if (p == null) {
      throw new IllegalArgumentException();
    }
    this.property = p;
    this.name = alias;
  }

  /**
   * Returns the name of the property.
   * 
   * @return The String name
   * @throws RepositoryException
   */
  public String getName() throws RepositoryException {
    return name;
  }

  /**
   * Returns the first value of the property or null if there is none.
   * 
   * @return The Value
   * @throws RepositoryException
   */
  public Value getValue() throws RepositoryException {
    Iterator i = getValues();
    if (i.hasNext()) {
      return (Value) i.next();
    }
    return null;
  }

  /**
   * Returns the values of the property as an iterator.
   * 
   * @return An Iterator of Values
   * @throws RepositoryException
   */
  public Iterator getValues() throws RepositoryException {
    javax.jcr.Value value = getSingleJcrValue();

    if (value != null) {
      return singleValueIterator(value);
    }

    // value is null - this is because a jcr ValueFormatException was
    // thrown earlier, which means that this is a multi-valued property

    javax.jcr.Value[] values = null;
    try {
      values = property.getValues();
    } catch (javax.jcr.ValueFormatException e) {
      // This shouldn't happen - because it would mean that the property is
      // single-valued - but we already tried getting its one value under that
      // assumption. So this is an internal consistency error
      throw new RepositoryException("Property is neither single-valued nor"
          + " multiple valued", e);
    } catch (javax.jcr.RepositoryException e) {
      throw new RepositoryException(e);
    }

    return multipleValueIterator(values);

  }

  private javax.jcr.Value getSingleJcrValue() throws RepositoryException {
    javax.jcr.Value value = null;
    try {
      value = property.getValue();
    } catch (javax.jcr.ValueFormatException e) {
      // this means that the property has multiple values
      // we can detect that later by testing if value == null
    } catch (javax.jcr.RepositoryException e) {
      throw new RepositoryException(e);
    }
    return value;
  }

  private Iterator multipleValueIterator(final javax.jcr.Value[] values) {
    return new Iterator() {

      private int index = 0;

      public boolean hasNext() {
        return index < values.length;
      }

      public Object next() {
        Object result = new SpiValueFromJcr(values[index]);
        index++;
        return result;
      }

      public void remove() {
        throw new UnsupportedOperationException();
      }

    };
  }

  private Iterator singleValueIterator(final javax.jcr.Value value) {
    return new Iterator() {

      private boolean hasnext = true;

      public boolean hasNext() {
        return hasnext;
      }

      public Object next() {
        hasnext = false;
        return new SpiValueFromJcr(value);
      }

      public void remove() {
        throw new UnsupportedOperationException();
      }

    };
  }
}
