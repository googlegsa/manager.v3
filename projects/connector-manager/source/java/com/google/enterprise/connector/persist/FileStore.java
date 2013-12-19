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

package com.google.enterprise.connector.persist;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.enterprise.connector.common.PropertiesException;
import com.google.enterprise.connector.common.PropertiesUtils;
import com.google.enterprise.connector.instantiator.Configuration;
import com.google.enterprise.connector.instantiator.TypeInfo;
import com.google.enterprise.connector.instantiator.TypeMap;
import com.google.enterprise.connector.scheduler.Schedule;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manage persistence for schedule and state and configuration
 * for a named connector. The persistent store for these data items
 * are files in the connector's work directory.
 */
public class FileStore implements PersistentStore {

  private static final Logger LOGGER =
      Logger.getLogger(FileStore.class.getName());

  private static final String schedName = "_schedule.txt";
  private static final String stateName = "_state.txt";
  private static final String configName = ".properties";

  private TypeMap typeMap;

  public void setTypeMap(TypeMap typeMap) {
    this.typeMap = typeMap;
  }

  /* @Override */
  public boolean isDisabled() {
    return (typeMap == null);
  }

  /**
   * Gets the version stamps of all persistent objects.
   *
   * @return an immutable map containing the version stamps; may be
   * empty but not {@code null}
   */
  /* @Override */
  public ImmutableMap<StoreContext, ConnectorStamps> getInventory() {
    Preconditions.checkNotNull(typeMap, "FileStore requires a TypeMap");
    ImmutableMap.Builder<StoreContext, ConnectorStamps> mapBuilder =
        new ImmutableMap.Builder<StoreContext, ConnectorStamps>();
    File[] directories =
        typeMap.getTypesDirectory().listFiles(CONNECTOR_TYPE_FILTER);
    if (directories != null) {
      for (File typeDirectory : directories) {
        processTypeDir(typeDirectory, mapBuilder);
      }
    }
    return mapBuilder.build();
  }

  // Find the subdirectories.
  static FileFilter CONNECTOR_TYPE_FILTER = new FileFilter() {
    public boolean accept(File file) {
      return file.isDirectory() && !file.getName().startsWith(".");
    }
  };

  private void processTypeDir(File typeDirectory,
      ImmutableMap.Builder<StoreContext, ConnectorStamps> mapBuilder) {
    String typeName = typeDirectory.getName();
    File[] directories = typeDirectory.listFiles(CONNECTOR_TYPE_FILTER);
    if (directories == null) {
      // This means the directory is empty - no connector instances.
      LOGGER.fine("No connectors of type " + typeName + " found.");
      return;
    }

    // Process each connector store.
    for (File directory : directories) {
      String name = directory.getName();
      StoreContext context = new StoreContext(name, typeName);
      FileStamp checkpointStamp =
          getStamp(context, getStoreFileName(context, stateName));
      FileStamp scheduleStamp =
          getStamp(context, getStoreFileName(context, schedName));
      FileStamp configurationStamp = new FileStamp(
          // ConfigurationStamp is the sum of the map and xml timestamps.
          getStoreFile(context, TypeInfo.CONNECTOR_INSTANCE_XML).lastModified()
          + getStoreFile(context, getStoreFileName(context, configName))
                .lastModified()
          );
      if (checkpointStamp.version != 0L || scheduleStamp.version != 0L
          || configurationStamp.version != 0L) {
        ConnectorStamps stamps = new ConnectorStamps(
            checkpointStamp, configurationStamp, scheduleStamp);
        mapBuilder.put(context, stamps);
        if (LOGGER.isLoggable(Level.FINE)) {
          LOGGER.fine("Found connector: name = " + name + "  type = " + typeName
                      + "  stamps = " + stamps);
        }
      }
    }
  }

  private FileStamp getStamp(StoreContext context, String filename) {
    return new FileStamp(getStoreFile(context, filename).lastModified());
  }

  /**
   * A version stamp based upon a {@code long File.lastModified()}.
   */
  private static class FileStamp implements Stamp {
    final long version;

    /** Constructs a File version stamp. */
    FileStamp(long version) {
      this.version = version;
    }

