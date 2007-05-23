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

package com.google.enterprise.connector.instantiator;



import java.io.File;
import java.io.FileFilter;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.enterprise.connector.persist.ConnectorExistsException;
import com.google.enterprise.connector.persist.ConnectorNotFoundException;
import com.google.enterprise.connector.spi.ConfigureResponse;

/**
 * This class keeps track of the installed connector instances, maintains a
 * corresponding directory structure and maintains properties files that
 * implement the instances.
 */
public class InstanceMap extends TreeMap {

  private static final Logger LOGGER =
      Logger.getLogger(InstanceMap.class.getName());

  private TypeMap typeMap;

  public InstanceMap(TypeMap typeMap) {
    if (typeMap == null) {
      throw new IllegalArgumentException();
    }
    this.typeMap = typeMap;
    for (Iterator i = typeMap.keySet().iterator(); i.hasNext();) {
      String typeName = (String) i.next();
      TypeInfo typeInfo = typeMap.getTypeInfo(typeName);
      if (typeInfo == null) {
        LOGGER.log(Level.WARNING, "Skipping " + typeName);
        continue;
      }
      processTypeDirectory(typeInfo);
    }
  }

  private void processTypeDirectory(TypeInfo typeInfo) {

    File typeDirectory = typeInfo.getConnectorTypeDir();

    // find the subdirectories
    FileFilter fileFilter = new FileFilter() {
      public boolean accept(File file) {
        return file.isDirectory();
      }
    };
    File[] directories = typeDirectory.listFiles(fileFilter);

    if (directories == null) {
      // this just means the directory is empty
      return;
    }

    // process each one
    for (int i = 0; i < directories.length; i++) {
      File directory = directories[i];
      String name = directory.getName();
      if (!name.startsWith(".")) {
        InstanceInfo instanceInfo =
            InstanceInfo.fromDirectory(name, directory, typeInfo);
        if (instanceInfo != null) {
          this.put(name, instanceInfo);
        }
      }
    }
  }

  public InstanceInfo getInstanceInfo(String name) {
    return (InstanceInfo) get(name);
  }

  public ConfigureResponse updateConnector(
      String name, String typeName, Map config,
      Locale locale, boolean update)
      throws InstantiatorException, ConnectorNotFoundException,
      ConnectorExistsException {
    InstanceInfo instanceInfo = getInstanceInfo(name);
    ConfigureResponse response = null;
    if (instanceInfo == null) {
      if (update) {
        throw new ConnectorNotFoundException();  
      }
      response = createNewConnector(name, typeName, config, locale);
    } else {
      if (!update) {
        throw new ConnectorExistsException();
      }
      TypeInfo typeInfo = instanceInfo.getTypeInfo();
      String previousTypeName = typeInfo.getConnectorTypeName();
      if (previousTypeName.equals(typeName)) {
        File connectorDir = instanceInfo.getConnectorDir();
        response = resetConfig(name, connectorDir, typeInfo, config, locale);
      } else {
        // an existing connector is being given a new type - drop then add
        dropConnector(name);
        response = createNewConnector(name, typeName, config, locale);
        if (response != null) {
          // TODO: We need to restore original Connector config. This is
          // necessary once we allow update a Connector with new ConnectorType.
       	  LOGGER.severe("Failed to update Connector config." +
       		" Need to restore original Connector config.");
        }
      }
    }
    return response;
  }

  private ConfigureResponse createNewConnector(String name, String typeName,
	  Map config, Locale locale)
      throws InstantiatorException {
    TypeInfo typeInfo = typeMap.getTypeInfo(typeName);
    if (typeInfo == null) {
      throw new InstantiatorException();
    }
    File connectorDir = makeConnectorDirectory(name, typeInfo);
    return resetConfig(name, connectorDir, typeInfo, config, locale);
  }

  private ConfigureResponse resetConfig(String name, File connectorDir,
      TypeInfo typeInfo, Map config, Locale locale)
      throws InstantiatorException {
    InstanceInfo instanceInfo =
        InstanceInfo.fromNewConfig(name, connectorDir, typeInfo, config);
    if (instanceInfo == null) {
      // we don't expect this, because an InstantiatorException should have been
      // thrown, but just in case
      throw new InstantiatorException();
    }
    // now, validate the configuration.
    ConfigureResponse response =
    	typeInfo.getConnectorType().validateConfig(config, locale);
    if (response == null) {
    	this.put(name, instanceInfo);
    } else {
    	LOGGER.warning("An invalid config for connector \"" + name
    		+ "\" was rejected.");
    }
    return response; 
  }

  private File makeConnectorDirectory(String name, TypeInfo typeInfo)
      throws InstantiatorException {
    File connectorTypeDir = typeInfo.getConnectorTypeDir();
    File connectorDir = new File(connectorTypeDir, name);
    if (connectorDir.exists()) {
      if (connectorDir.isDirectory()) {
        // we don't know why this directory already exists, but we're ok with it
        LOGGER.warning("Connector directory "
            + connectorDir.getAbsolutePath()
            + "; already exists for connector" + name);
        return connectorDir;
      } else {
        throw new InstantiatorException("Existing file blocks creation of "
            + "connector directory at " + connectorDir.getAbsolutePath()
            + " for connector " + name);
      }
    } else {
      connectorDir.mkdirs();
      if (!connectorDir.exists()) {
        throw new InstantiatorException("Can't create "
            + "connector directory at " + connectorDir.getAbsolutePath()
            + " for connector " + name);
      }
    }
    return connectorDir;
  }

  public void dropConnector(String name) {
    InstanceInfo instanceInfo = this.getInstanceInfo(name);
    if (instanceInfo == null) {
      return;
    }
    File propertiesFile = instanceInfo.getPropertiesFile();
    if (propertiesFile.exists()) {
      if (!propertiesFile.delete()) {
        LOGGER.warning("Can't delete properties file "
            + propertiesFile.getPath()
            + "; this connector may be difficult to delete.");
        // we're unlikely to succeed in deleting the containing directory, but
        // we'll try anyway
      }
    }
    File connectorDir = instanceInfo.getConnectorDir();
    if (connectorDir.exists()) {
      if (!connectorDir.delete()) {
        LOGGER.warning("Can't delete connector directory "
            + connectorDir.getPath()
            + "; this connector may be difficult to delete.");
      }
    }
    this.remove(name);
  }
}
