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

  public boolean nextDocument() {
    if (iterator == null) {
      iterator = documents.iterator();
    }
    boolean hasNext = iterator.hasNext();
    if (hasNext) {
      document = (Document) iterator.next();
    } 
    return hasNext;
  }

  public Document getDocument() {
    return document;
  }

}
