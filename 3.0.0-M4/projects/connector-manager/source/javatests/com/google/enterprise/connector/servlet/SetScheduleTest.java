// Copyright 2006 Google Inc.  All Rights Reserved.
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

import com.google.common.base.Strings;
import com.google.enterprise.connector.instantiator.Configuration;
import com.google.enterprise.connector.manager.Manager;
import com.google.enterprise.connector.manager.MockManager;
import com.google.enterprise.connector.persist.ConnectorNotFoundException;
import com.google.enterprise.connector.persist.PersistentStoreException;
import com.google.enterprise.connector.scheduler.Schedule;
import com.google.enterprise.connector.spi.XmlUtils;

import junit.framework.TestCase;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 * Tests SetSchedule servlet.
 */
public class SetScheduleTest extends TestCase {
  private static final Logger LOGGER =
      Logger.getLogger(SetScheduleTest.class.getName());

  public void testInvalidSchedule() {
    checkHandleDoPost("Invalid", false, null, null, null,
                      ConnectorMessageCode.ERROR_PARSING_XML_REQUEST);
  }

  public void testEmptySchedule() {
    checkHandleDoPost(null, false, null, null, null,
                      ConnectorMessageCode.RESPONSE_NULL_CONNECTOR);
  }

  public void testNoConnectorName() {
    checkHandleDoPost("", false, null, null, null,
                      ConnectorMessageCode.RESPONSE_NULL_CONNECTOR);
  }

  public void testDisabled() {
    checkHandleDoPost("connector1", true, "6", "1000", "1-2:5-10:12-18",
                      ConnectorMessageCode.SUCCESS);
  }

  public void testNoLoad() {
    checkHandleDoPost("connector1", false, null, "1000", "1-2:5-10:12-18",
                      ConnectorMessageCode.SUCCESS);
  }

  public void testEmptyLoad() {
    checkHandleDoPost("connector1", false, "", "1000", "1-2:5-10:12-18",
                      ConnectorMessageCode.SUCCESS);
  }

  public void testNoRetryDelay() {
    checkHandleDoPost("connector1", false, "6", null, "1-2:5-10:12-18",
                      ConnectorMessageCode.SUCCESS);
  }

  public void testEmptyRetryDelay() {
    checkHandleDoPost("connector1", false, "6", "", "1-2:5-10:12-18",
                      ConnectorMessageCode.SUCCESS);
  }

  public void testWithRetryDelay() {
    checkHandleDoPost("connector1", false, "6", "1000", "1-2:5-10:12-18",
                      ConnectorMessageCode.SUCCESS);
  }

  public void testNoTimeIntervals() {
    checkHandleDoPost("connector1", false, "6", "1000", null,
                      ConnectorMessageCode.SUCCESS);
  }

  public void testEmpyTimeIntervals() {
    checkHandleDoPost("connector1", false, "6", "1000", "",
                      ConnectorMessageCode.SUCCESS);
  }

  public void testConnectorNotFoundException() {
    checkHandleDoPost("UnknownConnector", false, "6", "1000", "1-2:5-10:12-18",
                      ConnectorMessageCode.EXCEPTION_CONNECTOR_NOT_FOUND);
  }

  public void testPersistenStoreException() {
    checkHandleDoPost("IntransigentConnector", false, "6", "1000", "1-2:5-10",
                      ConnectorMessageCode.EXCEPTION_PERSISTENT_STORE);
  }

  /**
   * Test method for {@link com.google.enterprise.connector.servlet.SetSchedule#
   * handleDoPost(java.lang.String, com.google.enterprise.connector.manager.Manager)}.
   */
  private void checkHandleDoPost(String connectorName, boolean isDisabled,
      String load, String retryDelay, String timeIntervals,
      int expectedResult) {
    LOGGER.info("\n==========================================================\n");
    LOGGER.info("Test: " + getName());
    String xmlBody = makeXmlBody(connectorName, isDisabled, load, retryDelay,
                                 timeIntervals);
    LOGGER.info("XmlBody:\n" + xmlBody);
    ScheduleManager manager = new ScheduleManager();
    ConnectorMessageCode status = SetSchedule.handleDoPost(xmlBody, manager);
    LOGGER.info("Status Id: " + String.valueOf(status.getMessageId()));
    assertEquals(status.getMessageId(), expectedResult);
    Schedule schedule = manager.getSchedule(connectorName);
    LOGGER.info("Schedule: " + schedule);
    if (schedule == null) {
      if (status.getMessageId() == ConnectorMessageCode.SUCCESS) {
        fail("Null Schedule");
      } else {
        return;
      }
    }
    assertEquals(connectorName.toLowerCase(), schedule.getConnectorName());
    assertEquals(isDisabled, schedule.isDisabled());
    assertEquals((Strings.isNullOrEmpty(load) ? 0 : Integer.parseInt(load)),
                 schedule.getLoad());
    if (Strings.isNullOrEmpty(retryDelay)) {
      assertEquals(Schedule.defaultRetryDelayMillis(),
                   schedule.getRetryDelayMillis());
    } else {
      assertEquals(Integer.parseInt(retryDelay),
                   schedule.getRetryDelayMillis());
    }
    assertEquals(((timeIntervals == null)? "" : timeIntervals),
                 schedule.getTimeIntervals());
  }

