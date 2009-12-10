// Copyright 2009 Google Inc. All Rights Reserved.

package com.google.enterprise.connector.servlet;

import junit.framework.TestCase;

import org.w3c.dom.Element;

/**
 * Tests {@link AuthorizationResource}.
 */
public class AuthorizationResourceTest extends TestCase {
  private static final String CONN_NAME_ONE = "connector1";
  private static final String DOCID_ONE = "foo1";
  private static final String FAB_URL_ONE =
      ServletUtil.PROTOCOL + CONN_NAME_ONE + ".localhost"
      + ServletUtil.DOCID + DOCID_ONE;
  private static final String GOOD_FAB_ONE_XML =
      "<" + ServletUtil.XMLTAG_RESOURCE + ">"
      + FAB_URL_ONE
      + "</" + ServletUtil.XMLTAG_RESOURCE + ">";

  private static final String CONN_NAME_TWO = "connector2";
  private static final String DOCID_TWO = "foo2";
  private static final String FAB_URL_TWO =
    ServletUtil.PROTOCOL + CONN_NAME_TWO + ".localhost"
    + ServletUtil.DOCID + DOCID_TWO;
  private static final String GOOD_FAB_TWO_XML =
      "<" + ServletUtil.XMLTAG_RESOURCE + ">"
      + FAB_URL_TWO
      + "</" + ServletUtil.XMLTAG_RESOURCE + ">";

  private static final String NON_FAB_URL =
      "http://sharepoint.host:8000/sites/mylist/test1.doc";
  private static final String NON_FAB_XML =
      "<" + ServletUtil.XMLTAG_RESOURCE + " "
      + ServletUtil.XMLTAG_CONNECTOR_NAME_ATTRIBUTE
      + "=\"" + CONN_NAME_ONE + "\"" + ">"
      + NON_FAB_URL + "</" + ServletUtil.XMLTAG_RESOURCE + ">";

  private static final String BAD_FAB_NO_CN_XML =
      "<" + ServletUtil.XMLTAG_RESOURCE + ">"
      + ServletUtil.PROTOCOL + ".localhost"
      + ServletUtil.DOCID + DOCID_TWO
      + "</" + ServletUtil.XMLTAG_RESOURCE + ">";
  private static final String BAD_FAB_NO_DOCID_XML =
    "<" + ServletUtil.XMLTAG_RESOURCE + ">"
    + ServletUtil.PROTOCOL + CONN_NAME_TWO + ".localhost"
    + "/doc?param=" + DOCID_TWO
    + "</" + ServletUtil.XMLTAG_RESOURCE + ">";
  private static final String BAD_NON_FAB_XML =
    "<" + ServletUtil.XMLTAG_RESOURCE + ">"
    + NON_FAB_URL + "</" + ServletUtil.XMLTAG_RESOURCE + ">";

  public final void testGoodFabricated() {
    Element root = ServletUtil.parseAndGetRootElement(GOOD_FAB_ONE_XML,
        ServletUtil.XMLTAG_RESOURCE);
    AuthorizationResource resource = new AuthorizationResource(root);
    assertTrue(resource.isFabricated());
    assertEquals(CONN_NAME_ONE, resource.getConnectorName());
    assertEquals(DOCID_ONE, resource.getDocId());
    assertEquals(FAB_URL_ONE, resource.getUrl());
    assertEquals(ConnectorMessageCode.SUCCESS, resource.getStatus());
  }

  public final void testNonFabricated() {
    Element root = ServletUtil.parseAndGetRootElement(NON_FAB_XML,
        ServletUtil.XMLTAG_RESOURCE);
    AuthorizationResource resource = new AuthorizationResource(root);
    assertFalse(resource.isFabricated());
    assertEquals(CONN_NAME_ONE, resource.getConnectorName());
    assertEquals(NON_FAB_URL, resource.getDocId());
    assertEquals(NON_FAB_URL, resource.getUrl());
    assertEquals(ConnectorMessageCode.SUCCESS, resource.getStatus());
  }

  public final void testBadFabricated() {
    // Bad connector name.
    Element root = ServletUtil.parseAndGetRootElement(BAD_FAB_NO_CN_XML,
        ServletUtil.XMLTAG_RESOURCE);
    AuthorizationResource resource = new AuthorizationResource(root);
    assertEquals(ConnectorMessageCode.RESPONSE_NULL_CONNECTOR,
        resource.getStatus());

    // Bad docid.
    root = ServletUtil.parseAndGetRootElement(BAD_FAB_NO_DOCID_XML,
        ServletUtil.XMLTAG_RESOURCE);
    resource = new AuthorizationResource(root);
    assertEquals(ConnectorMessageCode.RESPONSE_NULL_DOCID,
        resource.getStatus());

    // Using a good non-fabricated URL.
    root = ServletUtil.parseAndGetRootElement(NON_FAB_XML,
        ServletUtil.XMLTAG_RESOURCE);
    resource = new AuthorizationResource(root);
    assertEquals(ConnectorMessageCode.SUCCESS,
        resource.getStatus());
  }

  public final void testBadNonFabricated() {
    // Missing connector name.
    Element root = ServletUtil.parseAndGetRootElement(BAD_NON_FAB_XML,
        ServletUtil.XMLTAG_RESOURCE);
    AuthorizationResource resource = new AuthorizationResource(root);
    assertEquals(ConnectorMessageCode.RESPONSE_NULL_CONNECTOR,
        resource.getStatus());
  }

  public final void testCompare() {
    Element root = ServletUtil.parseAndGetRootElement(GOOD_FAB_ONE_XML,
        ServletUtil.XMLTAG_RESOURCE);
    AuthorizationResource resourceOne = new AuthorizationResource(root);
    AuthorizationResource secondResourceOne = new AuthorizationResource(root);
    root = ServletUtil.parseAndGetRootElement(GOOD_FAB_TWO_XML,
        ServletUtil.XMLTAG_RESOURCE);
    AuthorizationResource resourceTwo = new AuthorizationResource(root);
    assertEquals(0, resourceOne.compareTo(secondResourceOne));
    assertEquals(-1, resourceOne.compareTo(resourceTwo));
    assertEquals(1, resourceTwo.compareTo(resourceOne));
  }
}
