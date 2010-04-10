// Copyright (C) 2009 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.instantiator;

import com.google.enterprise.connector.logging.NDC;
import com.google.enterprise.connector.pusher.PusherFactory;
import com.google.enterprise.connector.scheduler.LoadManagerFactory;

import java.io.File;
import java.io.FileFilter;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility functions for operations on a {@link ConcurrentMap} of
 * {@link String} connector coordinator name to {@link ConnectorCoordinator}
 * Objects.
 */
class ConnectorCoordinatorMapHelper {
  private static final Logger LOGGER =
      Logger.getLogger(ConnectorCoordinatorMapHelper.class.getName());

  private ConnectorCoordinatorMapHelper() { // Prevents instantiation.
  }

  /**
   * Initializes <b>instanceMap</b> to contain a {@link ConnectorCoordinator}
   * for each connector defined in the provided {@link TypeMap}.
   *
   * @param pusherFactory creates instances of
   *        {@link com.google.enterprise.connector.pusher.Pusher Pusher}
   *        for pushing documents to the GSA.
   * @param loadManagerFactory creates instances of
   *  {@link com.google.enterprise.connector.scheduler.LoadManager LoadManager}
   *        used for controlling feed rate.
   * @param threadPool the {@link ThreadPool} for running traversals.
   */
  static void fillFromTypes(TypeMap typeMap,
      ConcurrentMap<String, ConnectorCoordinator> instanceMap,
      PusherFactory pusherFactory, LoadManagerFactory loadManagerFactory,
      ThreadPool threadPool) {
    for (String typeName : typeMap.keySet()) {
      TypeInfo typeInfo = typeMap.getTypeInfo(typeName);
      if (typeInfo == null) {
        LOGGER.log(Level.WARNING, "Skipping " + typeName);
        continue;
      }
      processTypeDirectory(instanceMap, typeInfo, pusherFactory,
                           loadManagerFactory, threadPool);
    }
  }

  /**
   * Initializes <b>instanceMap</b> to contain a {@link ConnectorCoordinator}
   * for each connector defined in the provided {@link TypeInfo}.
   *
   * @param pusherFactory creates instances of
   *        {@link com.google.enterprise.connector.pusher.Pusher Pusher}
   *        for pushing documents to the GSA.
   * @param loadManagerFactory creates instances of
   *  {@link com.google.enterprise.connector.scheduler.LoadManager LoadManager}
   *        used for controlling feed rate.
   * @param threadPool the {@link ThreadPool} for running traversals.
   */
  private static void processTypeDirectory(
      ConcurrentMap<String, ConnectorCoordinator> instanceMap,
      TypeInfo typeInfo, PusherFactory pusherFactory,
      LoadManagerFactory loadManagerFactory, ThreadPool threadPool) {
    File typeDirectory = typeInfo.getConnectorTypeDir();

    // Find the subdirectories.
    FileFilter fileFilter = new FileFilter() {
      public boolean accept(File file) {
        return file.isDirectory() && !file.getName().startsWith(".");
      }
    };
    File[] directories = typeDirectory.listFiles(fileFilter);

    if (directories == null) {
      // This just means the directory is empty.
      return;
    }

    // Process each one.
    for (int i = 0; i < directories.length; i++) {
      File directory = directories[i];
      String name = directory.getName();
      NDC.pushAppend(name);
      try {
        InstanceInfo instanceInfo =
            InstanceInfo.fromDirectory(name, directory, typeInfo);
        if (instanceInfo != null) {
          ConnectorCoordinator fromType = new ConnectorCoordinatorImpl(
              instanceInfo, pusherFactory, loadManagerFactory, threadPool);
          ConnectorCoordinator current =
              instanceMap.putIfAbsent(name, fromType);
          if (current != null) {
            throw new IllegalStateException(
                "Connector instance modified during initialization");
          }
        }

      } catch (InstantiatorException e) {
        LOGGER.log(Level.WARNING, "Problem creating connector instance", e);
      } finally {
        NDC.pop();
      }
    }
  }
}
