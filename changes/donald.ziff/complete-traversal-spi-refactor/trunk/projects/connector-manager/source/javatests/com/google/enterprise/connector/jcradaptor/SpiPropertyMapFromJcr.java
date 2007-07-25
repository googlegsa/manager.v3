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
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.old.Property;
import com.google.enterprise.connector.spi.old.PropertyMap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.jcr.PropertyIterator;

/**
 * Google SPI PropertyMap adaptor using JCR node. Part of an implementation of
 * the complete Google connector SPI using JCR. This class may need to be
 * revisited when look at queries that return things other than nodes.
 */
public class SpiPropertyMapFromJcr implements PropertyMap {

  private javax.jcr.Node node;
  private static Map aliasMap;
  private Map aliasedPropertyNames = null;
  private Map aliasedPropertyMap = new TreeMap();

  static {
    aliasMap = new HashMap();
    aliasMap.put(SpiConstants.PROPNAME_DOCID, "jcr:uuid");
    aliasMap.put(SpiConstants.PROPNAME_CONTENT, "jcr:content");
    aliasMap.put(SpiConstants.PROPNAME_LASTMODIFIED, "jcr:lastModified");
  }

  private Set getOriginalPropertyNames() throws RepositoryException {
    Set originalNames = new HashSet();
    final PropertyIterator propertyIterator = getJCRProperties();
    while (propertyIterator.hasNext()) {
      try {
        String name = propertyIterator.nextProperty().getName();
        originalNames.add(name);
      } catch (javax.jcr.RepositoryException e) {
        throw new RepositoryException(e);
      }
    }
    return originalNames;
  }

  private void setupAliases() throws RepositoryException {
    if (aliasedPropertyNames != null) {
      return;
    }
    aliasedPropertyNames = new TreeMap();
    Set originalNames = getOriginalPropertyNames();
    // set up aliases for the aliased names that actually appear in this node
    for (Iterator i = aliasMap.entrySet().iterator(); i.hasNext();) {
      Entry e = (Entry) i.next();
      String alias = (String) e.getKey();
      String name = (String) e.getValue();
      if (originalNames.contains(alias)) {
        // this one is explicitly supplied - we ignore the default
        aliasedPropertyNames.put(alias, alias);
        // remove the name we just aliased from the set of originals
        originalNames.remove(alias);
      } else if (originalNames.contains(name)) {
        aliasedPropertyNames.put(alias, name);
        // remove the name we just aliased from the set of originals
        originalNames.remove(name);
      }
    }
    // then alias all the remaining names to themselves
    // note: this has the effect of overriding any aliases with the native
    // value, if they have the same name.  For example, if a node has both
    // "google:docid" (PROPNAME_DOCID) and "jcr:uuid" (normal jcr id) then
    // the explicitly supplied "google:docid" beats out the aliased
    for (Iterator i = originalNames.iterator(); i.hasNext();) {
      String name = (String) i.next();
      aliasedPropertyNames.put(name, name);
    }
  }

  public SpiPropertyMapFromJcr(javax.jcr.Node n) {
    this.node = n;
  }

  public Property getProperty(String name) throws RepositoryException {
    setupAliases();
    // we use a lazily constructed map - if there's an entry, we're done
    Property property = (Property) aliasedPropertyMap.get(name);
    if (property != null) {
      return property;
    }
    // otherwise, we create an entry
    // first, we check whether there is a JCR property with this name
    String originalName = (String) aliasedPropertyNames.get(name);
    if (originalName == null) {
      return null;
    }
    // now we believe there is a JCR property with this name,
    // so we construct the SPI property and cache it
    javax.jcr.Property jcrProperty = null;
    try {
      jcrProperty = node.getProperty(originalName);
    } catch (javax.jcr.PathNotFoundException e) {
      throw new RepositoryException(e);
    } catch (javax.jcr.RepositoryException e) {
      throw new RepositoryException(e);
    }
    if (jcrProperty == null) {
      throw new RepositoryException();
    }
    property = new SpiPropertyFromJcr(jcrProperty, name);
    aliasedPropertyMap.put(name, property);
    return property;
  }

  public Iterator getProperties() throws RepositoryException {
    setupAliases();
    final Iterator propertyIterator = aliasedPropertyNames.keySet().iterator();
    return new Iterator() {

      public boolean hasNext() {
        return propertyIterator.hasNext();
      }

      public Object next() {
        String name = (String) propertyIterator.next();
        Object result;
        try {
          result = getProperty(name);
        } catch (RepositoryException e) {
          throw new RuntimeException(e);
        }
        return result;
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
