// Copyright 2008 Google Inc.
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

package com.google.enterprise.connector.common;

import com.google.common.collect.ImmutableSet;
import com.google.enterprise.connector.instantiator.EncryptedPropertyPlaceholderConfigurer;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

public class PropertiesUtils {

  private static final Logger LOGGER =
      Logger.getLogger(PropertiesUtils.class.getName());

  // Non-persistable special google properties.
  public static final String GOOGLE_CONNECTOR_WORK_DIR =
      "googleConnectorWorkDir";
  public static final String GOOGLE_WORK_DIR = "googleWorkDir";
  public static final String GOOGLE_FEED_HOST = "googleFeedHost";
  public static final String GOOGLE_CONNECTOR_NAME = "googleConnectorName";

  public static final Set<String> GOOGLE_NONPERSISTABLE_PROPERTIES =
      ImmutableSet.of(GOOGLE_CONNECTOR_WORK_DIR, GOOGLE_WORK_DIR,
                      GOOGLE_FEED_HOST, GOOGLE_CONNECTOR_NAME);

  // Persistable special google properties.
  public static final String GOOGLE_GLOBAL_NAMESPACE = "googleGlobalNamespace";
  public static final String GOOGLE_LOCAL_NAMESPACE = "googleLocalNamespace";
  public static final String GOOGLE_PROPERTIES_VERSION =
      "googlePropertiesVersion";
  public static final int GOOGLE_PROPERTIES_VERSION_NUMBER = 3;

  // Non-XML format Properties files are by definition 8859-1 encoding.
  public static final String PROPERTIES_ENCODING = "ISO-8859-1";

  private PropertiesUtils() {
    // prevents instantiation
  }

  /**
   * Read Properties from a file.  Decrypt passwords.
   *
   * @param propertiesFile Properties File to read
   * @return Properties as read from file
   * @throws PropertiesException if error reading file
   */
  public static Properties loadFromFile(File propertiesFile)
    throws PropertiesException {
    try {
      InputStream is =
          new BufferedInputStream(new FileInputStream(propertiesFile));
      try {
        return loadProperties(is);
      } finally {
        is.close();
      }
    } catch (Exception e) {
      throw new PropertiesException("Unable to load Properties from file "
                                    + propertiesFile.getPath(), e);
    }
  }

  /**
   * Write the properties to a file.  Encrypt passwords,
   * version the properties.
   *
   * @param properties Properties to write
   * @param propertiesFile File to write properties to
   * @param comment optional comment String to pass to Properties.store()
   * @throws PropertiesException if error writing to file
   */
  public static void storeToFile(Properties properties, File propertiesFile,
      String comment) throws PropertiesException {
    try {
      FileOutputStream fos = new FileOutputStream(propertiesFile);
      try {
        storeProperties(properties, fos, comment);
      } finally {
        fos.close();
      }
    } catch (Exception e) {
      throw new PropertiesException("Unable to store Properties to file "
                                    + propertiesFile.getPath(), e);
    }
  }

  /**
   * Store a set of Properties to a String.  This is effectively
   * java.util.Properties.store(StringOutputStream), if there were
   * such a thing as StringOutputStream.  The returned string is
   * suitable for loading back into as set of Properties using
   * fromString(String).
   *
   * @param properties to encode into a String
   * @param comment optional comment string to pass to Properties.store()
   * @return a String object with containing the properties.
   * @throws PropertiesException
   */
  public static String storeToString(Properties properties, String comment)
      throws PropertiesException {
    try {
      ByteArrayOutputStream os = null;
      try {
        os = new ByteArrayOutputStream();
        storeProperties(properties, os, comment);
        return os.toString(PROPERTIES_ENCODING);
      } finally {
        os.close();
      }
    } catch (IOException e) {
      throw new PropertiesException("Unable to encode Properties to String", e);
    }
  }

  /**
   * Load a set of Properties from a String.  This is effectively
   * java.util.Properties.load(StringInputStream), if there were
   * such a thing as StringInputStream.  This should be able to
   * load properties from strings created by toString();
   *
   * @param propertiesString
   * @return a Properties object, or null if null string
   * @throws PropertiesException
   */
  public static Properties loadFromString(String propertiesString)
      throws PropertiesException {
    if (propertiesString != null) {
      try {
        ByteArrayInputStream is = null;
        try {
          is = new ByteArrayInputStream(
             propertiesString.getBytes(PROPERTIES_ENCODING));
          return loadProperties(is);
        } finally {
          is.close();
        }
      } catch (IOException e) {
        throw new PropertiesException("Unable to decode Properties from String",
                                      e);
      }
    }
    return null;
  }

