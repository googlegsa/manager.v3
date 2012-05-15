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

package com.google.enterprise.connector.instantiator;

import com.google.common.annotations.VisibleForTesting;
import com.google.enterprise.connector.persist.ConnectorExistsException;
import com.google.enterprise.connector.persist.ConnectorNotFoundException;
import com.google.enterprise.connector.scheduler.Schedule;
import com.google.enterprise.connector.spi.AuthenticationManager;
import com.google.enterprise.connector.spi.AuthorizationManager;
import com.google.enterprise.connector.spi.ConfigureResponse;
import com.google.enterprise.connector.spi.Retriever;
import com.google.enterprise.connector.spi.TraversalManager;
import com.google.enterprise.connector.traversal.Traverser;
import com.google.enterprise.connector.util.filter.DocumentFilterFactory;

import java.util.Locale;

/**
 * Management interface for a connector instance.
 * <p>
 * This interface provides a single point of control to allow for proper
 * synchronization between concurrent
 * <OL>
 * <LI>Configuration operations
 * <LI>Running of traversals
 * </OL>
 * <p>
 * Note that a {@link ConnectorCoordinator} may be created with a name but not a
 * complete full configuration for purposes of providing synchronization during
 * the creation process. The configuration can be specified using
 * {@link #setConnectorConfiguration(TypeInfo, Configuration, Locale, boolean)}
 * The {@link #exists()} method will return false for a
 * {@link ConnectorCoordinator} without a complete configuration. In addition
 * many of the methods in this interface will throw a
 * {@link ConnectorNotFoundException} if the {@link ConnectorCoordinator} does
 * not have a complete configuration.
 * <p>
 * It is expected the caller will guarantee that each
 * {@link ConnectorCoordinator} in the system has a unique name.
 */
@VisibleForTesting
public interface ConnectorCoordinator {
  /**
   * Returns true if this {@link ConnectorCoordinator} holds a
   * configured usable connector instance. This function is for reporting
   * purposes only. Concurrent operations by other threads can invalidate
   * the result of calling this quickly so in code such as
   * <pre>
   * if (c.exists()) {
   *   doSomething();
   * }
   * </pre>
   * The {@link ConnectorCoordinator} may not exist when <code>doSomething()
   * </code> actually runs.
   */
  public boolean exists();

  /**
   * Returns the name for this {@link ConnectorCoordinator}.
   */
  public String getConnectorName();

  /**
   * Removes this {@link ConnectorCoordinator} from the system. After this
   * returns {@link ConnectorCoordinator#exists()} will return false until
   * someone re-creates the connector configuration by calling
   * {@link #setConnectorConfiguration(TypeInfo, Configuration, Locale, boolean)}.
   * This is a noop if this {@link ConnectorCoordinator} does not exist.
   */
  public void removeConnector();

  /**
   * Returns the {@link AuthenticationManager} for this
   * {@link ConnectorCoordinator}
   *
   * @throws ConnectorNotFoundException if this {@link ConnectorCoordinator}
   *         does not exist.
   * @throws InstantiatorException if unable to instantiate the requested
   *         {@link AuthenticationManager}
   */
  public AuthenticationManager getAuthenticationManager()
      throws ConnectorNotFoundException, InstantiatorException;

  /**
   * Returns the {@link AuthorizationManager} for this
   * {@link ConnectorCoordinator}
   *
   * @throws ConnectorNotFoundException if this {@link ConnectorCoordinator}
   *         does not exist.
   * @throws InstantiatorException if unable to instantiate the requested
   *         {@link AuthorizationManager}
   */
  public AuthorizationManager getAuthorizationManager()
      throws ConnectorNotFoundException, InstantiatorException;

  /**
   * Returns the {@link TraversalManager} for this
   * {@link ConnectorCoordinator}
   *
   * @throws ConnectorNotFoundException if this {@link ConnectorCoordinator}
   *         does not exist.
   * @throws InstantiatorException if unable to instantiate the requested
   *         {@link Traverser}
   */
  @VisibleForTesting
  public TraversalManager getTraversalManager()
      throws ConnectorNotFoundException, InstantiatorException;

  /**
   * Return a {@link Retriever} that may be used to access content for the
   * document identified by {@code docid}.  If the connector does not support
   * the {@link Retriever} interface, {@code null} is returned.
   *
   * @return a {@link Retriever}, or {@code null} if none is available
   * @throws ConnectorNotFoundException if this {@link ConnectorCoordinator}
   *         does not exist.
   * @throws InstantiatorException if unable to instantiate the requested
   *         {@link Retriever}
   */
  public Retriever getRetriever()
      throws ConnectorNotFoundException, InstantiatorException;

  /**
   * Returns {@link ConfigureResponse} with a configuration form or a
   * message indicating the problem.
    *
   * @throws ConnectorNotFoundException if this {@link ConnectorCoordinator}
   *         does not exist.
   * @throws InstantiatorException if unable to instantiate the support
   *         classes needed to construct the {@link ConfigureResponse}
   */
  public ConfigureResponse getConfigForm(Locale locale)
      throws ConnectorNotFoundException, InstantiatorException;

