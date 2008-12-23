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
import java.io.IOException;
import java.io.InputStream;
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

  public final void testSetConnectorManagerConfig() throws InstantiatorException, 
      IOException {
    // Make a copy of the properties file first and work from it, so it
    // doesn't appear that this file is modified every time the test runs.
    String origFileName = TEST_DIR + APPLICATION_PROPERTIES;
    String propFileName = TEST_DIR + TEST_PROPERTIES;
    ConnectorTestUtils.copyFile(origFileName, propFileName);

    Context.refresh();
    Context context = Context.getInstance();
    context.setStandaloneContext(TEST_DIR + APPLICATION_CONTEXT,
                                 "testdata/mocktestdata/");
    context.setFeeding(false);

    Properties props = loadProperties(propFileName);
    String host = (String) props.get(Context.GSA_FEED_HOST_PROPERTY_KEY);
    int port = Integer.parseInt((String) props.
                                get(Context.GSA_FEED_PORT_PROPERTY_KEY));

    assertEquals("fubar", host);
    assertEquals(25, port);

    context.setConnectorManagerConfig("shme", 14);
    verifyPropsValues("shme", 14, propFileName);
    
    context.setConnectorManagerConfig(host, port);
    verifyPropsValues(host, port, propFileName);
    Context.refresh();
    ConnectorTestUtils.deleteFile(propFileName);
  }

  private void verifyPropsValues(String expectedHost, int expectedPort,
      String propFileName) throws IOException {
    Properties props = loadProperties(propFileName);
    String actualHost = (String) props.get(Context.GSA_FEED_HOST_PROPERTY_KEY);
    int actualPort =
        Integer.valueOf((String) props.get(Context.GSA_FEED_PORT_PROPERTY_KEY))
            .intValue();
    assertEquals(expectedHost, actualHost);
    assertEquals(expectedPort, actualPort);
  }

  private Properties loadProperties(String propFileName) throws IOException {
    Properties props = new Properties();
    InputStream inStream = new FileInputStream(propFileName);
    props.load(inStream);
    return props;
  }
}
