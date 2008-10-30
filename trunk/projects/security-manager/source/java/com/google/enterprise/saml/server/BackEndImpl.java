package com.google.enterprise.saml.server;

import com.google.enterprise.sessionmanager.SessionManagerInterface;
import com.google.enterprise.saml.common.GsaConstants;

import java.util.logging.Logger;
import java.util.List;
import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;

import org.opensaml.saml2.core.ArtifactResponse;
import org.opensaml.saml2.core.ArtifactResolve;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.AuthzDecisionQuery;

/**
 * The implementation of the BackEnd interface for the Security Manager.
 *
 * At present, this implementation is very shallow and returns canned responses
 * for almost everything.
 */
public class BackEndImpl implements BackEnd {
    private static final Logger LOGGER =
      Logger.getLogger(BackEndImpl.class.getName());

  private SessionManagerInterface sm;

  private static BackEnd backendInstance;
  private ArtifactResolver artifactResolver;
  private AuthzResponder authzResponder;

  private BackEndImpl() {
    artifactResolver = new ArtifactResolverImpl();
    authzResponder = new AuthzResponderImpl();
  }

  public static BackEnd getInstance() {
    if (backendInstance != null) {
      return backendInstance;
    }

    backendInstance = new BackEndImpl();
    return backendInstance;
  }

  public void setSessionManager(SessionManagerInterface sm) {
    this.sm = sm;
  }

  public SessionManagerInterface getSessionManager() {
    return sm;
  }

  public String loginRedirect(String referer, String relayState) {
    String urlEncodedRelayState = "";
    try {
      urlEncodedRelayState = URLEncoder.encode(relayState, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      LOGGER.warning("Could not encode RelayState");
    }

    String gsaUrl = referer.substring(0, referer.indexOf("search?"));
    String redirectUrl = gsaUrl + GsaConstants.GSA_ARTIFACT_HANDLER_NAME
        + "?" + GsaConstants.GSA_ARTIFACT_PARAM_NAME + "=" + "foo"
        + "&" + GsaConstants.GSA_RELAY_STATE_PARAM_NAME + "=" + urlEncodedRelayState;

    LOGGER.info("Referer: " + referer);
    LOGGER.info("RelayState: " + relayState);
    LOGGER.info("URLEncoded RelayState: " + urlEncodedRelayState);
    LOGGER.info("GSA URL: " + gsaUrl);
    LOGGER.info("Redirect URL: " + redirectUrl);

    return redirectUrl;
  }

  public ArtifactResponse resolveArtifact(ArtifactResolve artifactResolve) {
    return artifactResolver.resolve(null);
  }

  public List<Response> authorize(List<AuthzDecisionQuery> authzDecisionQueries) {
    return authzResponder.authorizeBatch(authzDecisionQueries);
  }
}
