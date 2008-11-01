package com.google.enterprise.saml.server;

import org.opensaml.saml2.core.ArtifactResponse;
import org.opensaml.saml2.core.ArtifactResolve;

/**
 * Interface for resolving SAML ArtifactResolve requests.
 */
public interface ArtifactResolver {

  public ArtifactResponse resolve(ArtifactResolve request);
}
