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

import com.google.common.io.Files;
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

  private File testPropFile;
  private File classesDirectory;
  private File classesPropFile;
  private File configPropFile;
  private File appPropFile;

  private Logger feedLogger;
  private Logger rootLogger;
  private Level origLevel;
  private String origPropFile;

  private MockHttpServletRequest req;
  private MockHttpServletResponse res;


  @Override
  protected void setUp() throws Exception {
    super.setUp();
    ConnectorTestUtils.deleteAllFiles(baseDirectory);
    assertTrue(ConnectorTestUtils.mkdirs(baseDirectory));

    testPropFile = new File(new File("testdata", "config"), PROP_FILE);
    configPropFile = new File(baseDirectory, PROP_FILE);
    appPropFile = new File(baseDirectory, APP_PROP_FILE);

    Files.copy(testPropFile, configPropFile);
    Files.copy(new File(new File("testdata", "mocktestdata"), APP_PROP_FILE),
               appPropFile);

    Context.refresh();
    Context context = Context.getInstance();
    context.setStandaloneContext(APPLICATION_CONTEXT,
        Context.DEFAULT_JUNIT_COMMON_DIR_PATH);

    classesDirectory = new File(context.getCommonDirPath(), "classes");
    classesPropFile = new File(classesDirectory, PROP_FILE);
    assertTrue(ConnectorTestUtils.mkdirs(classesDirectory));

    feedLogger =  (Logger) context.getApplicationContext()
        .getBean("FeedWrapperLogger", Logger.class);
    feedLogger.setLevel(Level.OFF);

    rootLogger = Logger.getLogger("");
    origLevel = rootLogger.getLevel();
    origPropFile = System.getProperty("java.util.logging.config.file");
    System.setProperty("java.util.logging.config.file",
                       configPropFile.getAbsolutePath());
    rootLogger.setLevel(Level.CONFIG);

    req = new MockHttpServletRequest();
    res = new MockHttpServletResponse();
  }

  @Override
  protected void tearDown() throws Exception {
    ConnectorTestUtils.deleteAllFiles(baseDirectory);
    ConnectorTestUtils.deleteAllFiles(classesDirectory);
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

  /** Test setConnectorLogLevel with logging config system property. */
  public void testSetConnectorLogLevel1() throws Exception {
    setConnectorLogLevel();

    // Check that the system property logging.properties file has new log level.
    checkProperty(configPropFile, ".level", "FINEST");
  }

  /**
   * Test setConnectorLogLevel with no classes/logging.properties and no
   * System logging config property.
   */
  public void testSetConnectorLogLevel2() throws Exception {
    // Make sure there is no classes/logging.properties file.
    classesPropFile.delete();
    assertFalse(classesPropFile.exists());
      
    // And no System logging configuration.
    System.clearProperty("java.util.logging.config.file");

    setConnectorLogLevel();

    // Check that the system property logging.properties file is intact.
    assertTrue(Files.equal(testPropFile, configPropFile));
  }

  /** Test setConnectorLogLevel with classes/logging.properties. */
  public void testSetConnectorLogLevel3() throws Exception {
    Files.copy(testPropFile, classesPropFile);

    setConnectorLogLevel();

    // Check that the classes/logging.properties file has new log level.
    checkProperty(classesPropFile, ".level", "FINEST");

    // Check that the system property logging.properties file is intact.
    assertTrue(Files.equal(testPropFile, configPropFile));
  }

  /** Set Connector Log Level. */
  private void setConnectorLogLevel() throws Exception {
    req.setServletPath("/setConnectorLogLevel");
    req.setParameter("level", "finest");
    new LogLevel().doPost(req, res);
    String expectedResponse = "<CmResponse>\n  <StatusId>0</StatusId>\n"
        + "  <Level>FINEST</Level>\n</CmResponse>\n";
    assertEquals(expectedResponse,
                 removeInfoElements(res.getContentAsString()));
    assertEquals(Level.FINEST, rootLogger.getLevel());
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
    checkProperty(appPropFile, "feedLoggingLevel", "FINEST");
  }

  private void checkProperty(File propFile, String propName,
      String expectedValue) throws Exception {
    Properties props = PropertiesUtils.loadFromFile(propFile);
    assertEquals(expectedValue, props.get(propName));
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
