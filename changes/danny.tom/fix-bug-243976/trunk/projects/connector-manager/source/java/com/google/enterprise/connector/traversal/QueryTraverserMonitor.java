package com.google.enterprise.connector.traversal;

/**
 * An interface which can be passed to a Connector's QueryTraversalManager 
 * to interact with the ConnectorManager (CM), to provide guidance on its
 * management, in particular, the timeout which the Connector requests.  
 */
public interface QueryTraverserMonitor {

  /**
   * Request not to be timed out for the specified number of seconds, counting
   * from the time of this call.
   * @param timeoutRequested number of seconds requested
   */
  public void requestTimeout(long timeoutRequested);
  
  /**
   * Inform ConnectorManager of the current status of the traversal. 
   * Use of this method is voluntary, and Connectors should only use it if
   * they can be reasonably confident of the accuracy.
   * @param percentDone non-negative number in [0 .. 100]
   */
  public void reportProgress(double percentDone);
}
