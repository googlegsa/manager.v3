package com.google.enterprise.connector.traversal;

/**
 * An interface which can be passed to a Connector's QueryTraversalManager 
 * to interact with the ConnectorManager (CM), to provide guidance on its
 * management, in particular, the timeout which the Connector requests.  
 */
public interface QueryTraverserStatus {

  /**
   * Request not to be timed out for the specified number of seconds.
   * The ConnectorManager does not guarantee to heed the request, since it
   * may have other scheduling considerations which override.
   * @param timeoutRequested number of seconds requested
   */
  public void requestTimeout(long timeoutRequested);
  
  /**
   * Inform ConnectorManager of the current status of the traversal
   * @param percentDone non-negative number in [0 .. 100]
   */
  public void setStatus(double percentDone);
}
