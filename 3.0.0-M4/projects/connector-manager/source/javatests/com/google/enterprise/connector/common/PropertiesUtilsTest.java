// Copyright 2011 Google Inc.
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

package com.google.enterprise.connector.common;

import com.google.enterprise.connector.test.ConnectorTestUtils;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

public class PropertiesUtilsTest extends TestCase {
  private static final Logger logger =
      Logger.getLogger(PropertiesUtilsTest.class.getName());

  private static final String TEST_DIR_NAME =
      "testdata/tmp/PropertiesUtilsTests";
  private static final String NAME = "test.properties";
  private final File baseDirectory  = new File(TEST_DIR_NAME);

  private static final String PROP1 = "prop1";
  private static final String PROP1_VALUE = "value1";
  private static final String PROP2 = "prop2";
  private static final String PROP2_VALUE = "value2";
  private static final String COMMENT = "Comment";
  private static final String MULTILINE_COMMENT = COMMENT + "\n More Comment";
  private static final String PROPERTIES =
      "# " + COMMENT + "\n"
      + PROP1 + " = " + PROP1_VALUE + "\n"
      + PROP2 + " = " + PROP2_VALUE + "\n";

  private Properties expected;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    ConnectorTestUtils.deleteAllFiles(baseDirectory);
    assertTrue(ConnectorTestUtils.mkdirs(baseDirectory));
    expected = new Properties();
    expected.setProperty(PROP1, PROP1_VALUE);
    expected.setProperty(PROP2, PROP2_VALUE);
  }

  @Override
  protected void tearDown() throws Exception {
    try {
      ConnectorTestUtils.deleteAllFiles(baseDirectory);
    } finally {
      super.tearDown();
    }
  }

  /** Test load from null string. */
  public void testLoadFromNullString() throws Exception {
    assertNull(PropertiesUtils.loadFromString(null));
  }

  /** Test load from empty string. */
  public void testLoadFromEmptyString() throws Exception {
    Properties props = PropertiesUtils.loadFromString("");
    assertNotNull(props);
    assertTrue(props.isEmpty());
  }

  /** Test load from string. */
  public void testLoadFromString() throws Exception {
    Properties props = PropertiesUtils.loadFromString(PROPERTIES);
    compareProperties(expected, props);
  }

  /** Test store to string. */
  public void testStoreToString() throws Exception {
    String output = PropertiesUtils.storeToString(expected, COMMENT);
    assertTrue(output.startsWith("#" + COMMENT));
    assertTrue(output.contains(PROP1 + "=" + PROP1_VALUE));
    assertTrue(output.contains(PROP2 + "=" + PROP2_VALUE));
    Properties props = PropertiesUtils.loadFromString(output);
    compareProperties(expected, props);
  }

  /** Test load from null InputStream. */
  public void testLoadFromNullStream() throws Exception {
    assertNull(PropertiesUtils.loadProperties(null));
  }

  /** Test load from InputStream. */
  public void testLoadFromStream() throws Exception {
    ByteArrayInputStream bais = new ByteArrayInputStream(
        PROPERTIES.getBytes(PropertiesUtils.PROPERTIES_ENCODING));
    Properties props = PropertiesUtils.loadProperties(bais);
    compareProperties(expected, props);
  }

  /** Test load from Bad InputStream. */
  public void testLoadFromBadStream() throws Exception {
    Properties props;
    try {
      BadInputStream is = new BadInputStream();
      props = PropertiesUtils.loadProperties(is);
      fail("Expected PropertiesException");
    } catch (PropertiesException expected) {
      // Expected.
    }
  }

  /** Test store null properties to OutputStream. */
  public void testNullStoreToStream() throws Exception {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    assertEquals(0, baos.size());
    PropertiesUtils.storeProperties(null, baos, COMMENT);
    assertEquals(0, baos.size());
  }

  /** Test store empty properties to OutputStream. */
  public void testEmptyStoreToStream() throws Exception {
    Properties emptyProps = new Properties();
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    assertEquals(0, baos.size());
    PropertiesUtils.storeProperties(emptyProps, baos, MULTILINE_COMMENT);
    assertTrue(baos.size() > 0);
    String output = baos.toString(PropertiesUtils.PROPERTIES_ENCODING);
    // Make sure multi-line comments get "commented out".
    assertTrue(output.startsWith("#" + COMMENT));
    assertTrue(output.contains("# More Comment"));
    compareProperties(emptyProps, PropertiesUtils.loadFromString(output));
  }

  /** Test store properties to OutputStream. */
  public void testStoreToStream() throws Exception {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    assertEquals(0, baos.size());
    PropertiesUtils.storeProperties(expected, baos, COMMENT);
    assertTrue(baos.size() > 0);
    String output = baos.toString(PropertiesUtils.PROPERTIES_ENCODING);
    assertTrue(output.startsWith("#" + COMMENT));
    assertTrue(output.contains(PROP1 + "=" + PROP1_VALUE));
    assertTrue(output.contains(PROP2 + "=" + PROP2_VALUE));
    compareProperties(expected, PropertiesUtils.loadFromString(output));
  }

  /** Test store to Bad OutputStream. */
  public void testStoreToBadStream() throws Exception {
    try {
      BadOutputStream os = new BadOutputStream();
      PropertiesUtils.storeProperties(new Properties(), os, COMMENT);
      fail("Expected PropertiesException");
    } catch (PropertiesException expected) {
      // Expected.
    }
  }

  /** Test load from non-existent file. */
  public void testLoadFromFileNoExist() throws Exception {
    try {
      Properties props = PropertiesUtils.loadFromFile(
          new File(baseDirectory, "nonExistent.properties"));
      fail("Expected PropertiesException");
    } catch (PropertiesException expected) {
      // Expected.
    }
  }

  /** Test load from file. */
  public void testLoadFromFile() throws Exception {
    File propFile = new File(baseDirectory, NAME);
    assertFalse(propFile.exists());
    FileOutputStream fos = new FileOutputStream(propFile);
    fos.write(PROPERTIES.getBytes(PropertiesUtils.PROPERTIES_ENCODING));
    fos.close();
    assertTrue(propFile.exists());

    Properties props = PropertiesUtils.loadFromFile(propFile);
    compareProperties(expected, props);
  }

  /** Test store to non-existent directory. */
  public void testStoreToDirNoExist() throws Exception {
    File propFile = new File(new File(baseDirectory, "nonExistentDir"), NAME);
    assertFalse(propFile.exists());
    try {
      PropertiesUtils.storeToFile(expected, propFile, COMMENT);
      fail("Expected PropertiesException");
    } catch (PropertiesException expected) {
      // Expected.
    }
  }

  /** Test store to file. */
  public void testStoreToFile() throws Exception {
    File propFile = new File(baseDirectory, NAME);
    assertFalse(propFile.exists());
    PropertiesUtils.storeToFile(expected, propFile, null);
    assertTrue(propFile.exists());

    Properties props = PropertiesUtils.loadFromFile(propFile);
    compareProperties(expected, props);
  }

  /** Test copy Properties. */
  public void testCopy() throws Exception {
    Properties emptyProps = new Properties();
    compareProperties(emptyProps, PropertiesUtils.copy(null));
    compareProperties(emptyProps, PropertiesUtils.copy(emptyProps));

    Properties copyProps = PropertiesUtils.copy(expected);
    compareProperties(expected, copyProps);

    // Make sure it is a deep copy.
    copyProps.setProperty(PROP1, "foo");
    assertEquals("foo", copyProps.getProperty(PROP1));
    assertEquals(PROP1_VALUE, expected.getProperty(PROP1));
  }

  /** Test toMap and fromMap. */
  public void testToFromMap() throws Exception {
    assertNull(PropertiesUtils.toMap(null));
    assertNull(PropertiesUtils.fromMap(null));

    Map<String, String> propMap = PropertiesUtils.toMap(expected);
    assertEquals(PROP1_VALUE, propMap.get(PROP1));
    assertEquals(PROP2_VALUE, propMap.get(PROP2));
    compareProperties(expected, PropertiesUtils.fromMap(propMap));
  }

  /** Test Properties Version. */
  public void testVersion() throws Exception {
    Properties props = PropertiesUtils.copy(expected);
    // No explicit version.
    assertEquals(0, PropertiesUtils.getPropertiesVersion(props));

    // Test real version.
    PropertiesUtils.stampPropertiesVersion(props);
    assertEquals(PropertiesUtils.GOOGLE_PROPERTIES_VERSION_NUMBER,
                 PropertiesUtils.getPropertiesVersion(props));

    // Test newer version.
    props.put(PropertiesUtils.GOOGLE_PROPERTIES_VERSION,
              Integer.toString(PropertiesUtils.GOOGLE_PROPERTIES_VERSION_NUMBER
                               + 100000));
    assertEquals(PropertiesUtils.GOOGLE_PROPERTIES_VERSION_NUMBER + 100000,
                 PropertiesUtils.getPropertiesVersion(props));

    // Test invalid version.
    props.put(PropertiesUtils.GOOGLE_PROPERTIES_VERSION, "bad_version");
    assertEquals(0, PropertiesUtils.getPropertiesVersion(props));
  }

  /** Test encrypt/decrypt sensitive properties. */
  public void testSensitiveProperties() throws Exception {
    Properties props = PropertiesUtils.copy(expected);
    props.setProperty("password", "foo");
    props.setProperty("otherPassword", "bar");

    PropertiesUtils.encryptSensitiveProperties(props);
    // Password properties should be encrypted.
    assertFalse("foo".equals(props.getProperty("password")));
    assertFalse("bar".equals(props.getProperty("otherPassword")));
    // Non-Password properties should be unmodified.
    assertEquals(PROP1_VALUE, props.getProperty(PROP1));
    assertEquals(PROP2_VALUE, props.getProperty(PROP2));

    PropertiesUtils.decryptSensitiveProperties(props);
    // Password properties should be restored.
    assertEquals("foo", props.getProperty("password"));
    assertEquals("bar", props.getProperty("otherPassword"));
    // Non-Password properties should be unmodified.
    assertEquals(PROP1_VALUE, props.getProperty(PROP1));
    assertEquals(PROP2_VALUE, props.getProperty(PROP2));
  }

  /** Test encrypt/decrypt sensitive properties during load/store. */
  public void testLoadStoreSensitiveProperties() throws Exception {
    Properties props = PropertiesUtils.copy(expected);
    props.setProperty("password", "foo");
    props.setProperty("otherPassword", "bar");

    String output = PropertiesUtils.storeToString(props, COMMENT);
    // Password properties should be encrypted.
    assertFalse(output.contains("foo"));
    assertFalse(output.contains("bar"));

    // Passwords should be restored when read back in.
    compareProperties(props, PropertiesUtils.loadFromString(output));
  }

  /** Compare the supplied propertied against expected properties. */
  private void compareProperties(Properties expected, Properties props)
      throws Exception {
    assertNotNull(props);
    ConnectorTestUtils.compareMaps(expected, props);
  }

  /** An InputStream that throws IOException. */
  private class BadInputStream extends InputStream {
    public int read() throws IOException {
      throw new IOException("test");
    }
  }

  /** An OutputStream that throws IOException. */
  private class BadOutputStream extends OutputStream {
    public void write(int b) throws IOException {
      throw new IOException("test");
    }
  }
}