package com.google.enterprise.connector.scheduler;

import com.google.enterprise.connector.traversal.BatchResult;

/**
 * Recorder for batch completions.
 * @author EricStrellis@gmail.com (EricStrellis)
 */
public interface BatchResultRecorder {
  /**
   *  Record the result of running a single batch.
   */
  public void recordResult(BatchResult result);
}
