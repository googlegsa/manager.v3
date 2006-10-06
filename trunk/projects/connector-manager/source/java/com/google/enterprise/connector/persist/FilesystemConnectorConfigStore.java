// Copyright (C) 2006 Google Inc.
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

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.logging.Logger;

/**
 * Uses the file system to store connector configurations. In this
 * implementation, there is a base directory, then sub-directories named after
 * the connectorType. Individual connector configs are stored in the appropriate
 * sub-directory for their type.
 */
public class FilesystemConnectorConfigStore implements ConnectorConfigStore {

  private static final Logger LOGGER =
    Logger.getLogger(FilesystemConnectorConfigStore.class.getName());

  File baseDirectory = null;
  Map connectorConfigMap = null;
  Map connectorTypeMap = null;
  boolean initialized = false;

  public FilesystemConnectorConfigStore() {

  }

  /**
   * @param baseDirectory the baseDirectory to set
   */
  public void setBaseDirectory(File baseDirectory) {
    this.baseDirectory = baseDirectory;
    try {
      LOGGER.info("Base dir path: "+ baseDirectory.getCanonicalPath());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void discoverExistingConnectorTypes() {

    // find the first-level subdirectories
    FileFilter fileFilter = new FileFilter() {
      public boolean accept(File file) {
        return file.isDirectory();
      }
    };
    File[] subDirectories = baseDirectory.listFiles(fileFilter);

    if (subDirectories == null) {
      // if the directory is empty you could get null...
      return;
    }
    
    // remember them along with their base names (connectorTypeName)
    for (int i = 0; i < subDirectories.length; i++) {
      File thisSubDir = subDirectories[i];
      String connectorTypeName = thisSubDir.getName();
      if (!connectorTypeName.startsWith(".")) {
        // skipping anything that begins with .
        connectorTypeMap.put(connectorTypeName, thisSubDir);
      }
    }
  }

  private void discoverExistingConnectors(String connectorTypeName,
      File connectorTypeDir) {

    // find the first-level normal files
    FileFilter fileFilter = new FileFilter() {
      public boolean accept(File file) {
        return file.isFile();
      }
    };
    File[] normalFiles = connectorTypeDir.listFiles(fileFilter);

    // remember them along with other interesting info about them
    for (int i = 0; i < normalFiles.length; i++) {
      File thisFile = normalFiles[i];
      String fileName = thisFile.getName();
      if (!fileName.startsWith(".") && fileName.endsWith(".xml")
          && fileName.length() > 4) {
        String connectorName = fileName.substring(0, fileName.length() - 4);
        ConnectorConfigInfo info =
            new ConnectorConfigInfo(connectorTypeName, connectorName, thisFile);
        connectorConfigMap.put(connectorName, info);
      }
    }
  }

  private void initialize() {
    if (initialized) {
      return;
    }
    if (baseDirectory == null) {
      throw new IllegalStateException();
    }

    connectorTypeMap = new TreeMap();
    discoverExistingConnectorTypes();

    connectorConfigMap = new TreeMap();
    for (Iterator i = connectorTypeMap.entrySet().iterator(); i.hasNext();) {
      Entry e = (Entry) i.next();
      String connectorTypeName = (String) e.getKey();
      File connectorTypeDir = (File) e.getValue();
      discoverExistingConnectors(connectorTypeName, connectorTypeDir);
    }
    initialized = true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.enterprise.connector.persist.ConnectorConfigStore#getConnectorNames()
   */
  public Iterator getConnectorNames() {
    initialize();
    return Collections.unmodifiableSet(connectorConfigMap.keySet()).iterator();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.enterprise.connector.persist.ConnectorConfigStore#getConnectorType(java.lang.String)
   */
  public String getConnectorTypeName(String connectorName)
      throws ConnectorNotFoundException {
    initialize();
    ConnectorConfigInfo result =
        (ConnectorConfigInfo) connectorConfigMap.get(connectorName);
    if (result == null) {
      throw new ConnectorNotFoundException(connectorName);
    }
    return result.getConnectorTypeName();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.enterprise.connector.persist.ConnectorConfigStore#getConnectorResourceString(java.lang.String)
   */
  public String getConnectorResourceString(String connectorName)
      throws ConnectorNotFoundException, PersistentStoreException {
    initialize();
    ConnectorConfigInfo result =
        (ConnectorConfigInfo) connectorConfigMap.get(connectorName);
    if (result == null) {
      throw new ConnectorNotFoundException(connectorName);
    }
    try {
      return result.getConnectorFile().getCanonicalPath();
    } catch (IOException e) {
      throw new PersistentStoreException(e);
    }
  }

  public void dropConnectorFromStore(String connectorName) {
    initialize();
    ConnectorConfigInfo info =
        (ConnectorConfigInfo) connectorConfigMap.get(connectorName);
    if (info == null) {
      return;
    }
    File connectorFile = info.getConnectorFile();
    if (connectorFile != null) {
      connectorFile.delete();
      // TODO(ziff): check the success of this call
    }
    connectorConfigMap.remove(connectorName);
  }

  public void setConnectorConfig(String connectorName,
      String connectorTypeName, String newConfig) throws PersistentStoreException {
    File typeDir = new File(baseDirectory, connectorTypeName);
    typeDir.mkdirs();
    File connectorConfig = new File(typeDir, connectorName + ".xml");
    try {
      connectorConfig.createNewFile();
    } catch (IOException e) {
      throw new PersistentStoreException(e);
    }
    OutputStream os = null;
    try {
      os = new FileOutputStream(connectorConfig);
    } catch (FileNotFoundException e) {
      throw new PersistentStoreException(e);
    }
    OutputStreamWriter osw = new OutputStreamWriter(os);
    try {
      osw.write(newConfig);
    } catch (IOException e) {
      throw new PersistentStoreException(e);
    } finally {
      try {
        osw.close();
      } catch (IOException e) {
        throw new PersistentStoreException(e);
      }
    }
    ConnectorConfigInfo info =
      new ConnectorConfigInfo(connectorTypeName, connectorName, connectorConfig);
    connectorConfigMap.put(connectorName, info);
  }

  static class ConnectorConfigInfo {
    String connectorTypeName;
    String connectorName;
    File connectorFile;

    public ConnectorConfigInfo(String connectorTypeName, String connectorName,
        File thisFile) {
      this.connectorTypeName = connectorTypeName;
      this.connectorName = connectorName;
      this.connectorFile = thisFile;
    }

    /**
     * @return the connectorName
     */
    public String getConnectorName() {
      return connectorName;
    }

    /**
     * @return the connectorTypeName
     */
    public String getConnectorTypeName() {
      return connectorTypeName;
    }

    /**
     * @return the resourceString
     */
    public File getConnectorFile() {
      return connectorFile;
    }

  }

}
