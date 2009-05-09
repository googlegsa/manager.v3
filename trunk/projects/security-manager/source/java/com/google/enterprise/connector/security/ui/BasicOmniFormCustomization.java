// Copyright 2009 Google Inc.
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

package com.google.enterprise.connector.security.ui;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * A simple implementation of OmniFormCustomization that can store/read
 * its configuration to/from a file.
 */
public class BasicOmniFormCustomization implements OmniFormCustomization {
  Logger LOGGER = Logger.getLogger(BasicOmniFormCustomization.class.getName());

  /** The name of the serialized Properties file. */
  private String fileName;

  /**
   * The global configuration data corresponding to the
   * FormGlobalOption data.
   */
  private Map<FormGlobalOption, String> globals =
      new HashMap<FormGlobalOption, String>();

  /**
   * The pre-credential configuration data corresponding to the
   * PerCredentialOption data.
   */
  private Map<String, HashMap<PerCredentialOption, String>> groups =
      new HashMap<String, HashMap<PerCredentialOption, String>>();

  /**
   * Constructor that takes a file name as input and reads the configuration
   * from that file.  Note: the way this is currently written is not atomic -
   * it may try to read the configuration and encounter some error halfway
   * through and the internal Map objects may contain only half the
   * configuration.
   */
  public BasicOmniFormCustomization(String fileName) {
    this.fileName = fileName;
    readConfig();
  }

  /** Default constructor for testing purposes. */
  BasicOmniFormCustomization() {
  }

  /**
   * Returns a copy of the credential options for the specified credential
   * group if those options exist, or an empty Map otherwise.
   */
  public Map<PerCredentialOption, String> getCredentialGroupOptions(String credentialGroupName) {
    if (!groups.containsKey(credentialGroupName)) {
      return Collections.emptyMap();
    }
    HashMap<PerCredentialOption, String> mapCopy = new HashMap<PerCredentialOption, String>();
    mapCopy.putAll(groups.get(credentialGroupName));
    return mapCopy;
  }

  /**
   * Returns the global options for the OmniForm customization.
   */
  public Map<FormGlobalOption, String> getGlobalOptions() {
    HashMap<FormGlobalOption, String> mapCopy = new HashMap<FormGlobalOption, String>();
    mapCopy.putAll(globals);
    return mapCopy;
  }

  /**
   * Sets the global options for the OmniForm customization.
   */
  public List<OptionValidationError> setGlobalOptions(Map<FormGlobalOption, String> options) {
    globals.clear();
    globals.putAll(options);
    // TODO(martincochran): there is no error-checking or input validation.  This should be
    // corrected after the demo.
    return Collections.emptyList();
  }

  /**
   * Sets the credential options for the given credential group.
   */
  public List<OptionValidationError> setCredentialGroupOptions(String credentialGroupName,
      Map<PerCredentialOption, String> options) {
    if (!groups.containsKey(credentialGroupName)) {
      groups.put(credentialGroupName, new HashMap<PerCredentialOption, String>());
    }
    groups.get(credentialGroupName).clear();
    groups.get(credentialGroupName).putAll(options);
    // TODO(martincochran): there is no error-checking or input validation.  This
    // might already be done by the consumer of this class, but it's an issue worth
    // revisiting in depth.
    return Collections.emptyList();
  }

  public Set<String> getCredentialGroups() {
    return groups.keySet();
  }

