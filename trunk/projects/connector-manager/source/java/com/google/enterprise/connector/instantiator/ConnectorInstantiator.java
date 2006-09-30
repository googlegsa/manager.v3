package com.google.enterprise.connector.instantiator;

import com.google.enterprise.connector.persist.ConnectorNotFoundException;
import com.google.enterprise.connector.persist.ConnectorTypeNotFoundException;
import com.google.enterprise.connector.spi.AuthenticationManager;
import com.google.enterprise.connector.spi.AuthorizationManager;
import com.google.enterprise.connector.traversal.Traverser;

import java.util.Map;



public interface ConnectorInstantiator {

  /**
   * gets an AuthenticationManager for a named connector.
   * 
   * @param connectorName the String name of the connector for which to get the
   *        Traverser
   * @return the AuthenticationManager, fully instantiated
   * @throws ConnectorNotFoundException to indicate that no connector of the
   *         specified name is found
   * @throws InstantiatorException if something bad, probably unrecoverable,
   *         happens
   */
  public AuthenticationManager getAuthenticationManager(String connectorName)
      throws ConnectorNotFoundException, InstantiatorException;

  /**
   * gets an AuthorizationManager for a named connector.
   * 
   * @param connectorName the String name of the connector for which to get the
   *        Traverser
   * @return the AuthorizationManager, fully instantiated
   * @throws ConnectorNotFoundException to indicate that no connector of the
   *         specified name is found
   * @throws InstantiatorException if something bad, probably unrecoverable,
   *         happens
   */
  public AuthorizationManager getAuthorizationManager(String connectorName)
      throws ConnectorNotFoundException, InstantiatorException;

  /**
   * gets a Traverser for a named connector.
   * 
   * @param connectorName the String name of the connector for which to get the
   *        Traverser
   * @return the Traverser, fully instantiated
   * @throws ConnectorNotFoundException to indicate that no connector of the
   *         specified name is found
   * @throws InstantiatorException if something bad, probably unrecoverable,
   *         happens
   */
  public Traverser getTraverser(String connectorName)
      throws ConnectorNotFoundException, InstantiatorException;

  /**
   * gets a Config Map for a named connector.
   * 
   * @param connectorName the String name of the connector for which to get the
   *        Config Map
   * @return the Config Map, fully instantiated
   * @throws ConnectorNotFoundException to indicate that no connector of the
   *         specified name is found
   * @throws InstantiatorException if something bad, probably unrecoverable,
   *         happens
   */
  public Map getConfigMap(String connectorName)
      throws ConnectorNotFoundException, InstantiatorException;

  /**
   * Gets the type for a named connector
   * 
   * @param connectorName
   * @return the connector type name
   * @throws ConnectorNotFoundException if the connector does not exist
   */
  public String getConnectorType(String connectorName)
      throws ConnectorNotFoundException;

  /**
   * Drops a named connector.
   * 
   * @param connectorName the String name of the connector to drop
   * @throws InstantiatorException if something bad, probably unrecoverable,
   *         happens
   */
  public void dropConnector(String connectorName) throws InstantiatorException;

  /**
   * Stores a new connector config and updates the instantiator state
   * 
   * @param connectorName the String name of the connector for which to set the
   *        config
   * @param connectorTypeName the String name of the connector's Type
   * @param configKeys a Map of String to String giving configuration for this
   *        connector
   * @param prototypeString A String fragment of Spring XML giving the prototype
   *        of configuration for this type of connector
   * @throws ConnectorNotFoundException to indicate that no connector of the
   *         specified name is found
   * @throws ConnectorTypeNotFoundException to indicate that no connector type
   *         of the specified name is found
   * @throws InstantiatorException if something bad, probably unrecoverable,
   *         happens
   */
  public void setConnectorConfig(String connectorName,
      String connectorTypeName, Map configKeys, String prototypeString)
      throws ConnectorNotFoundException, ConnectorTypeNotFoundException,
      InstantiatorException;

}
