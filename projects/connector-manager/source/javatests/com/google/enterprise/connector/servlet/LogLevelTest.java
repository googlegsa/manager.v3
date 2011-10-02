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

package com.google.enterprise.connector.servlet;

import com.google.enterprise.connector.common.StringUtils;
import com.google.enterprise.connector.common.PropertiesUtils;
import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.connector.test.ConnectorTestUtils;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import junit.framework.TestCase;

import java.io.File;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Tests LogLevel servlet.
 */
public class LogLevelTest extends TestCase {
  private static final Logger LOGGER =
      Logger.getLogger(LogLevelTest.class.getName());

  private static final String APPLICATION_CONTEXT =
      "testdata/contextTests/LogLevelTest.xml";
  private static final String TEST_DIR_NAME = "testdata/tmp/LogLevelTests/";
  private static final String PROP_FILE = "logging.properties";
  private static final String APP_PROP_FILE = "applicationContext.properties";
  private final File baseDirectory  = new File(TEST_DIR_NAME);

  private Logger rootLogger;
  private Level origLevel;
  private String origPropFile;
  private String propFile;
  private Logger feedLogger;
  private String appPropFile;
  private MockHttpServletRequest req;
  private MockHttpServletResponse res;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    assertTrue(ConnectorTestUtils.deleteAllFiles(baseDirectory));
    assertTrue(baseDirectory.mkdirs());
    propFile = TEST_DIR_NAME + PROP_FILE;
    appPropFile = TEST_DIR_NAME + APP_PROP_FILE;
    ConnectorTestUtils.copyFile("testdata/config/" + PROP_FILE, propFile);
    ConnectorTestUtils.copyFile("testdata/mocktestdata/" + APP_PROP_FILE,
                                appPropFile);
    Context.refresh();
    Context context = Context.getInstance();
    context.setStandaloneContext(APPLICATION_CONTEXT,
        Context.DEFAULT_JUNIT_COMMON_DIR_PATH);
    feedLogger =  (Logger) context.getApplicationContext()
        .getBean("FeedWrapperLogger", Logger.class);
    feedLogger.setLevel(Level.OFF);

    rootLogger = Logger.getLogger("");
    origLevel = rootLogger.getLevel();
    origPropFile = System.getProperty("java.util.logging.config.file");
    System.setProperty("java.util.logging.config.file", propFile);
    rootLogger.setLevel(Level.CONFIG);

    req = new MockHttpServletRequest();
    res = new MockHttpServletResponse();
  }

  @Override
  protected void tearDown() throws Exception {
    ConnectorTestUtils.deleteAllFiles(baseDirectory);
    rootLogger.setLevel(origLevel);
    System.setProperty("java.util.logging.config.file", origPropFile);
  }

  /** Test getConnectorLogLevel. */
  public void testGetConnectorLogLevel() throws Exception {
    req.setServletPath("/getConnectorLogLevel");
    new LogLevel().doGet(req, res);
    String expectedResponse = "<CmResponse>\n  <StatusId>0</StatusId>\n"
        + "  <Level>CONFIG</Level>\n</CmResponse>\n";
    assertEquals(expectedResponse,
                 removeInfoElements(res.getContentAsString()));
  }

  /** Test setConnectorLogLevel. */
  public void testSetConnectorLogLevel() throws Exception {
    req.setServletPath("/setConnectorLogLevel");
    req.setParameter("level", "finest");
    new LogLevel().doPost(req, res);
    String expectedResponse = "<CmResponse>\n  <StatusId>0</StatusId>\n"
        + "  <Level>FINEST</Level>\n</CmResponse>\n";
    assertEquals(expectedResponse,
                 removeInfoElements(res.getContentAsString()));
    assertEquals(Level.FINEST, rootLogger.getLevel());
    Properties props = PropertiesUtils.loadFromFile(new File(propFile));
    assertEquals("FINEST", props.get(".level"));
  }

  /** Test getFeedLogLevel. */
  public void testGetFeedLogLevel() throws Exception {
    req.setServletPath("/getFeedLogLevel");
    new LogLevel().doGet(req, res);
    String expectedResponse = "<CmResponse>\n  <StatusId>0</StatusId>\n"
        + "  <Level>OFF</Level>\n</CmResponse>\n";
    assertEquals(expectedResponse,
                 removeInfoElements(res.getContentAsString()));
  }

  /** Test setFeedLogLevel. */
  public void testSetFeedLogLevel() throws Exception {
    req.setServletPath("/setFeedLogLevel");
    req.setParameter("level", "finest");
    new LogLevel().doPost(req, res);
    String expectedResponse = "<CmResponse>\n  <StatusId>0</StatusId>\n"
        + "  <Level>FINEST</Level>\n</CmResponse>\n";
    assertEquals(expectedResponse,
                 removeInfoElements(res.getContentAsString()));
    assertEquals(Level.FINEST, feedLogger.getLevel());
    Properties props = PropertiesUtils.loadFromFile(new File(appPropFile));
    assertEquals("FINEST", props.get("feedLoggingLevel"));
  }

  /**
   * Strips the ServletUtil.XMLTAG_INFO elements from the response
   * and normalizes newlines.
   */
  private String removeInfoElements(String response) {
    StringBuilder buffer =
        new StringBuilder(StringUtils.normalizeNewlines(response));
    int start = buffer.indexOf("  <" + ServletUtil.XMLTAG_INFO + ">");
    if (start >= 0) {
      buffer.delete(start, buffer.indexOf("\n", start) + 1);
    }
    return buffer.toString();
  }
}