    /** {@inheritDoc} */
    /* @Override */
    public int compareTo(Stamp other) {
      return (int) (version - ((FileStamp) other).version);
    }

    @Override
    public String toString() {
      return Long.toString(version);
    }
  }

  /**
   * Retrieves connector schedule.
   *
   * @param context a StoreContext
   * @return connectorSchedule schedule of the corresponding connector.
   */
  /* @Override */
  public Schedule getConnectorSchedule(StoreContext context) {
    testStoreContext(context);
    return Schedule.of(
        readStoreFile(context, getStoreFileName(context, schedName)));
  }

  /**
   * Stores connector schedule.
   *
   * @param context a StoreContext
   * @param connectorSchedule schedule of the corresponding connector.
   */
  /* @Override */
  public void storeConnectorSchedule(StoreContext context,
                                     Schedule connectorSchedule) {
    if (connectorSchedule == null) {
      // We can't write null state to file, so just remove it.
      removeConnectorSchedule(context);
      return;
    }
    testStoreContext(context);
    writeStoreFile(context, getStoreFileName(context, schedName),
                   connectorSchedule.toString());
  }

  /**
   * Remove a connector schedule.  If no such connector exists, do nothing.
   *
   * @param context a StoreContext
   */
  /* @Override */
  public void removeConnectorSchedule(StoreContext context) {
    testStoreContext(context);
    deleteStoreFile(context, getStoreFileName(context, schedName));
  }

  /**
   * Gets the stored state of a named connector.
   *
   * @param context a StoreContext
   * @return the state, or null if no state has been stored for this connector.
   */
  /* @Override */
  public String getConnectorState(StoreContext context) {
    testStoreContext(context);
    return readStoreFile(context, getStoreFileName(context, stateName));
  }

  /**
   * Stores connector state.
   *
   * @param context a StoreContext
   * @param connectorState state of the corresponding connector
   */
  /* @Override */
  public void storeConnectorState(StoreContext context, String connectorState) {
    if (connectorState == null) {
      // We can't write null state to file, so just remove it.
      removeConnectorState(context);
      return;
    }
    testStoreContext(context);
    writeStoreFile(context, getStoreFileName(context, stateName),
                   connectorState);
  }

  /**
   * Remove connector state.  If no such connector exists, do nothing.
   *
   * @param context a StoreContext
   */
  /* @Override */
  public void removeConnectorState(StoreContext context) {
    testStoreContext(context);
    deleteStoreFile(context, getStoreFileName(context, stateName));
  }


  /**
   * Gets the stored configuration of a named connector.
   *
   * @param context a StoreContext
   * @return the configuration Properties, or null if no configuration
   *         has been stored for this connector.
   */
  /* @Override */
  public Configuration getConnectorConfiguration(StoreContext context) {
    testStoreContext(context);
    File propFile = getStoreFile(context, getStoreFileName(context, configName));
    Properties props = null;
    if (propFile.exists()) {
      try {
        props = PropertiesUtils.loadFromFile(propFile);
      } catch (PropertiesException e) {
        LOGGER.log(Level.WARNING, "Failed to read connector configuration for "
                   + context.getConnectorName(), e);
        return null;
      }
    }
    String xml = readStoreFile(context, TypeInfo.CONNECTOR_INSTANCE_XML);
    if (props != null || xml != null) {
      String typeName = context.getTypeName();
      return new Configuration(typeName, PropertiesUtils.toMap(props), xml);
    }
    return null;
  }

  /**
   * Stores the configuration of a named connector.
   *
   * @param context a StoreContext
   * @param configuration Properties to store
   */
  /* @Override */
  public void storeConnectorConfiguration(StoreContext context,
      Configuration configuration) {
    if (configuration == null) {
      removeConnectorConfiguration(context);
      return;
    }
    testStoreContext(context);
    if (configuration.getXml() == null) {
      deleteStoreFile(context, TypeInfo.CONNECTOR_INSTANCE_XML);
    } else {
      writeStoreFile(context, TypeInfo.CONNECTOR_INSTANCE_XML,
                     configuration.getXml());
    }
    String propName = getStoreFileName(context, configName);
    if (configuration.getMap() == null) {
      deleteStoreFile(context, propName);
    } else {
      Properties properties = PropertiesUtils.fromMap(configuration.getMap());
      File propFile = getStoreFile(context, propName);
      String header = "Configuration for Connector "
          + context.getConnectorName();
      try {
        PropertiesUtils.storeToFile(properties, propFile, header);
      } catch (PropertiesException e) {
        LOGGER.log(Level.WARNING, "Failed to store connector configuration for "
            + context.getConnectorName(), e);
      }
    }
  }

