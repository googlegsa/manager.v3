package com.google.enterprise.connector.instantiator;

import com.google.enterprise.connector.persist.ConnectorNotFoundException;
import com.google.enterprise.connector.persist.ConnectorTypeNotFoundException;
import com.google.enterprise.connector.traversal.Traverser;

import java.util.Map;



public interface ConnectorInstantiator {

  /**
   * Finds a named connector.
   * 
   * @param connectorName
   * @return the Connector, fully instantiated
   * @throws ConnectorNotFoundException
   * @throws InstantiatorException
   */
  public Traverser getTraverser(String connectorName)
      throws ConnectorNotFoundException, InstantiatorException;

  /**
   * Drops a named connector.
   * 
   * @param connectorName
   * @throws InstantiatorException
   */
  public void dropConnector(String connectorName) throws InstantiatorException;

  /**
   * Stores a new connector config and updates the instantiator state
   * 
   * @param connectorName
   * @param connectorTypeName
   * @param configKeys
   * @param prototypeString
   * @throws ConnectorNotFoundException
   * @throws ConnectorTypeNotFoundException
   * @throws InstantiatorException
   */
  public void setConnectorConfig(String connectorName,
      String connectorTypeName, Map configKeys, String prototypeString)
      throws ConnectorNotFoundException, ConnectorTypeNotFoundException,
      InstantiatorException;

}
