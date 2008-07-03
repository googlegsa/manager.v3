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

package com.google.enterprise.connector.persist;

import java.util.HashMap;

/**
 * Wraps GenerationalStore over an instance of {@link ConnectorStateStore}.
 * This provides for "generations" of storage state.  Each
 * time a Traverser for a repository is created, it gets a
 * new generation number.  Reads and writes to older generations
 * will then fail.
 */
public class GenerationalStateStore implements ConnectorStateStore {

  // Holds the most current generations of connector instances.
  private static HashMap generations = new HashMap();

  // This instance's generations of connector instances.
  // May be older than the most current ones stored above.
  private HashMap myGenerations;

  // The underlying ConnectorStateStore
  private ConnectorStateStore baseStore;

  /**
   * Constructor wraps a ConnectorStateStore with a GenerationalStore.
   *
   * @param baseStore The underlying ConnectorStateStore to wrap.
   */
  public GenerationalStateStore(ConnectorStateStore baseStore) {
    this.myGenerations = new HashMap();
    this.baseStore = baseStore;
  }

  /**
   * Constructor wraps a ConnectorStateStore with a GenerationalStore
   * and caches the current generation for the named connector.
   *
   * @param baseStore The underlying ConnectorStateStore to wrap.
   * @param connectorName
   */
  public GenerationalStateStore(ConnectorStateStore baseStore,
                                String connectorName) {
    this(baseStore);
    myGeneration(connectorName);
  }

  /**
   * Gets the stored state of a named connector.
   *
   * @param connectorName
   * @return the state, or null if no state has been stored for this connector
   * @throws IllegalStateException if state store is disabled for this generation
   */
  public String getConnectorState(String connectorName) {
    testGeneration(connectorName);
    return baseStore.getConnectorState(connectorName);
  }

  /**
   * Sets the stored state of a named connector.
   *
   * @param connectorName
   * @param connectorState String to store
   * @throws IllegalStateException if state store is disabled for this generation
   */
  public void storeConnectorState(String connectorName, String connectorState) {
    testGeneration(connectorName);
    baseStore.storeConnectorState(connectorName, connectorState);
  }

  /**
   * Remove connector state.  If no such connector exists, do nothing.
   * As as side-effect, bump the current generation.
   *
   * @param connectorName name of the connector.
   */
  public void removeConnectorState(String connectorName) {
    newGeneration(connectorName);
    baseStore.removeConnectorState(connectorName);
  }

  /**
   * Test if my generation is the most recent generation for this connector.
   * If not, throw and IllegalStateException.
   *
   * @param connectorName the name of the connector
   * @throws IllegalStateException if my generation is not current
   */
  private void testGeneration(String connectorName) {
    if (myGeneration(connectorName) != currentGeneration(connectorName)) {
      // Why don't you all just f-f-f-fade away.
      throw new IllegalStateException("Attempt to access disabled "
          + "Connector State Store for connector: " + connectorName);
    }
  }

  /**
   * Return this instance's generation for the named connector.
   * If this instance does not yet have a generation for the named
   * connector, it grabs a snapshot of the most current generation.
   *
   * @param connectorName  the connector name
   * @return the current generation number
   */
  protected synchronized long myGeneration(String connectorName) {
    Long generation = (Long)myGenerations.get(connectorName);
    if (generation == null) {
      // If we have no generation yet, get the most current one.
      generation = new Long(currentGeneration(connectorName));
      myGenerations.put(connectorName, generation);
    }
    return generation.longValue();
  }

  /**
   * Return the current generation for the named connector.
   *
   * @param connectorName  the connector name
   * @return the current generation number
   */
  protected static long currentGeneration(String connectorName) {
    synchronized (generations) {
      Long generation = (Long)generations.get(connectorName);
      return (generation == null) ? 0 : generation.longValue();
    }
  }

  /**
   * Increment the current generation for the named connector.
   * Bumps the current generation number of the connector state for
   * this connector.  Subsequent reads or writes to older generations
   * will fail.
   *
   * @param connectorName  the connector name
   */
  public static void newGeneration(String connectorName) {
    synchronized (generations) {
      Long generation = (Long)generations.get(connectorName);
      generations.put(connectorName,
          new Long((generation == null) ? 1 : generation.longValue() + 1));
    }
  }

}
