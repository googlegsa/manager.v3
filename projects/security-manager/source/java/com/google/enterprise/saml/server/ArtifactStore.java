package com.google.enterprise.saml.server;

import com.google.enterprise.connector.manager.Manager;
import com.google.enterprise.sessionmanager.SessionManagerInterface;
import com.google.enterprise.saml.common.GsaConstants;

import java.util.logging.Logger;
import java.util.List;
import java.util.HashMap;
import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;

import org.opensaml.saml2.core.ArtifactResponse;
import org.opensaml.saml2.core.ArtifactResolve;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.AuthzDecisionQuery;

/**
 * Keep track of the artifacts we have issued.
 *
 */
public class ArtifactStore {
  private HashMap<String, String> artMap;
  
  private static final Logger LOGGER =
      Logger.getLogger(ArtifactStore.class.getName());

  public ArtifactStore() {
    artMap = new HashMap<String, String>();
  }
  
  public void put(String artifact, String subject) {
    artMap.put(artifact, subject);
  }

  public String getSubject(String artifact) {
    return artMap.get(artifact);
  }
  
  public void remove(String artifact) {
    artMap.remove(artifact);
  }
}
