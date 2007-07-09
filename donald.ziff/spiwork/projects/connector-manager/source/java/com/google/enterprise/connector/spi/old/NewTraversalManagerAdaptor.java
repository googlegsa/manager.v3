package com.google.enterprise.connector.spi.old;

import com.google.enterprise.connector.spi.DocumentList;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.TraversalManager;

public class NewTraversalManagerAdaptor implements TraversalManager {

  private com.google.enterprise.connector.spi.old.TraversalManager 
    oldTraversalManager = null;

  public NewTraversalManagerAdaptor(
      com.google.enterprise.connector.spi.old.TraversalManager oldTraversalManager) {
    this.oldTraversalManager = oldTraversalManager;
  }

  public DocumentList resumeTraversal(String checkPoint)
      throws RepositoryException {
    PropertyMapList propertyMapList = oldTraversalManager.resumeTraversal(checkPoint);
    return new NewDocumentListAdaptor(propertyMapList, oldTraversalManager);
  }

  public void setBatchHint(int batchHint) throws RepositoryException {
    oldTraversalManager.setBatchHint(batchHint);
  }

  public DocumentList startTraversal() throws RepositoryException {
    PropertyMapList propertyMapList = oldTraversalManager.startTraversal();
    return new NewDocumentListAdaptor(propertyMapList, oldTraversalManager);
  }

}