  /**
   * Remove a stored connector configuration.  If no such connector exists,
   * do nothing.
   *
   * @param context a StoreContext
   */
  /* @Override */
  public void removeConnectorConfiguration(StoreContext context) {
    testStoreContext(context);
    deleteStoreFile(context, getStoreFileName(context, configName));
    deleteStoreFile(context, TypeInfo.CONNECTOR_INSTANCE_XML);
  }

  /**
   * Test the StoreContext to make sure it is sane.
   *
   * @param context a StoreContext
   */
  private void testStoreContext(StoreContext context) {
    Preconditions.checkNotNull(context, "StoreContext may not be null.");
    Preconditions.checkNotNull(typeMap, "FileStore requires a TypeMap.");
    // The StoreContext ConnectorName and TypeName are now checked as
    // Preconditions on the StoreContext constructor.
  }

  /**
   * Return a filename for the store file.
   *
   * @param context a StoreContext
   * @param suffix String to append to file name
   */
  private static String getStoreFileName(StoreContext context, String suffix) {
    return context.getConnectorName() + suffix;
  }

  /**
   * Return a File object representing the on-disk store.
   *
   * @param context a StoreContext
   * @param filename Filename of the on-disk store file.
   */
  private File getStoreFile(StoreContext context, String filename) {
    File typeDirectory =
        new File(typeMap.getTypesDirectory(), context.getTypeName());
    File connectorDir = new File(typeDirectory, context.getConnectorName());
    return new File(connectorDir, filename);
  }

  /**
   * Delete a store file.
   *
   * @param context a StoreContext
   * @param filename Filename of the on-disk store file.
   */
  private void deleteStoreFile(StoreContext context, String filename) {
    getStoreFile(context, filename).delete();
  }

  /**
   * Write the data to a store file.
   *
   * @param context a StoreContext
   * @param data to write to file
   */
  private void writeStoreFile(StoreContext context, String filename,
      String data) {
    FileOutputStream fos = null;
    File storeFile = null;
    try {
      storeFile = getStoreFile(context, filename);
      // Make sure the connectorDir exists.
      File connectorDir = storeFile.getParentFile();
      if (!connectorDir.exists()) {
        connectorDir.mkdirs();
      }
      fos = new FileOutputStream(storeFile);
      fos.write(data.getBytes());
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Cannot write store file "
          + storeFile + " for connector " + context.getConnectorName(), e);
    } finally {
      if (fos != null) {
        try {
          fos.close();
        } catch (IOException e) {
          LOGGER.log(Level.WARNING, "Error closing store file "
              + storeFile + " for connector " + context.getConnectorName(), e);
        }
      }
    }
  }

  /**
   * Read a store file, returning a String containing the contents.
   *
   * @param context a StoreContext
   * @param filename Filename of the on-disk store file
   * @return String containing store file contents or null if none exists.
   */
  private String readStoreFile(StoreContext context, String filename) {
    FileInputStream fis = null;
    File storeFile = null;
    try {
      storeFile = getStoreFile(context, filename);
      int length = (int) storeFile.length();
      if (length == 0) {
        return (storeFile.exists() ? "" : null);
      }
      byte[] buffer = new byte[length];
      fis = new FileInputStream(storeFile);
      int bytesRead = fis.read(buffer);
      return new String(buffer, 0, bytesRead);
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Cannot read store file "
          + storeFile + " for connector " + context.getConnectorName(), e);
      return null;
    } finally {
      if (fis != null) {
        try {
          fis.close();
        } catch (IOException e1) {
          LOGGER.log(Level.WARNING, "Error closing store file " + storeFile
                     + " for connector " + context.getConnectorName(), e1);
        }
      }
    }
  }
}
