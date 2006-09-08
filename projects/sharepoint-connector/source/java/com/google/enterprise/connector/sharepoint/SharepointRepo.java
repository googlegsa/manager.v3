package com.google.enterprise.connector.sharepoint;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.LoginException;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Session;

import connector.ClientContext;
import connector.ConnectorConstants;

public class SharepointRepo implements Connector {

  private static Log logger = LogFactory.getLog(SharepointRepo.class);
  static {
    try {
      ClientContext.init();
      ClientContext.setMode(ConnectorConstants.MODE_QUERY);
    } catch (ConfigurationException e) {

    }
  }

  public Session login(String username, String password) throws LoginException,
      RepositoryException {
    try {
      return new SharepointSession(username, password);
    } catch (Exception e) {
      logger.error(e.getMessage());
      throw new RepositoryException("connection to repo failed");
    }
  }
  
}
