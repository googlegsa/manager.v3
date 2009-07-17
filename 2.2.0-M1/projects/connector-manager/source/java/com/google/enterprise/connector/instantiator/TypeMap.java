// Copyright (C) 2006-2009 Google Inc.
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

import com.google.enterprise.connector.common.JarUtils;
import com.google.enterprise.connector.manager.Context;

import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class keeps track of the installed connector types and maintains a
 * corresponding directory structure.
 */
public class TypeMap extends TreeMap<String, TypeInfo> {

  private static final String CONNECTOR_TYPE_PATTERN =
      "classpath*:config/connectorType.xml";

  private static final Logger LOGGER =
      Logger.getLogger(TypeMap.class.getName());

  public TypeMap() {
    initialize(CONNECTOR_TYPE_PATTERN, null);
  }

  /**
   * For testing only. Either parameter may be null, in which case the default
   * is used.
   *
   * @param connectorTypePattern used instead of normal default
   * @param baseDirPath
   */
  public TypeMap(String connectorTypePattern, String baseDirPath) {
    initialize(connectorTypePattern, baseDirPath);
  }

  private File baseDirectory = null;
  private File typesDirectory = null;

  private void initialize(String connectorTypePattern, String baseDirPath) {
    initializeTypes(connectorTypePattern);
    initializeBaseDirectories(baseDirPath);
    initializeTypeDirectories();
  }

  private void initializeTypes(String connectorTypePattern) {
    ApplicationContext ac = Context.getInstance().getApplicationContext();

    Resource[] resourceArray;
    try {
      resourceArray = ac.getResources(connectorTypePattern);
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "IOException from Spring while getting "
          + connectorTypePattern
          + " resources.  No connector types can be found", e);
      return;
    }

    if (resourceArray.length == 0) {
      LOGGER.info("No connector types found.");
      return;
    }

    for (Resource r : resourceArray) {
      TypeInfo typeInfo = TypeInfo.fromSpringResource(r);
      if (typeInfo == null) {
        LOGGER.log(Level.WARNING, "Skipping " + r.getDescription());
        continue;
      }
      this.put(typeInfo.getConnectorTypeName(), typeInfo);
      LOGGER.info("Found connector type: " + typeInfo.getConnectorTypeName()
          + "  version: "
          + JarUtils.getJarVersion(typeInfo.getConnectorType().getClass()));
    }
  }

  private void initializeDefaultBaseDirectory() {
    String commonDirPath = Context.getInstance().getCommonDirPath();
    baseDirectory = new File(commonDirPath);
  }

  private void initializeBaseDirectories(String baseDirPath) {
    if (baseDirPath == null) {
      initializeDefaultBaseDirectory();
    } else {
      baseDirectory = new File(baseDirPath);
    }

    typesDirectory = new File(baseDirectory, "connectors");
    if (!typesDirectory.exists()) {
      if (!typesDirectory.mkdirs()) {
        throw new IllegalStateException("Can't create connector types directory "
            + typesDirectory.getPath());
      }
    }

    if (!typesDirectory.isDirectory()) {
      throw new IllegalStateException("Unexpected file "
          + typesDirectory.getPath() + " blocks creation of types directory");
    }
  }

  public TypeInfo getTypeInfo(String connectorTypeName) {
    return this.get(connectorTypeName);
  }

  private void initializeTypeDirectories() {
    for (Iterator<String> iter = keySet().iterator(); iter.hasNext(); ) {
      String typeName = iter.next();
      TypeInfo typeInfo = getTypeInfo(typeName);
      File connectorTypeDir = new File(typesDirectory, typeName);
      if (!connectorTypeDir.exists()) {
        if(!connectorTypeDir.mkdirs()) {
          LOGGER.warning("Type " + typeName
              + " has a valid definition but no type directory - skipping it");
          iter.remove();
          return;
        }
      }
      if (!typesDirectory.isDirectory()) {
        LOGGER.warning("Unexpected file " + connectorTypeDir.getPath()
            + " blocks creation of instances directory for type " + typeName
            + " - skipping it");
        iter.remove();
      } else {
        typeInfo.setConnectorTypeDir(connectorTypeDir);
        LOGGER.info("Connector type: " + typeInfo.getConnectorTypeName()
            + " has directory " + connectorTypeDir.getAbsolutePath());
      }
    }
  }
}
