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

import java.util.logging.Logger;

import com.google.enterprise.connector.manager.Manager;
import com.google.enterprise.connector.manager.MockManager;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * Tests SetSchedule servlet.
 *
 */
public class SetScheduleTest extends TestCase {
  private static final Logger LOG =
      Logger.getLogger(SetScheduleTest.class.getName());

  /**
   * Test method for {@link com.google.enterprise.connector.servlet.SetSchedule#
   * handleDoPost(java.lang.String, com.google.enterprise.connector.manager.Manager)}.
   */
  public void testHandleDoPost() {
    String connectorName = "connector1";
    int load = 6;
    String timeIntervals = "1-2:5-10:12-18";
    String xmlBody =
            "<" + ServletUtil.XMLTAG_CONNECTOR_SCHEDULES + ">\n"
          + "  <" + ServletUtil.XMLTAG_CONNECTOR_NAME + ">" + connectorName
          + "</" + ServletUtil.XMLTAG_CONNECTOR_NAME + ">\n"
          + "  <" + ServletUtil.XMLTAG_LOAD + ">" + load + "</"
          + ServletUtil.XMLTAG_LOAD + ">\n"
          + "  <" + ServletUtil.XMLTAG_TIME_INTERVALS + ">" + timeIntervals + "</"
          + ServletUtil.XMLTAG_TIME_INTERVALS + ">\n"
          + "</" + ServletUtil.XMLTAG_CONNECTOR_SCHEDULES + ">\n";
    int expectedResult = ConnectorMessageCode.SUCCESS;
    LOG.info(xmlBody);
    Manager manager = MockManager.getInstance();
    ConnectorMessageCode status = SetSchedule.handleDoPost(xmlBody, manager);
    LOG.info("Status Id: " + String.valueOf(status.getMessageId()));
    Assert.assertEquals(status.getMessageId(), expectedResult);
  }

  public void testHandleDoPostWithRetryDelay() {
    String connectorName = "connector1";
    int load = 6;
    int retryDelay = 1000;
    String timeIntervals = "1-2:5-10:12-18";
    String xmlBody =
            "<" + ServletUtil.XMLTAG_CONNECTOR_SCHEDULES + ">\n"
          + "  <" + ServletUtil.XMLTAG_CONNECTOR_NAME + ">" + connectorName
          + "</" + ServletUtil.XMLTAG_CONNECTOR_NAME + ">\n"
          + "  <" + ServletUtil.XMLTAG_LOAD + ">" + load + "</"
          + ServletUtil.XMLTAG_LOAD + ">\n"
          + "  <" + ServletUtil.XMLTAG_DELAY + ">" + retryDelay + "</"
          + ServletUtil.XMLTAG_DELAY + ">\n"
          + "  <" + ServletUtil.XMLTAG_TIME_INTERVALS + ">" + timeIntervals + "</"
          + ServletUtil.XMLTAG_TIME_INTERVALS + ">\n"
          + "</" + ServletUtil.XMLTAG_CONNECTOR_SCHEDULES + ">\n";
    int expectedResult = ConnectorMessageCode.SUCCESS;
    LOG.info(xmlBody);
    Manager manager = MockManager.getInstance();
    ConnectorMessageCode status = SetSchedule.handleDoPost(xmlBody, manager);
    LOG.info("Status Id: " + String.valueOf(status.getMessageId()));
    Assert.assertEquals(status.getMessageId(), expectedResult);
  }

}
