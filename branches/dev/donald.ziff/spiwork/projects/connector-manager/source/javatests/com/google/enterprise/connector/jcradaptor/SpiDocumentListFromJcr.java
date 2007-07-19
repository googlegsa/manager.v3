package com.google.enterprise.connector.jcradaptor;

import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.DocumentList;

import javax.jcr.Node;
import javax.jcr.NodeIterator;

public class SpiDocumentListFromJcr implements DocumentList {

  private final Node startNode;
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

  public boolean nextDocument() {
    if (startNode != null) {
      document = new SpiDocumentFromJcr(startNode);
      return true;
    }
    if (jcrIterator == null) {
      return false;
    }
    boolean result = jcrIterator.hasNext();
    if (result) {
      document = new SpiDocumentFromJcr(jcrIterator.nextNode());
    }
    return result;
  }

  public Document getDocument() {
    return document;
  }

}
