package com.google.enterprise.connector.jcradaptor;

import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.DocumentList;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.Value;

import org.json.JSONException;
import org.json.JSONObject;

import javax.jcr.Node;
import javax.jcr.NodeIterator;

public class SpiDocumentListFromJcr implements DocumentList {

  private Node startNode;
  private final NodeIterator jcrIterator;
  private Document document = null;

  public SpiDocumentListFromJcr(final Node thisNode, final NodeIterator nodes) {
    startNode = thisNode;
    jcrIterator = nodes;
  }

  public SpiDocumentListFromJcr(final NodeIterator nodes) {
    startNode = null;
    jcrIterator = nodes;
  }

  public Document nextDocument() {
    if (startNode != null) {
      document = new SpiDocumentFromJcr(startNode);
      startNode = null;
      return document;
    }
    if (jcrIterator == null) {
      return null;
    }
    if (jcrIterator.hasNext()) {
      document = new SpiDocumentFromJcr(jcrIterator.nextNode());
    }
    return document;
  }

  public String checkpoint() throws RepositoryException {
    String uuid = Value.getSingleValueString(document,
        SpiConstants.PROPNAME_DOCID);
    String dateString = Value.getSingleValueString(document,
        SpiConstants.PROPNAME_LASTMODIFIED);
    String result = null;
    try {
      JSONObject jo = new JSONObject();
      jo.put("uuid", uuid);
      jo.put("lastModified", dateString);
      result = jo.toString();
    } catch (JSONException e) {
      throw new RepositoryException("Unexpected JSON problem", e);
    }
    return result;
  }

}