  /**
   * Restarts the traversal for this {@link ConnectorCoordinator} by
   * <OL>
   * <LI> Canceling the current traversal,
   * <LI> Clearing the current checkpoint.
   * <LI> Starting the traversal again.
   * </OL>
   *
   * @throws ConnectorNotFoundException if this {@link ConnectorCoordinator}
   *         does not exist.
   */
  public void restartConnectorTraversal() throws ConnectorNotFoundException;

  /**
   * Sets the {@link Schedule} for this {@link ConnectorCoordinator}. If this
   * {@link ConnectorCoordinator} supports persistence this will persist the
   * new Schedule.
   *
   * @throws ConnectorNotFoundException if this {@link ConnectorCoordinator}
   *         does not exist.
   */
  public void setConnectorSchedule(Schedule connectorSchedule)
      throws ConnectorNotFoundException;

  /**
   * Returns the {@link Schedule} for this {@link ConnectorCoordinator}.
   *
   * @throws ConnectorNotFoundException if this {@link ConnectorCoordinator}
   *         does not exist.
   */
  public Schedule getConnectorSchedule() throws ConnectorNotFoundException;

  /**
   * Set the Connector's traversal state.
   *
   * @param state a String representation of the state to store.
   *        If null, any previous stored state is discarded.
   * @throws ConnectorNotFoundException if this {@link ConnectorCoordinator}
   *         does not exist.
   */
  public void setConnectorState(String state)
     throws ConnectorNotFoundException;

  /**
   * Returns the Connector's traversal state.
   *
   * @return String representation of the stored state, or
   *         null if no state is stored.
   * @throws ConnectorNotFoundException if this {@link ConnectorCoordinator}
   *         does not exist.
   */
  public String getConnectorState() throws ConnectorNotFoundException;

  /**
   * Returns the type name for this {@link ConnectorCoordinator}.
   *
   * @throws ConnectorNotFoundException if this {@link ConnectorCoordinator}
   *         does not exist.
   */
  public String getConnectorTypeName() throws ConnectorNotFoundException;

  /**
   * Sets the configuration for this {@link ConnectorCoordinator}. If this
   * {@link ConnectorCoordinator} supports persistence this will persist the new
   * configuration.
   * <p>
   * Upon success this function returns
   * <OL>
   * <LI>null
   * <LI> {@link ConfigureResponse} with a null message
   * <LI> {@link ConfigureResponse} with a zero length message
   * </OL>
   * <p>
   * Upon success if this modifies the configuration parameters then calling
   * {@link ConfigureResponse#getConfigData()} on the returned value returns the
   * updated configuration parameters.
   *
   * @param newTypeInfo the new {@link TypeInfo} for this
   *        {@link ConnectorCoordinator}.
   * @param configuration the replacement {@link Configuration} properties for
   *        this {@link ConnectorCoordinator}.
   * @param locale the locale for use in constructing the returned
   *        {@link ConfigureResponse}.
   * @param update true means to update and existing configuration and flase
   *        means to create a new one.
   *
   * @throws ConnectorNotFoundException if {@link #exists()} returns false and
   *         update is true.
   * @throws ConnectorExistsException {@link #exists()} returns true and update
   *         is false.
   */
  public ConfigureResponse setConnectorConfiguration(TypeInfo newTypeInfo,
      Configuration configuration, Locale locale, boolean update)
      throws ConnectorNotFoundException, ConnectorExistsException,
      InstantiatorException;

  /**
   * Returns the {@link Configuration} for this {@link ConnectorCoordinator}.
   *
   * @throws ConnectorNotFoundException if this {@link ConnectorCoordinator}
   *         does not exist.
   */
  public Configuration getConnectorConfiguration()
      throws ConnectorNotFoundException;

  /**
   * Sets {@code GData} configuration for GData aware Connectors.
   */
  public void setGDataConfig()
      throws ConnectorNotFoundException, InstantiatorException;

  /**
   * Returns a connector's {@link DocumentFilterFactory}. Connectors may define
   * a document filter specific to that connector instance.  This filter will
   * be used in conjuction with the Connector Manager's document filter, and
   * will act as the source for the Connector Manager's document filter.
   *
   * @return {@link DocumentFilterFactory} for the connector, or {@code null}
   *         if the connector does not define a DocumentFilterFactory.
   */
  public DocumentFilterFactory getDocumentFilterFactory()
      throws ConnectorNotFoundException;

  /**
   * Starts running a batch for this {@link ConnectorCoordinator} if a batch is
   * not already running.
   *
   * @return true if this call started a batch
   * @throws ConnectorNotFoundException if this {@link ConnectorCoordinator}
   *         does not exist.
   */
  public boolean startBatch() throws ConnectorNotFoundException;

  /**
   * Shuts down this {@link ConnectorCoordinator} if {@link #exists()}.
   */
  public void shutdown();
}
