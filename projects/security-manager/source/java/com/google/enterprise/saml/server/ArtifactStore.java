package com.google.enterprise.saml.server;

import java.util.HashMap;

/**
 * Keep track of the artifacts we have issued.
 *
 */
public class ArtifactStore {
  private HashMap<String, String> artMap;

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
