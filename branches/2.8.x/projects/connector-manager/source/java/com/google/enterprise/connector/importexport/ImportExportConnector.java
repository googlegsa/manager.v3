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

package com.google.enterprise.connector.importexport;

import com.google.enterprise.connector.instantiator.Configuration;
import com.google.enterprise.connector.scheduler.Schedule;
import com.google.enterprise.connector.servlet.ServletUtil;
import com.google.enterprise.connector.spi.XmlUtils;
import com.google.enterprise.connector.util.XmlParseUtil;

import org.w3c.dom.Element;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.TreeMap;

/**
 * An encapsulation of all the information we export/import per connector
 * instance.
 */
public class ImportExportConnector {
  private String name;
  private Configuration configuration;
  private Schedule schedule;
  private String checkpoint;
  private String typeVersion;

  public ImportExportConnector() {
  }

  /**
   * @param name the connector's name
   * @param configuration the connector's Configuration
   * @param schedule the connector's Schedule
   * @param checkpoint the connector's Traversal checkpoint string.
   */
  public ImportExportConnector(String name, Configuration configuration,
       Schedule schedule, String checkpoint) {
    this(name, configuration, null, schedule, checkpoint);
  }

  /**
   * @param name the connector's name
   * @param configuration the connector's Configuration
   * @param typeVersion ConnectorType version string
   * @param schedule the connector's Schedule
   * @param checkpoint the connector's Traversal checkpoint string.
   */
  public ImportExportConnector(String name, Configuration configuration,
      String typeVersion, Schedule schedule, String checkpoint) {
    setName(name);
    setConfiguration(configuration);
    setTypeVersion(typeVersion);
    setSchedule(schedule);
    setCheckpoint(checkpoint);
  }

  /**
   * Sets the Connector's name.
   *
   * @param name the connector's name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Sets the Connector's {@link Configuration}.
   *
   * @param configuration the connector's Configuration
   */
  public void setConfiguration(Configuration configuration) {
    this.configuration = configuration;
  }

  /**
   * Sets the Connector's {@link Schedule}.
   *
   * @param schedule the connector's Schedule.
   */
  public void setSchedule(Schedule schedule) {
    this.schedule = schedule;
  }

  /**
   * Sets the Connector's checkpoint.
   *
   * @param checkpoint the connector's checkpoint
   */
  public void setCheckpoint(String checkpoint) {
    this.checkpoint = checkpoint;
  }

  /**
   * Set the version string for the ConnectorType.
   *
   * @param version the version string for the Connector type.
   */
  public void setTypeVersion(String version) {
    this.typeVersion = version;
  }

  /**
   * @return the Connector's name.
   */
  public String getName() {
    return name;
  }

  /**
   * @return the Connector's ConnectorType-specific Configuration.
   */
  public Configuration getConfiguration() {
    return configuration;
  }

  /**
   * @return the connector's ConnectorType's name
   */
  public String getTypeName() {
    return configuration.getTypeName();
  }

  /**
   * @return the connector's ConnectorType-specific configuration map.
   */
  public Map<String, String> getConfigMap() {
    return configuration.getMap();
  }

  /**
   * @return the connector's ConnectorType-specific configuration XML,
   * or null if none is available.
   */
  public String getConfigXml() {
    return configuration.getXml();
  }

  /**
   * @return the Connector's traversal Schedule, or null if
   * the Connector has no Schedule.
   * @see com.google.enterprise.connector.scheduler.Schedule
   */
  public Schedule getSchedule() {
    return schedule;
  }

  /**
   * @return the connector's traversal schedule as a String, or null if
   * the connector has no schedule.
   * @see com.google.enterprise.connector.scheduler.Schedule
   */
  public String getScheduleString() {
    return (schedule == null) ? "" : schedule.toString();
  }

  /**
   * @return the connector's traversal checkpoint as a string, or null if
   * the connector has no checkpoint.
   */
  public String getCheckpoint() {
    return checkpoint;
  }

  /**
   * @return the connector's version string, or null if
   * the connector has no version string.
   */
  public String getTypeVersion() {
    return typeVersion;
  }

  /**
   * Deserializes an ImportExportConnector from XML.
   *
   * @param connectorElement an ConnectorInstance XML Element.
   */
  public void fromXml(Element connectorElement) {
    setName(XmlParseUtil.getFirstElementByTagName(
        connectorElement, ServletUtil.XMLTAG_CONNECTOR_NAME));

    // Extract the Configuration
    setConfiguration(readConfiguration(connectorElement));

    // Extract the Schedule.
    setSchedule(readSchedule(connectorElement));

    // Extract the Traversal Checkpoint.
    setCheckpoint(readCheckpoint(connectorElement));
  }

  /**
   * Extract the Traversal Checkpoint from the connectorElement.
   *
   * @param connectorElement an ConnectorInstance XML Element.
   * @return checkpoint as a String, or null if no checkpoint is encoded.
   */
  String readCheckpoint(Element connectorElement) {
    return XmlParseUtil.getFirstElementByTagName(
        connectorElement, ServletUtil.XMLTAG_CONNECTOR_CHECKPOINT);
  }

