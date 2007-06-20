package com.google.enterprise.connector.traversal;

public class FileSizeLimitInfo {

  private long maxDocumentSize = 0;
  
  public void setMaxDocumentSize(long maxDocumentSize) {
    this.maxDocumentSize = maxDocumentSize;
  }

  public long maxDocumentSize() {
    return maxDocumentSize;
  }

}
