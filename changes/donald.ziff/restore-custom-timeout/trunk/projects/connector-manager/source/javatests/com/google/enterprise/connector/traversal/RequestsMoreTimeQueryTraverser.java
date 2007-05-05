package com.google.enterprise.connector.traversal;

import java.util.Date;

  /**
   * A Traverser that asks for more time, via the QueryTraverserMonitor
   * interface.
   */
  public class RequestsMoreTimeQueryTraverser implements Traverser {

    /*
     * (non-Javadoc)
     * 
     * @see com.google.enterprise.connector.traversal.Traverser#runBatch(int)
     */
    public int runBatch(int batchHint, QueryTraverserMonitor monitor) {
      long sleepTime = 120 * 1000;

      boolean breakLoop = true;
      monitor.requestTimeout(sleepTime);
      boolean interrupted = false;
      int counter = 0;
      while (breakLoop) {
        long startTime = new Date().getTime();
        long now = startTime;
        while ((now - startTime) < (2 * 1000)) {
          // this inner loop simulates a call to a CMS that takes a while to
          // return and can't be interrupted
          try {
            Thread.sleep(1000);
          } catch (InterruptedException ie) {
            // Here be dragons: the fact that an InterruptedException was thrown
            // does NOT mean that if you then call Thread.interrupted() it returns
            // true! That's why we have to remember that we were interrupted in
            // order to exit the outer loop at the break below.
            interrupted = true;
          }
          now = new Date().getTime();
        }
        counter++;
        if (interrupted || Thread.interrupted()) {
          break;
        }
      }      
      
      
      try {
        Thread.sleep(sleepTime);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      return batchHint;
    }
}
