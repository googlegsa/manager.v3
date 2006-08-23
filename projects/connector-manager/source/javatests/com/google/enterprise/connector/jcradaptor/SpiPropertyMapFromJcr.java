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
import com.google.enterprise.connector.spi.PropertyMap;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SpiConstants;

import java.util.Iterator;

import javax.jcr.PropertyIterator;

/**
 * Google SPI PropertyMap adaptor using JCR node.  Part of an implementation 
 * of the complete Google connector SPI using JCR.  This class may need to be 
 * revisited when look at queries that return things other than nodes.
 */
public class SpiPropertyMapFromJcr implements PropertyMap {

  private javax.jcr.Node node;

  public SpiPropertyMapFromJcr(javax.jcr.Node n) {
    this.node = n;
  }

  public Property getProperty(String name) throws RepositoryException {
    /**
     * Need to alias the special SpiConstants to corresponding 
     * JCR constants where applicable
     */
    if (SpiConstants.PROPNAME_DOCID.equals(name)) {
      name = "jcr:uuid";
    }
    if (SpiConstants.PROPNAME_CONTENT.equals(name)) {
      name = "jcr:content";
    }
    javax.jcr.Property property = null;
    try {
      property = node.getProperty(name);
    } catch (javax.jcr.PathNotFoundException e) {
      return null;
    } catch (javax.jcr.RepositoryException e) {
      throw new RepositoryException(e);
    }
    if (property == null) {
      return null;
    }
    return new SpiPropertyFromJcr(property);
  }

  public Iterator getProperties() throws RepositoryException {
    final PropertyIterator propertyIterator = getJCRProperties();
    return new Iterator() {

      public boolean hasNext() {
        return propertyIterator.hasNext();
      }

      public Object next() {
        return new SpiPropertyFromJcr(propertyIterator.nextProperty());
      }

      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  private PropertyIterator getJCRProperties() throws RepositoryException {
    PropertyIterator propertyIterator = null;
    try {
      propertyIterator = node.getProperties();
    } catch (javax.jcr.RepositoryException e) {
      throw new RepositoryException(e);
    }
    return propertyIterator;
  }
}