  private String makeXmlBody(String connectorName, boolean isDisabled,
      String load, String retryDelay, String timeIntervals) {
    StringBuilder b = new StringBuilder();
    // Special trigger for invalid schedule body.
    if ("Invalid".equals(connectorName)) {
      return "<test></test>";
    }
    try {
      XmlUtils.xmlAppendStartTag(ServletUtil.XMLTAG_CONNECTOR_SCHEDULES, b);
      if (connectorName != null) {  // Null is trigger for empty schedule body.
        b.append("\n");
        XmlUtils.xmlAppendStartTag(ServletUtil.XMLTAG_CONNECTOR_NAME, b);
        b.append(connectorName);
        XmlUtils.xmlAppendEndTag(ServletUtil.XMLTAG_CONNECTOR_NAME, b);
        if (isDisabled) {
          XmlUtils.xmlAppendStartTag(ServletUtil.XMLTAG_DISABLED, b);
          b.append("true");
          XmlUtils.xmlAppendEndTag(ServletUtil.XMLTAG_DISABLED, b);
        }
        if (load != null) {
          XmlUtils.xmlAppendStartTag(ServletUtil.XMLTAG_LOAD, b);
          b.append(load);
          XmlUtils.xmlAppendEndTag(ServletUtil.XMLTAG_LOAD, b);
        }
        if (retryDelay != null) {
          XmlUtils.xmlAppendStartTag(ServletUtil.XMLTAG_DELAY, b);
          b.append(retryDelay);
          XmlUtils.xmlAppendEndTag(ServletUtil.XMLTAG_DELAY, b);
        }
        if (timeIntervals != null) {
          XmlUtils.xmlAppendStartTag(ServletUtil.XMLTAG_TIME_INTERVALS, b);
          b.append(timeIntervals);
          XmlUtils.xmlAppendEndTag(ServletUtil.XMLTAG_TIME_INTERVALS, b);
        }
      }
      XmlUtils.xmlAppendEndTag(ServletUtil.XMLTAG_CONNECTOR_SCHEDULES, b);
    } catch (IOException e) {
      fail(e.getMessage());
    }
    return b.toString();
  }

  /** A Manager that throws exceptions when setting the schedule. */
  private static class ScheduleManager extends MockManager {
    private HashMap<String, String> schedules = new HashMap<String, String>();

    @Override
    public Configuration getConnectorConfiguration(String connectorName)
        throws ConnectorNotFoundException {
      if ("UnknownConnector".equalsIgnoreCase(connectorName)) {
        throw new ConnectorNotFoundException(connectorName);
      }
      return new Configuration("Mock", new HashMap<String, String>(), null);
    }

    @Override
    public void setSchedule(String connectorName, String schedule)
        throws ConnectorNotFoundException, PersistentStoreException {
      if ("UnknownConnector".equalsIgnoreCase(connectorName)) {
        throw new ConnectorNotFoundException(connectorName);
      }
      if ("IntransigentConnector".equalsIgnoreCase(connectorName)) {
        throw new PersistentStoreException(connectorName);
      }
      schedules.put(connectorName.toLowerCase(), schedule);
    }

    /** Return the cached schedule for this connector. */
    protected Schedule getSchedule(String connectorName) {
      if (Strings.isNullOrEmpty(connectorName)) {
        return null;
      }
      String schedString = schedules.get(connectorName.toLowerCase());
      return (schedString == null) ? null : new Schedule(schedString);
    }
  }
}
