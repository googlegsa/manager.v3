// Copyright 2006 Google Inc.
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

package com.google.enterprise.connector.manager;

import com.google.common.base.Strings;
import com.google.enterprise.connector.instantiator.InstantiatorException;
import com.google.enterprise.connector.pusher.DocPusherFactory;
import com.google.enterprise.connector.pusher.GsaFeedConnection;
import com.google.enterprise.connector.test.ConnectorTestUtils;

import junit.framework.TestCase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.Properties;

/**
 * Tests for {@link Context#setConnectorManagerConfig(String, int)}.
 */
public class SetManagerConfigTest extends TestCase {
  private static final String APPLICATION_CONTEXT =
      "testdata/contextTests/SetManagerConfigTest.xml";

  private static final String TEST_PROPERTIES = "testContext.properties";
  private static final String TEST_DIR_NAME =
      "testdata/tmp/SetManagerConfigTest";
  private final File baseDirectory = new File(TEST_DIR_NAME);

  private File propFile = new File(baseDirectory, TEST_PROPERTIES);
  private Context context;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    ConnectorTestUtils.deleteAllFiles(baseDirectory);
    assertTrue(ConnectorTestUtils.mkdirs(baseDirectory));

    // Create an original set of properties.
    Properties props = new Properties();
    props.put(Context.GSA_FEED_HOST_PROPERTY_KEY, "fubar");
    props.put(Context.GSA_FEED_PORT_PROPERTY_KEY, "25");
    storeProperties(props, propFile, "Initial Props");

