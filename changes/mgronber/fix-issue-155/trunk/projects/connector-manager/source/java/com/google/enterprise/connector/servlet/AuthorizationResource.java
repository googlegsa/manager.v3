// Copyright 2009 Google Inc. All Rights Reserved.

package com.google.enterprise.connector.servlet;

import com.google.enterprise.connector.spi.AuthenticationIdentity;

import org.w3c.dom.Element;

/**
 * This class holds the fields associated with an authorization resource.
 * In the authorization scheme, a request is made to determine if a given
 * {@link AuthenticationIdentity} is authorized to view a sequence of
 * resources.  When the request comes into the Connector Manager it contains
 * both a URI and the name of the Connector that created the Document
 * associated with the URI. This class allows both bits of information to be
 * kept together.
 * <p>
 * It also provides some utility related to mapping the XML DTD for the
 * Resource to this representation.
 *
 * @see <a href="http://code.google.com/p/google-enterprise-connector-manager/source/browse/docs/dtd/ConnectorManagerAdmin.dtd">Connector Manager Admin DTD</a>
 */
public class AuthorizationResource
    implements Comparable<AuthorizationResource> {
  private String connectorName;
  private String documentUrl;
  private ParsedUrl fabricatedUrl;
  private int status = ConnectorMessageCode.SUCCESS;
  private boolean useFabricated = true;

  /**
   * Construct from given XML element.
   */
  public AuthorizationResource(Element resourceItem) {
    String resourceUrl = resourceItem.getFirstChild().getNodeValue();
    this.fabricatedUrl = new ParsedUrl(resourceUrl);
    if (fabricatedUrl.getStatus() ==
        ConnectorMessageCode.RESPONSE_NULL_CONNECTOR) {
      // Could not get the Connector Name from the URL so can't use it.
      useFabricated = false;
      // Get the connector name attribute.
      String connectorName = resourceItem.getAttribute(
          ServletUtil.XMLTAG_CONNECTOR_NAME_ATTRIBUTE);
      if ("".equals(connectorName)) {
        // This is an invalid state.
        status = ConnectorMessageCode.RESPONSE_NULL_CONNECTOR;
      } else {
        this.connectorName = connectorName;
        this.documentUrl = resourceUrl;
      }
    }
  }

  public String getConnectorName() {
    if (useFabricated) {
      return fabricatedUrl.getConnectorName();
    } else {
      return connectorName;
    }
  }

  public String getDocId() {
    if (useFabricated) {
      return fabricatedUrl.getDocid();
    } else {
      return documentUrl;
    }
  }

  public String getUrl() {
    if (useFabricated) {
      return fabricatedUrl.getUrl();
    } else {
      return documentUrl;
    }
  }

  public int getStatus() {
    if (useFabricated) {
      return fabricatedUrl.getStatus();
    } else {
      return status;
    }
  }

  public boolean isFabricated() {
    return useFabricated;
  }

  public int compareTo(AuthorizationResource o) {
    return getUrl().compareTo(o.getUrl());
  }
}
