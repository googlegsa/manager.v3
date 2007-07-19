package com.google.enterprise.connector.jcradaptor;

import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.Value;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.jcr.PropertyIterator;

public class SpiDocumentFromJcr implements Document {

  private javax.jcr.Node node;
  private static Map aliasMap;
  private Map aliasedPropertyNames = null;
  private Map aliasedPropertyMap = new TreeMap();

  private Property property;
  private Iterator propertyIterator;

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

  public SpiDocumentFromJcr(javax.jcr.Node n) {
    this.node = n;
    this.property = null;
  }

  public String checkpoint() throws RepositoryException {
    String uuid = fetchAndVerifyValueForCheckpoint(
        SpiConstants.PROPNAME_DOCID).toString();
    String dateString = fetchAndVerifyValueForCheckpoint(
        SpiConstants.PROPNAME_LASTMODIFIED).toString();
    String result = null;
    try {
      JSONObject jo = new JSONObject();
      jo.put("uuid", uuid);
      jo.put("lastModified", dateString);
      result = jo.toString();
    } catch (JSONException e) {
      throw new RepositoryException("Unexpected JSON problem", e);
    }
    return result;
  }

  private Value fetchAndVerifyValueForCheckpoint(String pName)
      throws RepositoryException {
    if (node == null) {
      throw new IllegalStateException();
    }
    if (!findProperty(pName)) {
      throw new IllegalArgumentException("checkpoint must have a " + pName
          + " property");
    }
    if (!property.nextValue()) {
      throw new IllegalArgumentException("checkpoint must have a non-empty"
          + pName + " property");
    }
    Value value = property.getValue();
    if (value == null) {
      throw new IllegalArgumentException("checkpoint " + pName
          + " property must have a non-null value");
    }
    return value;
  }

  public boolean findProperty(String name) throws RepositoryException {
    setupAliases();
    // we use a lazily constructed map - if there's an entry, we're done
    property = (Property) aliasedPropertyMap.get(name);
    if (property != null) {
      return true;
    }
    // otherwise, we create an entry
    // first, we check whether there is a JCR property with this name
    String originalName = (String) aliasedPropertyNames.get(name);
    if (originalName == null) {
      return false;
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
    return true;
  }

  public boolean nextProperty() throws RepositoryException {
    setupAliases();
    propertyIterator = aliasedPropertyNames.keySet().iterator();
    boolean hasNext = propertyIterator.hasNext();
    if (hasNext) {
      String name = (String) propertyIterator.next();
      findProperty(name);
    }
    return hasNext;
  }

  public String getPropertyName() throws RepositoryException {
    if (property == null) {
      throw new IllegalStateException();
    }
    return property.getPropertyName();
  }

  public Value getValue() throws RepositoryException {
    if (property == null) {
      throw new IllegalStateException();
    }
    return property.getValue();
  }

  public boolean nextValue() throws RepositoryException {
    if (property == null) {
      throw new IllegalStateException();
    }
    return property.nextValue();
  }

  public Property getProperty() {
    return property;
  }

}
