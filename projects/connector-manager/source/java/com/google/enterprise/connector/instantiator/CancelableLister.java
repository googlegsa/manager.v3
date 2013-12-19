// Copyright 2011 Google Inc.
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
import com.google.enterprise.connector.spi.Lister;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A {@link Cancelable} {@link Lister}
 */
public class CancelableLister implements Cancelable {
  private static final Logger LOGGER =
      Logger.getLogger(CancelableLister.class.getName());

  private final String connectorName;
  private final Lister lister;

  /**
   * Creates a new {@link Cancelable} {@link Lister} that could be run in a
   * separate thread.
   *
   * @param connectorName the connector name.
   * @param lister a {@link Lister}.
   */
  public CancelableLister(String connectorName, Lister lister) {
    this.connectorName = connectorName;
    this.lister = lister;
  }

  /** Start up the {@link Lister}. */
  /* @Override */
  public void run() {
    NDC.push("Lister " + connectorName);
    try {
      LOGGER.fine("Start Lister for connector " + connectorName);
      lister.start();
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "Failed to start Lister for connector "
                 + connectorName, e);
    } finally {
      NDC.remove();
    }
  }

  /** Shut down the {@link Lister}. */
  /* @Override */
  public void cancel() {
    try {
      LOGGER.fine("Stop Lister " + connectorName);
      lister.shutdown();
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "Failed to stop Lister for connector "
                 + connectorName, e);
    }
  }
}

