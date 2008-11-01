// Copyright (C) 2008 Google Inc.
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

import com.google.enterprise.connector.instantiator.EncryptedPropertyPlaceholderConfigurer;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.Map;
import java.util.Properties;

public class PropertiesUtils {
  
  private static final Logger LOGGER =
      Logger.getLogger(PropertiesUtils.class.getName());

  public static final String GOOGLE_PROPERTIES_FORMAT =
      "googlePropertiesFileFormat";
  public static final String GOOGLE_PROPERTIES_FORMAT_XML = "XML";

  public static final String GOOGLE_CONNECTOR_WORK_DIR =
      "googleConnectorWorkDir";
  public static final String GOOGLE_WORK_DIR = "googleWorkDir";
  public static final String GOOGLE_PROPERTIES_VERSION = 
      "googlePropertiesVersion";
  public static final int GOOGLE_PROPERTIES_VERSION_NUMBER = 1;

  private PropertiesUtils() {
    // prevents instantiation
  }

  /**
   * Read Properties from a file.  Supports both XML and
   * traditional Properties file formats.  Decrypt passwords.
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
   * version the properties, and try write them out in the
   * the same format they were read (XML or traditional).
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
   * @returns a String object with containing the properties.
   * @throws PropertiesException
   */
  public static String storeToString(Properties properties, String comment) 
      throws PropertiesException {
    try {
      ByteArrayOutputStream os = null;
      try {
        os = new ByteArrayOutputStream();
        storeProperties(properties, os, comment);
        return os.toString();
      } finally {
        os.close();
      }
    } catch (IOException e) {
      throw new PropertiesException("Unable to encode Properties to a String",
                                    e);
    }
  }

  /**
   * Load a set of Properties from a String.  This is effectively
   * java.util.Properties.load(StringInputStream), if there were
   * such a thing as StringInputStream.  This should be able to
   * load properties from strings created by toString();
   *
   * @param propertiesString 
   * @returns a Properties object, or null if null string
   * @throws PropertiesException
   */
  public static Properties loadFromString(String propertiesString)
      throws PropertiesException {
    if (propertiesString != null) {
      try {
        ByteArrayInputStream is = null;
        try {
          is = new ByteArrayInputStream(propertiesString.getBytes()); 
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
   * Read Properties from an InputStream.  The InputStream must 
   * support mark() and reset().  Supports both XML and
   * traditional Properties file formats.  Decrypt passwords.
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
    if (!inputStream.markSupported()) {
      throw new PropertiesException(
          "Properties InputStream must support mark() and reset() methods.");
    }
    Properties properties = new Properties();
    try {
      // Peek at the head of the Properties file to determine 
      // if it is in XML format or Traditional format.
      inputStream.mark(1024);
      byte[] buffer = new byte[512];
      int bytesRead = inputStream.read(buffer, 0, buffer.length);
      String propHeader = new String(buffer, 0, bytesRead);
      inputStream.reset();
    
      // Determine which Properties loader method to use.
      if (propHeader.indexOf("<?xml ") >= 0) {
        properties.loadFromXML(inputStream);
        properties.put(GOOGLE_PROPERTIES_FORMAT, GOOGLE_PROPERTIES_FORMAT_XML);
      } else {
        properties.load(inputStream);
      }
    } catch (Exception e) {
      throw new PropertiesException("Error loading properties from stream", e);
    }
      
    // Decrypt stored passwords.
    decryptSensitiveProperties(properties);
    
    return properties;
  }

  /**
   * Write the properties to an OutputStream.  Encrypt passwords, 
   * version the properties, and try write them out in the
   * the same format they were read (XML or traditional).
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
      if (GOOGLE_PROPERTIES_FORMAT_XML.equalsIgnoreCase(
          (String) props.remove(GOOGLE_PROPERTIES_FORMAT))) {
        props.storeToXML(outputStream, comment);
      } else {
        props.store(outputStream, comment);
      }
    } catch (Exception e) {
      throw new PropertiesException("Error storing properties to stream", e);
    }
  }

  /**
   * Make a Properties object from a Map, copying all the keys and values.
   * 
   * @param sourceMap a Map representing properties key-value map
   * @returns new Properties object that may be modified without altering
   *          the source properties.
   */
  public static Properties fromMap(Map sourceMap) {
    if (sourceMap == null) {
      return null;
    }
    Properties properties = new Properties();
    properties.putAll(sourceMap);
    return properties;
  }

  /**
   * Make a deep copy of a Properties object - copying all
   * the keys and values.  This is in contrast to java.util.Propeties.copy(),
   * which makes a shallow copy.
   *
   * @param sourceProperties a source set of Properties.
   * @returns new Properties object that may be modified without altering
   * the source properties.
   */
  public static Properties copy(Properties sourceProperties) {
    return fromMap(sourceProperties);
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
