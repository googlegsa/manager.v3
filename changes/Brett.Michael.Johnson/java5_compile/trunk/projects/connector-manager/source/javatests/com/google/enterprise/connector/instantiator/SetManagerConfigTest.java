// Copyright (C) 2006-2008 Google Inc.
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

package com.google.enterprise.connector.instantiator;

import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.connector.test.ConnectorTestUtils;

import junit.framework.TestCase;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

/**
 * Tests for {@link Context#setConnectorManagerConfig(String, int)}.
 */
public class SetManagerConfigTest extends TestCase {

  private static final String TEST_DIR =
      "testdata/contextTests/setManagerConfig/";
  private static final String APPLICATION_CONTEXT = "applicationContext.xml";
  private static final String APPLICATION_PROPERTIES =
      "applicationContext.properties";
  private static final String TEST_PROPERTIES = "testContext.properties";

  private String propFileName;
  private Context context;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    // Make a copy of the properties file first and work from it, so it
    // doesn't appear that this file is modified every time the test runs.
    String origFileName = TEST_DIR + APPLICATION_PROPERTIES;
    this.propFileName = TEST_DIR + TEST_PROPERTIES;
    ConnectorTestUtils.copyFile(origFileName, propFileName);

    Context.refresh();
    context = Context.getInstance();
    context.setStandaloneContext(TEST_DIR + APPLICATION_CONTEXT,
                                 "testdata/mocktestdata/");
    context.setFeeding(false);
  }

  @Override
  protected void tearDown() throws Exception {
    Context.refresh();
    ConnectorTestUtils.deleteFile(propFileName);
    super.tearDown();
  }

  public final void testGetConnectorManagerConfig() throws Exception {
    // Get the properties directly from the file.
    Properties props = loadProperties(propFileName);
    String host = props.getProperty(Context.GSA_FEED_HOST_PROPERTY_KEY);
    int port = Integer.parseInt(props.getProperty(
        Context.GSA_FEED_PORT_PROPERTY_KEY));
    // Now get them from the Context.
    Properties managerProps = context.getConnectorManagerConfig();
    String ctxHost =
        managerProps.getProperty(Context.GSA_FEED_HOST_PROPERTY_KEY);
    int ctxPort = Integer.parseInt(
        managerProps.getProperty(Context.GSA_FEED_PORT_PROPERTY_KEY));
    assertEquals("Correct host from context", host, ctxHost);
    assertEquals("Correct port from context", port, ctxPort);

    // Update the manager config and pull again.
    context.setConnectorManagerConfig("shme", 14);
    managerProps = context.getConnectorManagerConfig();
    ctxHost =
        managerProps.getProperty(Context.GSA_FEED_HOST_PROPERTY_KEY);
    ctxPort = Integer.parseInt(
        managerProps.getProperty(Context.GSA_FEED_PORT_PROPERTY_KEY));
    assertEquals("Correct host from context", "shme", ctxHost);
    assertEquals("Correct port from context", 14, ctxPort);
  }

  public final void testSetConnectorManagerConfig() throws InstantiatorException,
      IOException {
    Properties props = loadProperties(propFileName);
    String host = props.getProperty(Context.GSA_FEED_HOST_PROPERTY_KEY);
    int port = Integer.parseInt(
        props.getProperty(Context.GSA_FEED_PORT_PROPERTY_KEY));

    assertEquals("fubar", host);
    assertEquals(25, port);

    context.setConnectorManagerConfig("shme", 14);
    verifyPropsValues("shme", 14, propFileName);

    context.setConnectorManagerConfig(host, port);
    verifyPropsValues(host, port, propFileName);
  }

  public final void testIsManagerLocked() throws Exception {
    // Check initial states.
    assertTrue("Manager with missing prop is locked",
        context.getIsManagerLocked());
    setLockedProperty(Boolean.FALSE.toString());
    assertFalse("Manager with prop set to false", context.getIsManagerLocked());
    // Check state after updating the manager config.
    context.setConnectorManagerConfig("172.25.25.25", 19900);
    assertTrue("Manager is locked after setting config",
        context.getIsManagerLocked());
  }

  private void setLockedProperty(String isLocked) throws Exception {
    Properties props = loadProperties(propFileName);
    props.setProperty(Context.MANAGER_LOCKED_PROPERTY_KEY, isLocked);
    storeProperties(props, propFileName, "Updating lock");
  }

  private void verifyPropsValues(String expectedHost, int expectedPort,
      String propFileName) throws IOException {
    Properties props = loadProperties(propFileName);
    String actualHost = props.getProperty(Context.GSA_FEED_HOST_PROPERTY_KEY);
    int actualPort =
        Integer.valueOf(props.getProperty(Context.GSA_FEED_PORT_PROPERTY_KEY));
    assertEquals(expectedHost, actualHost);
    assertEquals(expectedPort, actualPort);
    String isManagerLocked =
        props.getProperty(Context.MANAGER_LOCKED_PROPERTY_KEY);
    assertEquals("Manager is locked", Boolean.TRUE.toString(), isManagerLocked);
  }

  private Properties loadProperties(String propFileName) throws IOException {
    Properties props = new Properties();
    InputStream inStream = new FileInputStream(propFileName);
    props.load(inStream);
    return props;
  }

  private void storeProperties(Properties props, String propFileName,
      String comments) throws IOException {
    OutputStream outStream = new FileOutputStream(propFileName);
    props.store(outStream, comments);
  }
}
