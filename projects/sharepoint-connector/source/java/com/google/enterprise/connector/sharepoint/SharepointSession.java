package com.google.enterprise.connector.sharepoint;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.enterprise.connector.sharepoint.impl.ClientContext;
import com.google.enterprise.connector.sharepoint.impl.Sharepoint;
import com.google.enterprise.connector.spi.AuthenticationManager;
import com.google.enterprise.connector.spi.AuthorizationManager;
import com.google.enterprise.connector.spi.QueryTraversalManager;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Session;

public class SharepointSession implements Session {

  private static Log logger = LogFactory.getLog(SharepointSession.class);

  SharepointQueryManager mgr = null;
  ClientContext context;
  
  public SharepointSession(String username, String password) throws Exception
  {
    context = new ClientContext(username, password);
  }
  /**
   * Gets a QueryTraversalManager to implement query-based traversal
   * 
   * @return a QueryTraversalManager
   * @throws RepositoryException
   */
  public QueryTraversalManager getQueryTraversalManager()
      throws RepositoryException {
    try {
      Sharepoint sp = new Sharepoint(context);
      mgr = new SharepointQueryManager(sp);
    } catch (Exception e) {
      logger.error(e.getMessage());
      throw new RepositoryException("Failed to get Query Manager");
    }
    return mgr;
  }
  
  public AuthenticationManager getAuthenticationManager()
  {
    return null;
  }

  public AuthorizationManager getAuthorizationManager()
  {
    return null;
  }
}