  /**
   * Reads and parses the config from the config file.
   */
  public void readConfig(InputStream is) {
    Properties config = new Properties();

    try {
      config.load(is);
      parsePropertiesConfig(config);
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "Could not read config", e);
    } catch (IllegalArgumentException e) {
      LOGGER.log(Level.SEVERE, "Could not parse config", e);
    }
  }

  /**
   * Read config from file.
   */
  public void readConfig() {
    if (fileName == null) {
      LOGGER.log(Level.WARNING,
          "Attempted to load config when filename is null");
      return;
    }
    try {
      FileInputStream fis = new FileInputStream(fileName);
      readConfig(fis);
    } catch (FileNotFoundException e) {
      LOGGER.log(Level.SEVERE, "Could not find file", e);
    }
  }

  public void saveConfig(OutputStream os) {
    Properties config = getProperties();

    try {
      config.store(os, "OmniForm Customization Options");
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "Could not save config file", e);
    }
  }

  public void saveConfigToFile(String fileName) {
    try {
      saveConfig(new FileOutputStream(fileName));
    } catch (FileNotFoundException e) {
      LOGGER.log(Level.SEVERE, "Could not save config file", e);
    }
  }

  /**
   * Returns a properties object that contains all the configuration data.
   */
  private Properties getProperties() {
    Properties config = new Properties();

    for (String name : groups.keySet()) {
      Map<PerCredentialOption, String> group = groups.get(name);
      for (Map.Entry<PerCredentialOption, String> option : group.entrySet()) {
        config.put(canonicalizeName(option.getKey(), name), option.getValue());
      }
    }
    for (Map.Entry<FormGlobalOption, String> option : globals.entrySet()) {
      config.put(canonicalizeName(option.getKey()), option.getValue());
    }

    return config;
  }

  /**
   * Parses a Properties object and populates the internal Maps with the data.
   * @return true if the config was parsed properly
   */
  private boolean parsePropertiesConfig(Properties config) {
    Enumeration e = config.propertyNames();

    boolean success = true;
    while (e.hasMoreElements()) {
      String item = (String) e.nextElement();
      if (isGlobalOption(item)) {
        globals.put(parseGlobalOption(item), config.getProperty(item));
      } else if (isCredentialOption(item)) {
        String credentialGroup = getCredentialGroup(item);
        if (!groups.containsKey(credentialGroup)) {
          groups.put(credentialGroup, new HashMap<PerCredentialOption, String>());
        }
        groups.get(credentialGroup).put(parseCredentialOption(item),
            config.getProperty(item));
      } else {
        LOGGER.info("Could not parse option: " + item);
        success = false;
      }
    }

    return success;
  }

  private boolean isGlobalOption(String item) {
    return item.startsWith(getShortClassName(FormGlobalOption.PAGE_TITLE));
  }

  private boolean isCredentialOption(String item) {
    return item.startsWith(getShortClassName(PerCredentialOption.INTRO_TEXT));
  }

  private FormGlobalOption parseGlobalOption(String option)
      throws IllegalArgumentException {
    String[] parts = option.split(":");
    if (parts.length < 2) {
      throw new IllegalArgumentException("Argument improperly formatted: " + option);
    }
    return Enum.valueOf(FormGlobalOption.class, parts[1]);
  }

  private PerCredentialOption parseCredentialOption(String option)
      throws IllegalArgumentException {
    String[] parts = option.split(":");
    if (parts.length < 2) {
      throw new IllegalArgumentException("Argument improperly formatted: " + option);
    }
    return Enum.valueOf(PerCredentialOption.class, parts[1]);
  }

  private String canonicalizeName(Object obj) {
    return getShortClassName(obj) + ":" + obj.toString();
  }

  private String canonicalizeName(Object obj, String name) {
    return getShortClassName(obj) + ":" + obj.toString() + ":" + name;
  }

  /**
   * For an obj of type FormGlobalOption, this function returns
   * "FormGlobalOption".  This is used, rather than the full classname,
   * because corresponding code on the GSA may exist in a different package
   * hierarchy.
   */
  private String getShortClassName(Object obj) {
    String fullName = obj.getClass().getName();
    return fullName.substring(fullName.lastIndexOf("$") + 1);
  }

  /**
   * Preconditions: item really is an entry for a Credential option,
   * not global option.
   * */
  private String getCredentialGroup(String item) throws IllegalArgumentException {
    String[] parts = item.split(":");
    if (parts.length != 3) {
      LOGGER.severe("Problem with format");
      throw new IllegalArgumentException("Argument did not have valid group: " + item);
    }
    return parts[2];
  }

  @Override
  public String toString() {
    return getProperties().toString();
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof BasicOmniFormCustomization)) {
      return false;
    }
    return toString().equals(obj.toString());
  }
}