    Context.refresh();
    context = Context.getInstance();
    context.setStandaloneContext(APPLICATION_CONTEXT,
        Context.DEFAULT_JUNIT_COMMON_DIR_PATH);
    context.setFeeding(false);
  }

  @Override
  protected void tearDown() throws Exception {
    ConnectorTestUtils.deleteAllFiles(baseDirectory);
    super.tearDown();
  }

  public final void testGetConnectorManagerConfig() throws Exception {
    // Get the properties directly from the file.
    Properties props = loadProperties(propFile);
    String host = props.getProperty(Context.GSA_FEED_HOST_PROPERTY_KEY);
    int port = Integer.parseInt(props.getProperty(
        Context.GSA_FEED_PORT_PROPERTY_KEY, Context.GSA_FEED_PORT_DEFAULT));
    // Now get them from the Context.
    Properties managerProps = context.getConnectorManagerConfig();
    String ctxHost =
        managerProps.getProperty(Context.GSA_FEED_HOST_PROPERTY_KEY);
    int ctxPort = Integer.parseInt(managerProps.getProperty(
        Context.GSA_FEED_PORT_PROPERTY_KEY, Context.GSA_FEED_PORT_DEFAULT));
    assertEquals("Correct host from context", host, ctxHost);
    assertEquals("Correct port from context", port, ctxPort);

    // Update the manager config and pull again.
    context.setConnectorManagerConfig("", "shme", 14,
        Context.GSA_FEED_SECURE_PORT_INVALID, null);
    managerProps = context.getConnectorManagerConfig();
    ctxHost =
        managerProps.getProperty(Context.GSA_FEED_HOST_PROPERTY_KEY);
    ctxPort = Integer.parseInt(managerProps.getProperty(
        Context.GSA_FEED_PORT_PROPERTY_KEY, Context.GSA_FEED_PORT_DEFAULT));
    assertEquals("Correct host from context", "shme", ctxHost);
    assertEquals("Correct port from context", 14, ctxPort);
  }

  public final void testSetConnectorManagerConfig()
      throws InstantiatorException, IOException {
    Properties props = loadProperties(propFile);
    String host = props.getProperty(Context.GSA_FEED_HOST_PROPERTY_KEY);
    int port = Integer.parseInt(props.getProperty(
        Context.GSA_FEED_PORT_PROPERTY_KEY, Context.GSA_FEED_PORT_DEFAULT));
    String contentUrlPrefix = props.getProperty(
        Context.FEED_CONTENTURL_PREFIX_PROPERTY_KEY);

    assertEquals("fubar", host);
    assertEquals(25, port);
    assertNull(contentUrlPrefix);

    context.setConnectorManagerConfig("", "shme", 14,
        Context.GSA_FEED_SECURE_PORT_INVALID,
        "http://test:8080/connector-manager");
    String expectedContentUrlPrefix =
        "http://test:8080/connector-manager/getDocumentContent";
    verifyPropsValues("shme", 14, expectedContentUrlPrefix, propFile);

    context.setConnectorManagerConfig("", host, port,
        Context.GSA_FEED_SECURE_PORT_INVALID, null);
    verifyPropsValues(host, port, expectedContentUrlPrefix, propFile);

    // Verify that the contentUrlPrefix was also set in the DocPusherFactory.
    DocPusherFactory pusherFactory = (DocPusherFactory)
        context.getBean("PusherFactory", DocPusherFactory.class);
    assertEquals(expectedContentUrlPrefix, pusherFactory.getContentUrlPrefix());
  }

  public final void testIsManagerLocked() throws Exception {
    // Check initial states.
    assertTrue("Manager with missing prop is locked",
        context.getIsManagerLocked());
    setLockedProperty(Boolean.FALSE.toString());
    assertFalse("Manager with prop set to false", context.getIsManagerLocked());
    // Check state after updating the manager config.
    context.setConnectorManagerConfig("", "172.25.25.25", 19900,
        Context.GSA_FEED_SECURE_PORT_INVALID, null);
    assertTrue("Manager is locked after setting config",
        context.getIsManagerLocked());
  }

  private void setLockedProperty(String isLocked) throws IOException {
    updateProperty(Context.MANAGER_LOCKED_PROPERTY_KEY, isLocked,
        "Updating lock");
  }

  private void updateProperty(String key, String value, String comment)
      throws IOException {
    Properties props = loadProperties(propFile);
    props.setProperty(key, value);
    storeProperties(props, propFile, comment);
  }

  private void verifyPropsValues(String expectedHost, int expectedPort,
      String expectedContentUrlPrefix, File propFile) throws IOException {
    Properties props = loadProperties(propFile);
    String actualHost = props.getProperty(Context.GSA_FEED_HOST_PROPERTY_KEY);
    int actualPort = Integer.valueOf(props.getProperty(
        Context.GSA_FEED_PORT_PROPERTY_KEY, Context.GSA_FEED_PORT_DEFAULT));
    String actualContentUrlPrefix = props.getProperty(
        Context.FEED_CONTENTURL_PREFIX_PROPERTY_KEY);                                                      
    assertEquals(expectedHost, actualHost);
    assertEquals(expectedPort, actualPort);
    assertEquals(expectedContentUrlPrefix, actualContentUrlPrefix);
    String isManagerLocked =
        props.getProperty(Context.MANAGER_LOCKED_PROPERTY_KEY);
    assertEquals("Manager is locked", Boolean.TRUE.toString(), isManagerLocked);
  }

  /** The field default is true, to make the code safer to use. */
  public void testDefaultFieldValidateCertificate() throws Exception {
    GsaFeedConnection feeder = new GsaFeedConnection("", "fubar", 25, -1);
    assertTrue(feeder.getValidateCertificate());
  }

  /**
   * The property default is false, so that the code will run without
   * certificates, and so that we can change this default to true when
   * the installer installs the GSA certificate.
   */
  public void testDefaultPropertyValidateCertificate() throws Exception {
    GsaFeedConnection feeder = new GsaFeedConnection("", "fubar", 25, -1);
    context.setConnectorManagerConfig("", "shme", 14,
        Context.GSA_FEED_SECURE_PORT_INVALID, feeder, null);
    assertFalse(feeder.getValidateCertificate());
  }

  public void testSetPropertyValidateCertificate() throws Exception {
    updateProperty(Context.GSA_FEED_VALIDATE_CERTIFICATE_PROPERTY_KEY,
        "true", "Updating validateCertificate");
    GsaFeedConnection feeder = new GsaFeedConnection("", "fubar", 25, -1);
    context.setConnectorManagerConfig("", "shme", 14,
        Context.GSA_FEED_SECURE_PORT_INVALID, feeder, null);
    assertTrue(feeder.getValidateCertificate());
  }

  public void testSecurePortSet() throws Exception {
    // Get the property directly from the file.
    Properties props = loadProperties(propFile);
    String securePort = props.getProperty(
        Context.GSA_FEED_SECURE_PORT_PROPERTY_KEY);
    assertNull(securePort, securePort);

    // Now get them from the Context.
    Properties managerProps = context.getConnectorManagerConfig();
    String ctxSecurePort = managerProps.getProperty(
        Context.GSA_FEED_SECURE_PORT_PROPERTY_KEY);
    assertNull(ctxSecurePort, ctxSecurePort);

    // Update the manager config and pull again.
    setAndGetSecurePort(42, 42);
  }

  public void testSecurePortUnset() throws Exception {
    // Set the secure port.
    setAndGetSecurePort(42, 42);

    // Unset the secure port, which should not overwrite the configured port.
    // This is for backward compatibility when manually setting the securePort.
    setAndGetSecurePort(Context.GSA_FEED_SECURE_PORT_INVALID, 42);
  }

  public void testSecurePortReset() throws Exception {
    // Set the secure port.
    setAndGetSecurePort(42, 42);

    // Reset the secure port.
    setAndGetSecurePort(45, 45);
  }

  public void testSecurePortNeverSet() throws Exception {
    Properties managerProps = context.getConnectorManagerConfig();
    String ctxSecurePort = managerProps.getProperty(
        Context.GSA_FEED_SECURE_PORT_PROPERTY_KEY);
    assertNull(ctxSecurePort, ctxSecurePort);

    context.setConnectorManagerConfig("", "shme", 14,
        Context.GSA_FEED_SECURE_PORT_INVALID, null);
    managerProps = context.getConnectorManagerConfig();
    ctxSecurePort = managerProps.getProperty(
        Context.GSA_FEED_SECURE_PORT_PROPERTY_KEY);
    assertNull(ctxSecurePort, ctxSecurePort);
  }

  private void setAndGetSecurePort(int setPort, int expectedPort)
      throws MalformedURLException, InstantiatorException {
    GsaFeedConnection feeder = new GsaFeedConnection("", "fubar", 25, -1);
    context.setConnectorManagerConfig("", "shme", 14, setPort, feeder, null);
    Properties managerProps = context.getConnectorManagerConfig();
    String ctxSecurePort = managerProps.getProperty(
        Context.GSA_FEED_SECURE_PORT_PROPERTY_KEY);
    assertNotNull(ctxSecurePort);
    assertEquals("Correct secure port from context",
        expectedPort, Integer.parseInt(ctxSecurePort));
    assertEquals(expectedPort, feeder.getFeedUrl().getPort());
  }

  /** Represents the XML attributes from a GSA SetManagerConfig request. */
  private static class Gsa {
    public Gsa(String protocolAttribute, int portAttribute,
        int securePortAttribute) {
      this.protocolAttribute = protocolAttribute;
      this.portAttribute = portAttribute;
      this.securePortAttribute = securePortAttribute;
    }
    private final String protocolAttribute;
    private final int portAttribute;
    private final int securePortAttribute;
  }

  /** Creates a properties object with the non-null arguments */
  private Properties makeProps(String protocolProperty,
      String securePortProperty) {
    Properties props = new Properties();
    if (protocolProperty != null) {
      props.put(Context.GSA_FEED_PROTOCOL_PROPERTY_KEY, protocolProperty);
    }
    if (securePortProperty != null) {
      props.put(Context.GSA_FEED_SECURE_PORT_PROPERTY_KEY, securePortProperty);
    }
    return props;
  }

  /**
   * Tests variations in the inputs to setConnectorManager.
   *
   * @param gsa the GSA SetManagerConfig request
   * @param extraProps the stored properties
   * @param expectedProtocol the expected feed URL protocol
   * @param expectedPort the expected feed URL port
   */
  private void setManagerConfig(Gsa gsa, Properties extraProps,
      String expectedProtocol, int expectedPort)
      throws IOException, InstantiatorException {
    Properties props = loadProperties(propFile);
    props.putAll(extraProps);
    storeProperties(props, propFile, "Extra Props");

    // Make the call.
    GsaFeedConnection feeder = new GsaFeedConnection("", "fubar", 25, -1);
    context.setConnectorManagerConfig(gsa.protocolAttribute, "shme",
        gsa.portAttribute, gsa.securePortAttribute, feeder, null);

    // Check the feed URL.
    assertEquals(expectedProtocol, feeder.getFeedUrl().getProtocol());
    assertEquals(expectedPort, feeder.getFeedUrl().getPort());

    // Check for overwritten protocol and secure port properties.
    Properties managerProps = context.getConnectorManagerConfig();
    assertProperty(!Strings.isNullOrEmpty(gsa.protocolAttribute),
        gsa.protocolAttribute, Context.GSA_FEED_PROTOCOL_PROPERTY_KEY,
        extraProps, managerProps);
    assertProperty(gsa.securePortAttribute >= 0,
        String.valueOf(gsa.securePortAttribute),
        Context.GSA_FEED_SECURE_PORT_PROPERTY_KEY, extraProps, managerProps);
  }

  /**
   * Asserts that a property value is either the value from the GSA or
   * the pre-existing property value.
   *
   * @param fromGsa whether the GSA supplied a value
   * @param gsaValue the value from the GSA (fromGsa != (gsaValue != null))
   * @param key the property key
   * @param extraProps the pre-existing properties
   * @param managerProps the properties under test
  */
  private void assertProperty(boolean fromGsa, String gsaValue,
      String key, Properties extraProps, Properties managerProps) {
    String expectedProperty =
        (fromGsa) ? gsaValue : extraProps.getProperty(key);
    assertEquals(expectedProperty, managerProps.getProperty(key));
  }

  /** Represents a GSA request with only a host and port. */
  private static final Gsa GSA_6_10 = new Gsa("", 80, -1);

  /** Represents a GSA request with a securePort attribute. */
  private static final Gsa GSA_6_12 = new Gsa("", 80, 443);

  /** Represents a GSA request with securePort and protocol attributes. */
  private static final Gsa GSA_7_0_HTTP = new Gsa("http", 80, 443);

  /** Represents a GSA request with securePort and protocol attributes. */
  private static final Gsa GSA_7_0_HTTPS = new Gsa("https", 80, 443);

  /**
   * 1. No securePort from GSA, no gsa.feed.securePort, protocol
   * defaults to HTTP: non-SSL feeds.
   */
  public void testGsa610Null() throws Exception {
    setManagerConfig(GSA_6_10, makeProps(null, null), "http", 80);
  }

  /**
   * 2. No securePort from GSA, no gsa.feed.securePort, HTTP: non-SSL
   * feeds.
   */
  public void testGsa610Http() throws Exception {
    setManagerConfig(GSA_6_10, makeProps("http", null), "http", 80);
  }

  /**
   * 3. No securePort from GSA, no gsa.feed.securePort, HTTPS: SSL
   * feeds on gsa.feed.port.
   */
  public void testGsa610Https() throws Exception {
    setManagerConfig(GSA_6_10, makeProps("https", null), "https", 19902);
  }

  /**
   * 4. No securePort from GSA, gsa.feed.securePort, protocol defaults
   * to HTTPS: SSL feeds.
   */
  public void testGsa610NullSecurePort() throws Exception {
    setManagerConfig(GSA_6_10, makeProps(null, "443"), "https", 443);
  }

  /**
   * 5. No securePort from GSA, gsa.feed.securePort, HTTP: non-SSL feeds.
   */
  public void testGsa610HttpSecurePort() throws Exception {
    setManagerConfig(GSA_6_10, makeProps("http", "443"), "http", 80);
  }

  /**
   * 6. No securePort from GSA, gsa.feed.securePort, HTTPS: SSL feeds.
   */
  public void testGsa610HttpsSecurePort() throws Exception {
    setManagerConfig(GSA_6_10, makeProps("https", "443"), "https", 443);
  }

  /**
   * 7. securePort from GSA, gsa.feed.securePort is overwritten,
   * protocol defaults to HTTPS: SSL feeds
   */
  public void testGsa612Null() throws Exception {
    setManagerConfig(GSA_6_12, makeProps(null, "777"), "https", 443);
  }

  /**
   * 8. securePort from GSA, gsa.feed.securePort is overwritten,
   * protocol is not overwritten: HTTP: non-SSL feeds.
   */
  public void testGsa612Http() throws Exception {
    setManagerConfig(GSA_6_12, makeProps("http", "777"), "http", 80);
  }

  /**
   * 9. securePort from GSA, gsa.feed.securePort is overwritten,
   * protocol is not overwritten: HTTPS: SSL feeds.
   */
  public void testGsa612Https() throws Exception {
    setManagerConfig(GSA_6_12, makeProps("https", "777"), "https", 443);
  }

  /**
   * 10. securePort and HTTP protocol from GSA, gsa.feed.securePort is
   * overwritten, protocol is overwritten: HTTP: non-SSL feeds.
   */
  public void testGsa70Http() throws Exception {
    setManagerConfig(GSA_7_0_HTTP, makeProps("https", "777"), "http", 80);
  }

  /**
   * 11. securePort and HTTPS protocol from GSA, gsa.feed.securePort
   * is overwritten, protocol is overwritten: HTTPS: SSL feeds.
   */
  public void testGsa70Https() throws Exception {
    setManagerConfig(GSA_7_0_HTTPS, makeProps("http", "777"), "https", 443);
  }

  private Properties loadProperties(File propFile) throws IOException {
    Properties props = new Properties();
    InputStream inStream = new FileInputStream(propFile);
    try {
      props.load(inStream);
    } finally {
      inStream.close();
    }
    return props;
  }

  private void storeProperties(Properties props, File propFile, String comments)
      throws IOException {
    OutputStream outStream = new FileOutputStream(propFile);
    try {
      props.store(outStream, comments);
    } finally {
      outStream.close();
    }
  }
}
