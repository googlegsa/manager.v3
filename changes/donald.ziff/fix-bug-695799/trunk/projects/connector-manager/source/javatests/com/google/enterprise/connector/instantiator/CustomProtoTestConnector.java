/**
 * 
 */
package com.google.enterprise.connector.instantiator;

import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.LoginException;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Session;

/**
 * @author ziff
 *
 */
public class CustomProtoTestConnector implements Connector {
  
  Connector delegateConnector;
  String customProperty = "default";
  int customIntProperty = 0;
  
  public void setDelegateConnector(Connector delegateConnector) {
    this.delegateConnector = delegateConnector;
  }

  public void setCustomProperty(String customProperty) {
    this.customProperty = customProperty;
  }

  public String getCustomProperty() {
    return customProperty;
  }

  public int getCustomIntProperty() {
    return customIntProperty;
  }

  public void setCustomIntProperty(int customIntProperty) {
    this.customIntProperty = customIntProperty;
  }

  /* (non-Javadoc)
   * @see com.google.enterprise.connector.spi.Connector#login()
   */
  public Session login() throws LoginException, RepositoryException {
     return delegateConnector.login();
  }
}