  /**
   * Read Properties from an InputStream.  Decrypt passwords.
   *
   * @param inputStream InputStream to read Properties from
   * @return Properties as read from inputStream
   * @throws PropertiesException
   */
  public static Properties loadProperties(InputStream inputStream)
      throws PropertiesException {
    if (inputStream == null) {
      return null;
    }
    Properties properties = new Properties();
    try {
      properties.load(inputStream);
    } catch (Exception e) {
      throw new PropertiesException("Error loading properties from stream", e);
    }

    // Decrypt stored passwords.
    decryptSensitiveProperties(properties);

    return properties;
  }

  /**
   * Write the properties to an OutputStream.  Encrypt passwords,
   * version the properties.
   *
   * @param properties Properties to write
   * @param outputStream OutputStream to write properties to
   * @param comment optional comment String
   * @throws PropertiesException if error writing to stream
   */
  public static void storeProperties(Properties properties,
      OutputStream outputStream, String comment) throws PropertiesException {
    if (properties == null) {
      return;
    }
    try {
      // Make a copy of the Properties before munging them.
      Properties props = copy(properties);
      stampPropertiesVersion(props);
      encryptSensitiveProperties(props);
      // If the comment contains embedded newlines, we must comment out each
      // subsequent line after the first, as Java Properties won't do it for us.
      if (comment != null && comment.indexOf('\n') > 0) {
        comment = comment.replaceAll("\n", "\n#");
      }
      props.store(outputStream, comment);
    } catch (Exception e) {
      throw new PropertiesException("Error storing properties to stream", e);
    }
  }

  /**
   * Make a Properties object from a Map, copying all the keys and values.
   *
   * @param sourceMap a Map representing properties key-value map
   * @return new Properties object that may be modified without altering
   *          the source properties.
   */
  public static Properties fromMap(Map<String, String> sourceMap) {
    if (sourceMap == null) {
      return null;
    }
    Properties properties = new Properties();
    properties.putAll(sourceMap);
    return properties;
  }

  /**
   * Make a Map&lt;String, String&gt; from the supplied Properties,
   * copying all the keys and values.
   *
   * @param sourceProperties Properties representing properties key-value map.
   * @return a Map&lt;String, String&gt; representation of the source
   *          Properties.
   */
  public static Map<String, String> toMap(Properties sourceProperties) {
    if (sourceProperties == null) {
      return null;
    }
    Map<String, String> configMap = new HashMap<String, String>();
    Iterator<?> iter = sourceProperties.keySet().iterator();
    while (iter.hasNext()) {
      String key = (String) iter.next();
      configMap.put(key, sourceProperties.getProperty(key));
    }
    return configMap;
  }

  /**
   * Make a deep copy of a Properties object - copying all
   * the keys and values.  This is in contrast to java.util.Propeties.copy(),
   * which makes a shallow copy.
   *
   * @param sourceProperties a source set of Properties.
   * @return new Properties object that may be modified without altering
   * the source properties.
   */
  public static Properties copy(Properties sourceProperties) {
    Properties props = new Properties();
    if (sourceProperties != null) {
      props.putAll(sourceProperties);
    }
    return props;
  }

  /**
   * Encrypt Properties values that may be sensitive.  At this point,
   * any property that has the case-insensitive substring 'password'
   * in the key is considered sensitive.  Encrypting sensitive properties
   * is advisable when storing or transmitting properties in plain text.
   *
   * @param properties a set of Properties.
   */
  public static void encryptSensitiveProperties(Properties properties) {
    EncryptedPropertyPlaceholderConfigurer.encryptSensitiveProperties(properties);
  }

  /**
   * Decrypt Properties values that may be sensitive.  At this point,
   * any property that has the case-insensitive substring 'password'
   * in the key is considered sensitive.  This decrypts a set of
   * properties that was encrypted via encryptSensitiveProperties();
   *
   * @param properties a set of Properties.
   */
  public static void decryptSensitiveProperties(Properties properties) {
    EncryptedPropertyPlaceholderConfigurer.decryptSensitiveProperties(properties);
  }

  /**
   * Stamp the Properties set with the current Properties Version.
   *
   * @param properties a set of Properties.
   */
  public static void stampPropertiesVersion(Properties properties) {
    properties.put(GOOGLE_PROPERTIES_VERSION,
        Integer.toString(GOOGLE_PROPERTIES_VERSION_NUMBER));
  }

  /**
   * Retrieve the Properties Version stamp from this Properties set.
   *
   * @param properties a set of Properties.
   */
  public static int getPropertiesVersion(Properties properties) {
    String versionStr = properties.getProperty(
        GOOGLE_PROPERTIES_VERSION, "0");
    int version = 0;
    try {
      version = Integer.parseInt(versionStr);
      if (version > GOOGLE_PROPERTIES_VERSION_NUMBER) {
        LOGGER.warning("Properties appear to have been written by a newer "
            + "version of Connector Manager (" + version + ")");
      }
    } catch (NumberFormatException e) {
      LOGGER.warning("Invalid Properties Version: " + versionStr);
    }
    return version;
  }
}
