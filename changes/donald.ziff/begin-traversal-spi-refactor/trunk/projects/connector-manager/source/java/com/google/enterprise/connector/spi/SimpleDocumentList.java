package com.google.enterprise.connector.spi;

import java.util.Iterator;
import java.util.List;

public class SimpleDocumentList implements DocumentList {

  private List documents;
  private Iterator iterator;
  private Document document;
  
  public SimpleDocumentList(List documents) {
    this.documents = documents;
    this.iterator = null;
    this.document = null;
  }

  public Document nextDocument() {
    if (iterator == null) {
      iterator = documents.iterator();
    }
    if (iterator.hasNext()) {
      document = (Document) iterator.next();
      return document;
    }
    return null;
  }

  public String checkpoint() throws RepositoryException {
    return Value.getSingleValue(document,
        SpiConstants.PROPNAME_LASTMODIFIED).toString();
  }

}
