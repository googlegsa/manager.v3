// Copyright 2006-2008 Google Inc.  All Rights Reserved.
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

import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SpiConstants;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.jcr.PropertyIterator;

public class JcrDocument implements Document {

  private javax.jcr.Node node;
  private static Map aliasMap;
  private Map aliasedPropertyNames = null;
  private Property property;

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

  private PropertyIterator getJCRProperties() throws RepositoryException {
    PropertyIterator propertyIterator = null;
    try {
      propertyIterator = node.getProperties();
    } catch (javax.jcr.RepositoryException e) {
      throw new RepositoryException(e);
    }
    return propertyIterator;
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
    // value, if they have the same name. For example, if a node has both
    // "google:docid" (PROPNAME_DOCID) and "jcr:uuid" (normal jcr id) then
    // the explicitly supplied "google:docid" beats out the aliased
    for (Iterator i = originalNames.iterator(); i.hasNext();) {
      String name = (String) i.next();
      aliasedPropertyNames.put(name, name);
    }
  }

  public JcrDocument(javax.jcr.Node n) {
    this.node = n;
    this.property = null;
  }

  public Property findProperty(String name) throws RepositoryException {
    setupAliases();
    // first, we check whether there is a JCR property with this name
    String originalName = (String) aliasedPropertyNames.get(name);
    if (originalName == null) {
      return null;
    }
    // now we believe there is a JCR property with this name,
    // so we construct the SPI property -
    // if this is the second time someone hs asked for this property, we
    // re-construct it
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
    property = new JcrProperty(jcrProperty, name);
    return property;
  }

  public Set getPropertyNames() throws RepositoryException {
    setupAliases();
    return aliasedPropertyNames.keySet();
  }

}
