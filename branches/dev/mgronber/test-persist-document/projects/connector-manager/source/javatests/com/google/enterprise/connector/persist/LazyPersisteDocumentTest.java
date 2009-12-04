// Copyright 2009 Google Inc. All Rights Reserved.

package com.google.enterprise.connector.persist;

import com.google.enterprise.connector.common.PropertiesException;
import com.google.enterprise.connector.common.PropertiesUtils;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.test.ConnectorTestUtils;

import junit.framework.TestCase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * Test of {@code LazyPersistDocument}.
 */
public class LazyPersisteDocumentTest extends TestCase {
  private String name;

  private LazyPersistDocument initialDoc;
  private File persistDir;
  private File repoFile;
  private File persistFile;
  private Set<String> expectedPropNames;
  private String contentValue;
  private String metadataValue;

  public LazyPersisteDocumentTest() {
    super();
  }

  public LazyPersisteDocumentTest(String name) {
    super(name);
    this.name = name;
  }

  @Override
  protected void setUp() throws Exception {
    persistDir = new File("testdata/tmp/persistdoc");
    assertTrue(persistDir.mkdirs());
    repoFile = new File(persistDir, name + "Repo");
    persistFile = new File(persistDir, name + "Persist");
    buildRepoFile(repoFile);
    initialDoc = new LazyPersistDocument(repoFile, name);
    String props[] = {
        SpiConstants.PROPNAME_DOCID,
        SpiConstants.PROPNAME_CONTENT,
        SpiConstants.PROPNAME_LASTMODIFIED,
        LazyPersistDocument.PROPNAME_METADATA
    };
    expectedPropNames = new HashSet<String>(Arrays.asList(props));
  }

  private void buildRepoFile(File testFile) throws PropertiesException {
    Properties props = new Properties();
    contentValue = "This is the content for " + name;
    props.setProperty(SpiConstants.PROPNAME_CONTENT, contentValue);
    metadataValue = "This is the meta data value for " + name; 
    props.setProperty(LazyPersistDocument.PROPNAME_METADATA, metadataValue);
    PropertiesUtils.storeToFile(props, testFile, "Props for " + name);
  }

  @Override
  protected void tearDown() throws Exception {
    assertTrue(ConnectorTestUtils.deleteAllFiles(persistDir));
  }

  public final void testBasicObject() throws RepositoryException {
    // This test just uses the object without persisting.
    checkDocumentProperties(initialDoc);
  }

  public final void testPersistingAndReading() throws Exception {
    // This test creates a persist object, persists it, then
    // reads it.
    LazyPersistDocument recoveredDoc = persistAndRecover(initialDoc);

    // Check to make sure the core fields in the recovered doc are there.
    assertEquals("Checking repo",
        initialDoc.getRepo().getCanonicalPath(),
        recoveredDoc.getRepo().getCanonicalPath());
    Set<String> propertyNames = recoveredDoc.getPropertyNames();
    assertTrue("Check property names",
        propertyNames.containsAll(expectedPropNames));
    assertEquals("Check docId",
        initialDoc.findProperty(SpiConstants.PROPNAME_DOCID)
            .nextValue().toString(),
        recoveredDoc.findProperty(SpiConstants.PROPNAME_DOCID)
            .nextValue().toString());
    assertNull("Checking repoProperties", recoveredDoc.getRepoProperties());
  }
  
  private LazyPersistDocument persistAndRecover(LazyPersistDocument initialDoc)
      throws Exception {
    FileOutputStream fos = new FileOutputStream(persistFile);
    ObjectOutputStream oos = new ObjectOutputStream(fos);
    oos.writeObject(initialDoc);
    oos.close();

    // Read in a new object from the persisted file.
    FileInputStream fis = new FileInputStream(persistFile);
    ObjectInputStream ois = new ObjectInputStream(fis);
    LazyPersistDocument recoveredDoc = (LazyPersistDocument) ois.readObject();
    ois.close();

    return recoveredDoc;
  }

  public final void testPersistAndUse() throws Exception {
    // This test persist the object then uses it after reading.
    LazyPersistDocument recoveredDoc = persistAndRecover(initialDoc);
    checkDocumentProperties(recoveredDoc);
  }

  private void checkDocumentProperties(Document document)
      throws RepositoryException {
    Set<String> propertyNames = document.getPropertyNames();
    assertTrue("Check property names",
        propertyNames.containsAll(expectedPropNames));

    // Now test each property value.
    String value = document.findProperty(SpiConstants.PROPNAME_DOCID)
        .nextValue().toString();
    assertEquals("Checking docId", name, value);
    value = document.findProperty(SpiConstants.PROPNAME_CONTENT)
        .nextValue().toString();
    assertEquals("Checking docId", contentValue, value);
    value = document.findProperty(LazyPersistDocument.PROPNAME_METADATA)
        .nextValue().toString();
    assertEquals("Checking docId", metadataValue, value);
    value = document.findProperty(SpiConstants.PROPNAME_LASTMODIFIED)
        .nextValue().toString();
    long longValue = Long.parseLong(value);
    assertEquals("Checking last modified", repoFile.lastModified(), longValue);
  }
}
