package com.google.enterprise.connector.servlet;

import com.google.enterprise.connector.manager.ConnectorManager;
import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.connector.saml.server.BackEnd;
import com.google.enterprise.connector.saml.common.Metadata;
import com.google.enterprise.connector.common.ServletBase;

import org.opensaml.saml2.metadata.EntityDescriptor;

import java.io.IOException;

/**
 * SecurityManagerServlet encapsulates the sec-mgr specific servlet extensions.
 *
 * All servlets within the security manager are expected to extend this class.
 */
public class SecurityManagerServlet extends ServletBase {

  public static ConnectorManager getConnectorManager() {
    return ConnectorManager.class.cast(Context.getInstance().getManager());
  }

  public static BackEnd getBackEnd() {
    BackEnd backend =
        BackEnd.class.cast(Context.getInstance().getRequiredBean("BackEnd", BackEnd.class));
    return backend;
  }

    public static EntityDescriptor getEntity(String id) throws IOException {
    return getMetadata().getEntity(id);
  }

  public static EntityDescriptor getSmEntity() throws IOException {
    return getMetadata().getSmEntity();
  }

  public static String getSmEntityId() {
    return getMetadata().getSmEntityId();
  }

  public static Metadata getMetadata() {
    return Metadata.class.cast(Context.getInstance().getRequiredBean("Metadata", Metadata.class));
  }

}

