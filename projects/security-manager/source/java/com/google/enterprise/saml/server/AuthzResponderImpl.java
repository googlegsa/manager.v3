package com.google.enterprise.saml.server;

import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.AuthzDecisionQuery;

import java.util.List;

/**
 * 
 */
public class AuthzResponderImpl implements AuthzResponder {
  public Response authorize(AuthzDecisionQuery query) {
    return null;
  }

  public List<Response> authorizeBatch(List<AuthzDecisionQuery> queries) {
    return null;
  }
}