  /**
   * Extract the Traversal Schedule from the connectorElement.
   *
   * @param connectorElement an ConnectorInstance XML Element.
   * @return a Schedule or null if no Schedule is encoded.
   */
  Schedule readSchedule(Element connectorElement) {
    String scheduleString = XmlParseUtil.getFirstElementByTagName(
        connectorElement, ServletUtil.XMLTAG_CONNECTOR_SCHEDULES);
    if (scheduleString != null && scheduleString.trim().length() > 0) {
        return new Schedule(scheduleString);
    } else {
      // Try looking for an exploded Schedule.
      Element scheduleElement = (Element) connectorElement.getElementsByTagName(
          ServletUtil.XMLTAG_CONNECTOR_SCHEDULES).item(0);
      if (scheduleElement != null && scheduleElement.hasChildNodes()) {
        boolean disabled =
            Boolean.parseBoolean(XmlParseUtil.getFirstElementByTagName(
            scheduleElement, ServletUtil.XMLTAG_DISABLED));
        int load = Integer.parseInt(XmlParseUtil.getFirstElementByTagName(
            scheduleElement, ServletUtil.XMLTAG_LOAD));
        String delay = XmlParseUtil.getFirstElementByTagName(
            scheduleElement, ServletUtil.XMLTAG_DELAY);
        int retryDelayMillis = (delay != null) ? Integer.parseInt(delay) :
            Schedule.defaultRetryDelayMillis();
        String timeIntervals = XmlParseUtil.getFirstElementByTagName(
            scheduleElement,  ServletUtil.XMLTAG_TIME_INTERVALS);
        return new Schedule(name, disabled, load, retryDelayMillis,
            timeIntervals);
      }
    }
    return null;
  }

  /**
   * Extract the Connector Configuration from the connectorElement.
   *
   * @param connectorElement an ConnectorInstance XML Element.
   * @return a Configuration
   */
  Configuration readConfiguration(Element connectorElement) {
    String type = XmlParseUtil.getFirstElementByTagName(
        connectorElement, ServletUtil.XMLTAG_CONNECTOR_TYPE);
    if (type == null || type.length() == 0) {
      Element typeElement = (Element) connectorElement.getElementsByTagName(
          ServletUtil.XMLTAG_CONNECTOR_TYPE).item(0);
      type = typeElement.getAttribute("name");
      typeVersion = typeElement.getAttribute("version");
    }
    Element configElement = (Element) connectorElement.getElementsByTagName(
        ServletUtil.XMLTAG_CONNECTOR_CONFIG).item(0);
    Map<String, String> configMap = XmlParseUtil.getAllAttributes(
        configElement, ServletUtil.XMLTAG_PARAMETERS);
    // TODO: Extract encryption status and preserve it.
    String configXml = null;
    configElement = (Element) connectorElement.getElementsByTagName(
        ServletUtil.XMLTAG_CONNECTOR_CONFIG_XML).item(0);
    if (configElement != null) {
      configXml = XmlParseUtil.getCdata(configElement);
      if (configXml != null) {
        configXml = ServletUtil.restoreEndMarkers(configXml);
      }
    }
    return new Configuration(type, configMap, configXml);
  }

  /**
   * Serializes this connector to XML.
   *
   * @param out PrintWriter to write XML output.
   * @param indent starting indent for the XML tags.
   */
  public void toXml(PrintWriter out, int indent) {
    ServletUtil.writeXMLTag(out, indent,
        ServletUtil.XMLTAG_CONNECTOR_INSTANCE, false);
    ServletUtil.writeXMLElement(out, indent + 1,
        ServletUtil.XMLTAG_CONNECTOR_NAME, getName());

    // Write out the Checkpoint.
    writeCheckpoint(out, indent + 1);

    // Write out the Connector Schedule.
    writeSchedule(out, indent + 1);

    // Write out the Configuration.
    writeConfiguration(out, indent + 1);

    ServletUtil.writeXMLTag(out, indent,
        ServletUtil.XMLTAG_CONNECTOR_INSTANCE, true);
  }

  /**
   * Write out the Checkpoint to the XML stream.
   *
   * @param out PrintWriter to write XML output.
   * @param indent indent for the XML tag.
   */
  void writeCheckpoint(PrintWriter out, int indent) {
    if (getCheckpoint() != null) {
      ServletUtil.writeXMLElement(out, indent,
          ServletUtil.XMLTAG_CONNECTOR_CHECKPOINT, getCheckpoint());
    }
  }

