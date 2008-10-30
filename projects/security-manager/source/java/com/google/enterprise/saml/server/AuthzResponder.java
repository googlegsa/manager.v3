package com.google.enterprise.saml.server;

import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.AuthzDecisionQuery;

import java.util.List;

/**
 * Interface for responding to SAML AuthzDecisionQueries, either in singular
 * or batched forms.
 */
public interface AuthzResponder {

  public Response authorize(AuthzDecisionQuery query);

  public List<Response> authorizeBatch(List<AuthzDecisionQuery> queries);
}
