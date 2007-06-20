package com.google.enterprise.connector.traversal;

import com.google.enterprise.connector.spi.TraversalContext;

public class ProductionTraversalContext implements TraversalContext {

  FileSizeLimitInfo fileSizeLimitInfo;
  MimeTypeMap mimeTypeMap;
  
  public void setFileSizeLimitInfo(FileSizeLimitInfo fileSizeLimitInfo) {
    this.fileSizeLimitInfo = fileSizeLimitInfo;
  }

  public void setMimeTypeMap(MimeTypeMap mimeTypeMap) {
    this.mimeTypeMap = mimeTypeMap;
  }

  public long maxDocumentSize() {
    return fileSizeLimitInfo.maxDocumentSize();
  }

  public int mimeTypeSupportLevel(String mimeType) {
    return mimeTypeMap.mimeTypeSupportLevel(mimeType);
  }

}
