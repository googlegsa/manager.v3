// Copyright 2010 Google Inc.
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

package com.google.enterprise.connector.importexport;

import com.google.enterprise.connector.instantiator.Configuration;
import com.google.enterprise.connector.scheduler.Schedule;
import com.google.enterprise.connector.servlet.ServletUtil;
import com.google.enterprise.connector.util.XmlParseUtil;

import org.w3c.dom.Element;

import java.io.PrintWriter;

/**
 * An implementation of {@ImportExportConnector} that understands the
 * legacy ImportExport XML format used by the GSA.
 */
public class LegacyImportExportConnector extends ImportExportConnector {

  public LegacyImportExportConnector() {
    super();
  }

  /**
   * @param name the connector's name
   * @param schedule the connector's schedule
   * @param configuration the connector's ConnectorType-specific configuration data
   */
  public LegacyImportExportConnector(String name, Configuration configuration,
       Schedule schedule, String checkpoint) {
    // Legacy format does not include checkpoints.
    super(name, configuration, schedule, null);
  }

  @Override
  public void setCheckpoint(String checkpoint) {
    // Legacy format does not include checkpoints.
    super.setCheckpoint(null);
  }

  // TODO: Remove this when CM v2.0 and older no longer needs to be supported.
  @Override
  @SuppressWarnings("deprecation")
  Schedule readSchedule(Element connectorElement) {
    Schedule schedule = super.readSchedule(connectorElement);
    if (schedule == null) {
      // Could be dealing with old format.
      String scheduleString = XmlParseUtil.getFirstElementByTagName(
           connectorElement, ServletUtil.XMLTAG_CONNECTOR_SCHEDULE);
      if (scheduleString != null && scheduleString.trim().length() > 0) {
        schedule =  new Schedule(scheduleString);
      }
    }
    return schedule;
  }

  @Override
  void writeSchedule(PrintWriter out, int indent) {
    // Legacy Schedules are stringized, with empty elements if null schedule.
    StringBuilder builder = new StringBuilder();
    ServletUtil.writeXMLTagWithAttrs(builder, indent,
        ServletUtil.XMLTAG_CONNECTOR_SCHEDULES,
        ServletUtil.ATTRIBUTE_VERSION + Schedule.CURRENT_VERSION
        + ServletUtil.QUOTE, false);
    Schedule schedule = getSchedule();
    if (schedule != null) {
        builder.append(schedule.toString());
    }
    ServletUtil.writeXMLTag(builder, 0,
        ServletUtil.XMLTAG_CONNECTOR_SCHEDULES, true);
    out.println(builder.toString());
  }

  @Override
  void writeType(PrintWriter out, int indent) {
    // Write out legacy type, but no version.
    ServletUtil.writeXMLElement(out, indent,
        ServletUtil.XMLTAG_CONNECTOR_TYPE, getTypeName());
  }
}
