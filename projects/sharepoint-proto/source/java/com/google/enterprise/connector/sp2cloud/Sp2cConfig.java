// Copyright 2010 Google Inc. All Rights Reserved.

package com.google.enterprise.connector.sp2cloud;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

public class Sp2cConfig {
  public static final String DEFAULT_PROPERTY_FILE_NAME = "sp2c.properties";

  private final Map<ConfigurationPropertyKey, String> config;

  static Sp2cConfig newConfig(String[] cmdLine) throws UsageException, IOException{
    Map<ConfigurationPropertyKey, String> configMap = mkconfigMapFromCommandLine(cmdLine);
    String propertyFileName = configMap.get(ConfigurationPropertyKey.property_file_name);
    NewConfigPropertiesResult mkConfigPropertiesResult = newConfigProperties(propertyFileName);
    System.out.println("configPropertied="+mkConfigPropertiesResult.getConfigProperties());
    for (ConfigurationPropertyKey k : ConfigurationPropertyKey.values()) {
      if (configMap.containsKey(k)) {
        continue;
      } else if (mkConfigPropertiesResult.getConfigProperties().containsKey(k)) {
        configMap.put(k, mkConfigPropertiesResult.getConfigProperties().get(k));
      } else if (k.equals(ConfigurationPropertyKey.property_file_name)) {
        if (mkConfigPropertiesResult.getPropertiesFileName() != null) {
          configMap.put(k, mkConfigPropertiesResult.getPropertiesFileName());
        }
      } else {
        throw UsageException.newMissingRequiredValue(k.name());
      }
    }

    return new Sp2cConfig(configMap);
  }

  private static Map<ConfigurationPropertyKey, String>
      mkconfigMapFromCommandLine(String[] cmdLine) throws UsageException {
    Map<ConfigurationPropertyKey, String> configMap =
      new HashMap<ConfigurationPropertyKey, String>();
    for(String arg : cmdLine){
      ConfigurationPropertyKey key = getKeyFromCommandLineArgument(arg);
      String value = getValueFromCommandLineArgument(arg);
      configMap.put(key, value);
    }
    return configMap;
  }

  private static class NewConfigPropertiesResult {
    private final String propertiesFileName;
    private final Map<ConfigurationPropertyKey,String> configProperties;
    NewConfigPropertiesResult(String propertiesFileName,
        Map<ConfigurationPropertyKey,String> configProperties) {
      this.propertiesFileName = propertiesFileName;
      this.configProperties = configProperties;
    }
    public String getPropertiesFileName() {
      return propertiesFileName;
    }
    public Map<ConfigurationPropertyKey, String> getConfigProperties() {
      return configProperties;
    }
  }

  /**
   * Constructs and returns a {MkConfigPropertiesResult Map} with
   * configuration values loaded from a user specified {@link Properties} file
   * if one exists. The rules for locating the file {@link Properties} are:
   *
   * <OL>
   * <LI> First choice - Load form file with name given by
   *      command line argument .
   * <LI> Second choice - Load from file with name given by
   *      {@link #DEFAULT_PROPERTY_FILE_NAME} if said file exists.
   * <LI> Third choice - Empty {@link Properties}
   * </OL>
   */
  private static NewConfigPropertiesResult newConfigProperties(String cmdLinePropertyFileName)
      throws IOException, FileNotFoundException, UsageException {
    Properties configProperties = new Properties();
    //Validated properties copied from configProperties.
    //All type casting related to dealing with a Properties Object
    //is restricted to this function.
    Map<ConfigurationPropertyKey, String> resultMap = new HashMap<ConfigurationPropertyKey, String>();

    String propertyFileName = cmdLinePropertyFileName;
    if (propertyFileName == null && new File(DEFAULT_PROPERTY_FILE_NAME).exists()) {
      propertyFileName = DEFAULT_PROPERTY_FILE_NAME;
    }
    if (propertyFileName != null) {
      configProperties.load(new FileReader(propertyFileName));
    }
    for (Entry<Object, Object> e : configProperties.entrySet()) {
      String propertyName = (String)e.getKey();
      ConfigurationPropertyKey k = null;
      /*
       * Disallow users setting the property_file_name configuration
       * value in the properties file. Since the configuration value
       * is used to locate the properties file it makes no sense to
       * set it in the properties file.
       */
      if (propertyName.equals(ConfigurationPropertyKey.property_file_name.name())) {
        throw UsageException.newUnsupportedProperty(propertyName);
      }
      try {
        k = ConfigurationPropertyKey.valueOf(propertyName);
      } catch (IllegalArgumentException iae) {
        throw UsageException.newUnsupportedProperty(propertyName);
      }
      String propertyValue = (String)e.getValue();
      if (propertyValue.length() == 0) {
        throw UsageException.newInvalidPropertyValue(propertyName, propertyValue);
      }
      resultMap.put(k, propertyValue);
    }
    return new NewConfigPropertiesResult(propertyFileName, resultMap);
  }

  private static ConfigurationPropertyKey getKeyFromCommandLineArgument(String arg)
      throws UsageException {
    if(!arg.startsWith("--")) {
      throw UsageException.newCommandLineArgumentMissingDashes(arg);
    }
    String key = arg.substring(2, getEqualsIx(arg));
    try {
      return ConfigurationPropertyKey.valueOf(key);
    } catch (IllegalArgumentException iae) {
      throw UsageException.newUnsupportedCommandLineArgument(arg);
    }
  }

  private static int getEqualsIx(String arg) throws UsageException {
    int ix = arg.indexOf("=");
    if (ix < 0) {
      throw UsageException.newCommandLineArgumentMissingEquals(arg);
    }
    return ix;
  }

  private static String getValueFromCommandLineArgument(String arg) throws UsageException {
    String result = arg.substring(getEqualsIx(arg) + 1);
    if (result.length() == 0) {
      throw UsageException.newInvalidCommandLineArgumentValue(arg);
    }
    return result;
  }

  private Sp2cConfig(Map<ConfigurationPropertyKey, String> config) {
    this.config = config;
  }

  public String getPropertyFileName() {
    return getProperty(ConfigurationPropertyKey.property_file_name);
  }

  public String getCloudRootFolder() {
    return getProperty(ConfigurationPropertyKey.cloud_root_folder);
  }

  public String getCloudAdminId() {
    return getProperty(ConfigurationPropertyKey.cloud_admin_id);
  }

  public String getCloudConsumerKey() {
    return getProperty(ConfigurationPropertyKey.cloud_consumer_key);
  }

  public String getCloudConsumerSecret() {
    return getProperty(ConfigurationPropertyKey.cloud_consumer_secret);
  }

  public String getSpAdminId() {
    return getProperty(ConfigurationPropertyKey.sp_admin_id);
  }

  public String getSpDomain() {
    return getProperty(ConfigurationPropertyKey.sp_domain);
  }

  public String getSpUrl() {
    return getProperty(ConfigurationPropertyKey.sp_url);
  }

  public String getSpAdminPassword() {
    return getProperty(ConfigurationPropertyKey.sp_admin_password);
  }

  private String getProperty(ConfigurationPropertyKey key) {
    return config.get(key);
  }

  @Override
  public String toString() {
    return "Sp2cConfig: " + config;
  }
}