  /**
   * Write out the Schedule to the XML stream.
   *
   * @param out PrintWriter to write XML output.
   * @param indent indent for the XML tag.
   */
  void writeSchedule(PrintWriter out, int indent) {
    Schedule schedule = getSchedule();
    if (schedule != null) {
      ServletUtil.writeXMLTagWithAttrs(out, indent,
          ServletUtil.XMLTAG_CONNECTOR_SCHEDULES,
          ServletUtil.ATTRIBUTE_VERSION + Schedule.CURRENT_VERSION
          + ServletUtil.QUOTE, false);
       if (schedule.isDisabled()) {
          ServletUtil.writeXMLElement(out, indent + 1,
              ServletUtil.XMLTAG_DISABLED, "true");
       }
       ServletUtil.writeXMLElement(out, indent + 1, ServletUtil.XMLTAG_LOAD,
           Integer.toString(schedule.getLoad()));
       ServletUtil.writeXMLElement(out, indent + 1, ServletUtil.XMLTAG_DELAY,
           Integer.toString(schedule.getRetryDelayMillis()));
       ServletUtil.writeXMLElement(out, indent + 1,
           ServletUtil.XMLTAG_TIME_INTERVALS, schedule.getTimeIntervals());
       ServletUtil.writeXMLTag(out, indent,
           ServletUtil.XMLTAG_CONNECTOR_SCHEDULES, true);
    }
  }

  /**
   * Write out the Configuration to the XML stream.
   *
   * @param out PrintWriter to write XML output.
   * @param indent indent for the XML tag.
   */
  void writeConfiguration(PrintWriter out, int indent) {
    writeType(out, indent);

    ServletUtil.writeXMLTag(out, indent,
        ServletUtil.XMLTAG_CONNECTOR_CONFIG, false);
    /* TODO:
    if (flags.contains(Style.LOCAL_ENCRYPTION)) {
      ServletUtil.writeXMLTagWithAttrs(out, indent,
          ServletUtil.XMLTAG_CONNECTOR_CONFIG,
          ServletUtil.ATTRIBUTE_CRYPT + "internal" + ServletUtil.QUOTE,
          false);
    } else if (flags.contains(Style.EXTERNAL_ENCRYPTION)) {
      ServletUtil.writeXMLTagWithAttrs(out, indent,
          ServletUtil.XMLTAG_CONNECTOR_CONFIG,
          ServletUtil.ATTRIBUTE_CRYPT + "external" + ServletUtil.QUOTE,
          false);
    } else {
      // TODO: Should this really be the default?
      // No encryption in legacy tag.
      ServletUtil.writeXMLTag(
          out, indent + 2, ServletUtil.XMLTAG_CONNECTOR_CONFIG, false);
    }
    */
    // TODO: Move this to a utility.
    Map<String, String> configMap = getConfigMap();
    Map<String, String> sorted = new TreeMap<String, String>(configMap);
    for (Map.Entry<String, String> me : sorted.entrySet()) {
      StringBuilder builder = new StringBuilder();
      try {
        builder.append(ServletUtil.ATTRIBUTE_NAME);
        XmlUtils.xmlAppendAttrValue(me.getKey(), builder);
        builder.append(ServletUtil.QUOTE).append(ServletUtil.ATTRIBUTE_VALUE);
        XmlUtils.xmlAppendAttrValue(me.getValue(), builder);
        builder.append(ServletUtil.QUOTE);
      } catch (IOException e) { /* Can't happen with StringBuilder */ }

      ServletUtil.writeXMLTagWithAttrs(out, indent + 1,
          ServletUtil.XMLTAG_PARAMETERS, builder.toString(), true);
    }
    ServletUtil.writeXMLTag(out, indent,
        ServletUtil.XMLTAG_CONNECTOR_CONFIG, true);

    // Write out connectorInstance.xml.
    if (getConfigXml() != null) {
      ServletUtil.writeXMLTag(out, indent,
          ServletUtil.XMLTAG_CONNECTOR_CONFIG_XML, false);
      out.print(ServletUtil.XML_CDATA_START);
      out.print(ServletUtil.escapeEndMarkers(getConfigXml()));
      out.println(ServletUtil.XML_CDATA_END);
      ServletUtil.writeXMLTag(out, indent,
          ServletUtil.XMLTAG_CONNECTOR_CONFIG_XML, true);
    }
  }

  /**
   * Write out the Connector TypeName to the XML stream.
   *
   * @param out PrintWriter to write XML output.
   * @param indent indent for the XML tag.
   */
  void writeType(PrintWriter out, int indent) {
    String version = getTypeVersion();
    if (version == null || version.trim().length() == 0) {
      ServletUtil.writeXMLElement(out, indent,
          ServletUtil.XMLTAG_CONNECTOR_TYPE, getTypeName());
    } else {
      StringBuilder builder = new StringBuilder();
      builder.append(ServletUtil.ATTRIBUTE_NAME).append(getTypeName());
      builder.append(ServletUtil.QUOTE);
      if (getTypeVersion() != null) {
        builder.append(' ').append(ServletUtil.ATTRIBUTE_VERSION);
        builder.append(getTypeVersion()).append(ServletUtil.QUOTE);
      }
      ServletUtil.writeXMLTagWithAttrs(out, indent,
          ServletUtil.XMLTAG_CONNECTOR_TYPE, builder.toString(), true);
    }
  }
}
