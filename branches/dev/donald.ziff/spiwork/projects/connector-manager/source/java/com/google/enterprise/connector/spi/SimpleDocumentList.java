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
    } else {
      document = null;
    }
    return hasNext;
  }

  public String checkpoint() throws RepositoryException {
    if (document == null) {
      throw new IllegalStateException();
    }
    return document.checkpoint();
  }

  public boolean findProperty(String name) {
    if (document == null) {
      throw new IllegalStateException();
    }
    return document.findProperty(name);
  }

  public String getPropertyName() throws RepositoryException {
    if (document == null) {
      throw new IllegalStateException();
    }
    return document.getPropertyName();
  }

  public Value getValue() throws RepositoryException {
    if (document == null) {
      throw new IllegalStateException();
    }
    return document.getValue();
  }

  public boolean nextProperty() {
    if (document == null) {
      throw new IllegalStateException();
    }
    return document.nextProperty();
  }

  public boolean nextValue() {
    if (document == null) {
      throw new IllegalStateException();
    }
    return document.nextValue();
  }

}
