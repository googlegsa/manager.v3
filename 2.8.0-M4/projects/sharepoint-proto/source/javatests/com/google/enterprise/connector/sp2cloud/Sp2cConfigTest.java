// Copyright 2010 Google Inc. All Rights Reserved.

package com.google.enterprise.connector.sp2cloud;

import junit.framework.TestCase;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class Sp2cConfigTest extends TestCase {

  private Map<ConfigurationPropertyKey, String> mkTestConfigMap(String propertyFileName) {
    Map<ConfigurationPropertyKey, String> result = new HashMap<ConfigurationPropertyKey, String>();
    for(ConfigurationPropertyKey k : ConfigurationPropertyKey.values()) {
      if (!k.equals(ConfigurationPropertyKey.property_file_name)) {
        result.put(k, k.name() + "_value");
      }
    }
    if (propertyFileName != null) {
      result.put(ConfigurationPropertyKey.property_file_name, propertyFileName);
    }
    return result;
  }

  public String[] configMapToCmdLine(Map<ConfigurationPropertyKey, String> config) {
    List<String> l = new ArrayList<String>();
    for (Map.Entry<ConfigurationPropertyKey, String> e : config.entrySet()) {
      l.add("--" + e.getKey().name() + "=" + e.getValue());
    }

    return l.toArray(new String[0]);
  }

  public void testNewConfig_noPropertiesFile() throws UsageException, IOException {
    File pf = new File(Sp2cConfig.DEFAULT_PROPERTY_FILE_NAME);
    pf.delete();
    Map<ConfigurationPropertyKey, String>configMap = mkTestConfigMap(null);
    String[] cmdLine = configMapToCmdLine(configMap);
    Sp2cConfig config = Sp2cConfig.newConfig(cmdLine);
    assertEquals(configMap.get(ConfigurationPropertyKey.cloud_admin_id), config.getCloudAdminId());
    assertEquals(configMap.get(ConfigurationPropertyKey.cloud_consumer_key), config.getCloudConsumerKey());
    assertEquals(configMap.get(ConfigurationPropertyKey.cloud_consumer_secret), config.getCloudConsumerSecret());
    assertEquals(configMap.get(ConfigurationPropertyKey.cloud_root_folder), config.getCloudRootFolder());

    assertEquals(configMap.get(ConfigurationPropertyKey.property_file_name), config.getPropertyFileName());

    assertEquals(configMap.get(ConfigurationPropertyKey.sp_admin_id), config.getSpAdminId());
    assertEquals(configMap.get(ConfigurationPropertyKey.sp_admin_password), config.getSpAdminPassword());
    assertEquals(configMap.get(ConfigurationPropertyKey.sp_domain), config.getSpDomain());
    assertEquals(configMap.get(ConfigurationPropertyKey.sp_url), config.getSpUrl());
    assertEquals(configMap.get(ConfigurationPropertyKey.sp_source_path), config.getSpSourcePath());
  }

  public void testNewConfig_missingPropertiesFile() throws UsageException, IOException {
    try {
      String[] cmdLine = new String[]{"--property_file_name=iNoExist"};
      Sp2cConfig config = Sp2cConfig.newConfig(cmdLine);
      fail("Expected failure.");
    } catch (FileNotFoundException fnfe) {
      //Expected
    }

  }

  public void testNewConfig_missingRequiredArgument() throws IOException {
    try {
      String[] cmdLine = new String[0];
      Sp2cConfig config = Sp2cConfig.newConfig(cmdLine);
      fail("Expected failure.");
    } catch (UsageException ue) {
      assertTrue(ue.getMessage().contains("Required configuration value not specified ("));
    }
  }
  public void testNewConfig_missingEquals() throws IOException {
    try {
      String[] cmdLine = new String[]{"--abc"};
      Sp2cConfig config = Sp2cConfig.newConfig(cmdLine);
      fail("Expected failure.");
    } catch (UsageException ue) {
      assertTrue(ue.getMessage().contains("Command line argument missing '=' ("));
    }
  }

  public void testNewConfig_missingDashDash() throws IOException {
    try {
      String[] cmdLine = new String[]{"abc="};
      Sp2cConfig config = Sp2cConfig.newConfig(cmdLine);
      fail("Expected failure.");
    } catch (UsageException ue) {
      assertTrue(ue.getMessage().contains("Command line argument missing '--' ("));
    }
  }
  public void testNewConfig_usupportedProperty() throws IOException {
    File tmpDir = new File(System.getProperty("java.io.tmpdir"));
    File pf = File.createTempFile("cfg", "properties", tmpDir);
    Properties props = new Properties();
    props.setProperty("xxYYzz", "johnfelton");
    Writer w = new FileWriter(pf);
    try {
    props.store(w, "hi");
    } finally {
      w.close();
    }

    try {
      String[] cmdLine = new String[]{"--property_file_name=" + pf.getAbsolutePath()};
      Sp2cConfig config = Sp2cConfig.newConfig(cmdLine);
      fail("Expected failue.");
    } catch (UsageException ue) {
      assertEquals("UnsupportedProperty(xxYYzz)", ue.getMessage());
    }


  }

  public void testNewConfig_usupportedArgument() throws IOException {
    try {
      String[] cmdLine = new String[]{"--XXYYZZ="};
      Sp2cConfig config = Sp2cConfig.newConfig(cmdLine);
      fail("Expected failure.");
    } catch (UsageException ue) {
      assertEquals("Unknown command line argument (--XXYYZZ=)", ue.getMessage());
    }
  }

  public void testNewConfig_defaultPropertiesFile() throws UsageException, IOException {
    File pf = new File(Sp2cConfig.DEFAULT_PROPERTY_FILE_NAME);
    Properties props = new Properties();
    props.setProperty(ConfigurationPropertyKey.cloud_admin_id.name(), "johnfelton");
    props.setProperty(ConfigurationPropertyKey.sp_admin_id.name(), "strellis");
    Writer w = new FileWriter(pf);
    try {
    props.store(w, "hi");
    } finally {
      w.close();
    }

    Map<ConfigurationPropertyKey, String>configMap = mkTestConfigMap(null);
    configMap.remove(ConfigurationPropertyKey.cloud_admin_id);
    String[] cmdLine = configMapToCmdLine(configMap);
    Sp2cConfig config = Sp2cConfig.newConfig(cmdLine);
    assertEquals(props.get(ConfigurationPropertyKey.cloud_admin_id.name()), config.getCloudAdminId());
    assertEquals(configMap.get(ConfigurationPropertyKey.cloud_consumer_key), config.getCloudConsumerKey());
    assertEquals(configMap.get(ConfigurationPropertyKey.cloud_consumer_secret), config.getCloudConsumerSecret());
    assertEquals(configMap.get(ConfigurationPropertyKey.cloud_root_folder), config.getCloudRootFolder());

    assertEquals(Sp2cConfig.DEFAULT_PROPERTY_FILE_NAME, config.getPropertyFileName());

    assertEquals(configMap.get(ConfigurationPropertyKey.sp_admin_id), config.getSpAdminId());
    assertEquals(configMap.get(ConfigurationPropertyKey.sp_admin_password), config.getSpAdminPassword());
    assertEquals(configMap.get(ConfigurationPropertyKey.sp_domain), config.getSpDomain());
    assertEquals(configMap.get(ConfigurationPropertyKey.sp_url), config.getSpUrl());
    assertEquals(configMap.get(ConfigurationPropertyKey.sp_source_path), config.getSpSourcePath());
  }

  public void testNewConfig_commandLineropertiesFile() throws UsageException, IOException {
    File tmpDir = new File(System.getProperty("java.io.tmpdir"));
    File pf = File.createTempFile("cfg", "properties", tmpDir);
    Properties props = new Properties();
    props.setProperty(ConfigurationPropertyKey.cloud_admin_id.name(), "johnfelton");
    props.setProperty(ConfigurationPropertyKey.sp_admin_id.name(), "strellis");
    Writer w = new FileWriter(pf);
    try {
    props.store(w, "hi");
    } finally {
      w.close();
    }

    Map<ConfigurationPropertyKey, String>configMap = mkTestConfigMap(pf.getAbsolutePath());
    configMap.remove(ConfigurationPropertyKey.cloud_admin_id);
    String[] cmdLine = configMapToCmdLine(configMap);
    Sp2cConfig config = Sp2cConfig.newConfig(cmdLine);
    assertEquals(props.get(ConfigurationPropertyKey.cloud_admin_id.name()), config.getCloudAdminId());
    assertEquals(configMap.get(ConfigurationPropertyKey.cloud_consumer_key), config.getCloudConsumerKey());
    assertEquals(configMap.get(ConfigurationPropertyKey.cloud_consumer_secret), config.getCloudConsumerSecret());
    assertEquals(configMap.get(ConfigurationPropertyKey.cloud_root_folder), config.getCloudRootFolder());

    assertEquals(configMap.get(ConfigurationPropertyKey.property_file_name), config.getPropertyFileName());

    assertEquals(configMap.get(ConfigurationPropertyKey.sp_admin_id), config.getSpAdminId());
    assertEquals(configMap.get(ConfigurationPropertyKey.sp_admin_password), config.getSpAdminPassword());
    assertEquals(configMap.get(ConfigurationPropertyKey.sp_domain), config.getSpDomain());
    assertEquals(configMap.get(ConfigurationPropertyKey.sp_url), config.getSpUrl());
    assertEquals(configMap.get(ConfigurationPropertyKey.sp_source_path), config.getSpSourcePath());
  }
}
