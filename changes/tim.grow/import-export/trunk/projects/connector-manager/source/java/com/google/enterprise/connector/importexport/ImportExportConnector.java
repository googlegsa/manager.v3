package com.google.enterprise.connector.importexport;

import java.util.Map;

/**
 * An encapsulation of all the information we export/import per connector
 * instance.
 */
public class ImportExportConnector {
  private String name;
  private String type;
  private String scheduleString;
  private Map config;

  /**
   * @param name the connector's name
   * @param type the connector's ConnectorType's name
   * @param scheduleString the connector's schedule as a String
   * @param config the connector's ConnectorType-specific configuration data
   */
  public ImportExportConnector(
      String name, String type, String scheduleString, Map config) {
    this.name = name;
    this.type = type;
    this.scheduleString = scheduleString;
    this.config = config;
  }

  /**
   * @return the connector's name
   */
  public String getName() {
    return name;
  }

  /**
   * @return the connector's ConnectorType's name
   */
  public String getType() {
    return type;
  }

  /**
   * @return the connector's schedule as a String
   * @see com.google.enterprise.connector.scheduler.Schedule
   */
  public String getScheduleString() {
    return scheduleString;
  }

  /**
   * @return the connector's ConnectorType-specific configuration data
   */
  public Map getConfig() {
    return config;
  }
}
