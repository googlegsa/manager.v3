// Copyright 2009 Google Inc. All Rights Reserved.

package com.google.enterprise.connector.persist;

import com.google.enterprise.connector.common.PropertiesException;
import com.google.enterprise.connector.common.PropertiesUtils;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SimpleProperty;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.Value;

import java.io.Externalizable;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Potential template class for a {@code Document} implementation that uses lazy
 * evaluation to get the the property values and can be persisted.
 */
public class LazyPersistDocument implements Document, Externalizable {
  private static final Logger LOGGER =
      Logger.getLogger(LazyPersistDocument.class.getName());
  public static final String PROPNAME_METADATA = "lazy.meta";

  private Map<String, List<Value>> documentValues;
  private File repo;
  private Properties repoProperties;

  public LazyPersistDocument() {
    // Public no-arg constructor for Externalizable.
  }

  public LazyPersistDocument(File repo, String docId) {
    this.repo = repo;
    initialize(docId);
  }

  private void initialize(String docId) {
    this.documentValues = new LinkedHashMap<String, List<Value>>(4);
    // Store the docId.
    List<Value> values = new LinkedList<Value>();
    values.add(Value.getStringValue(docId));
    documentValues.put(SpiConstants.PROPNAME_DOCID, values);
    // Fill the rest of the properties with lazy values.
    String key = SpiConstants.PROPNAME_CONTENT;
    values = new LinkedList<Value>();
    values.add(Value.getStringValue(key));
    documentValues.put(key, values);
    key = SpiConstants.PROPNAME_LASTMODIFIED;
    values = new LinkedList<Value>();
    values.add(Value.getStringValue(key));
    documentValues.put(key, values);
    key = PROPNAME_METADATA;
    values = new LinkedList<Value>();
    values.add(Value.getStringValue(key));
    documentValues.put(key, values);
  }

  public Property findProperty(String name) throws RepositoryException {
    List<Value> values = documentValues.get(name);
    if (values == null) {
      return null;
    } else if (SpiConstants.PROPNAME_DOCID.equals(name)) {
      LOGGER.info("found property (" + name + ", " + values + ")");
      return new SimpleProperty(values);
    } else {
      values = findLazyProperty(values);
      LOGGER.info("found property (" + name + ", " + values + ")");
      return new SimpleProperty(values);
    }
  }

  private List<Value> findLazyProperty(List<Value> values)
      throws RepositoryException {
    if (repoProperties == null) {
      // Load up properties from repo.
      try {
        repoProperties = PropertiesUtils.loadFromFile(repo);
      } catch (PropertiesException e) {
        LOGGER.log(Level.SEVERE, "Problem reading props from file", e);
        throw new RepositoryException(e);
      }
    }
    List<Value> repoValues = new LinkedList<Value>();
    for (Value value : values) {
      String repoPropKey = value.toString();
      if (SpiConstants.PROPNAME_LASTMODIFIED.equals(repoPropKey)) {
        repoValues.add(Value.getLongValue(repo.lastModified()));
      } else {
        String repoPropValue = repoProperties.getProperty(repoPropKey);
        repoValues.add(Value.getStringValue(repoPropValue));
      }
    }
    return repoValues;
  }

  public Set<String> getPropertyNames() {
    return documentValues.keySet();
  }

  public void readExternal(ObjectInput in) throws IOException {
    LOGGER.info("Reading object...");
    String repoPath = in.readUTF();
    String docId = in.readUTF();
    LOGGER.info("repoPath=" + repoPath + ", docId=" + docId);
    repo = new File(repoPath);
    initialize(docId);
  }

  public void writeExternal(ObjectOutput out) throws IOException {
    LOGGER.info("Writing object...");
    // Write out the file name and docId.
    String repoPath = repo.getCanonicalPath();
    List<Value> values = documentValues.get(SpiConstants.PROPNAME_DOCID);
    String docId = values.get(0).toString();
    LOGGER.info("repoPath=" + repoPath + ", docId=" + docId);
    out.writeUTF(repo.getCanonicalPath());
    out.writeUTF(docId);
    out.flush();
  }

  public File getRepo() {
    return repo;
  }

  public Properties getRepoProperties() {
    return repoProperties;
  }
}
