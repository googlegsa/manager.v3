package com.google.enterprise.connector.scheduler;

import com.google.enterprise.connector.traversal.BatchResult;
import com.google.enterprise.connector.traversal.Traverser;

import java.util.logging.Logger;

public class CancelableBatch implements Cancelable {
  private static final Logger LOGGER =
    Logger.getLogger(CancelableBatch.class.getName());

  final Traverser traverser;
  final String traverserName;
  final BatchResultRecorder batchResultRecorder;
  final int batchHint;

  CancelableBatch(Traverser traverser, String traverserName,
      BatchResultRecorder batchResultRecorder, int batchHint) {
      this.traverser = traverser;
      this.traverserName = traverserName;
      this.batchResultRecorder = batchResultRecorder;
      this.batchHint = batchHint;
  }

  @Override
  public void cancel() {
   traverser.cancelBatch();
  }

  @Override
  public void run() {
    //int batchDone = Traverser.ERROR_WAIT;
    LOGGER.finest("Begin runBatch; traverserName = " + traverserName
        + "batchHint = " + batchHint);
    int legacyBatchResult = traverser.runBatch(batchHint);
    BatchResult batchResult = BatchResult.newBatchResultFromLegacyBatchResult(legacyBatchResult);
    LOGGER.finest("Traverser " + traverserName +" batchDone with result = " + batchResult);
    //TODO(strellis): The original code did not record a result if the
    //    batch was canceled. I think a cancel was probably possible
    //    after the check and before recording results. Should I
    //    replace that check?
    batchResultRecorder.recordResult(batchResult);
  }

  @Override
  public String toString() {
    return "CancelableBatch traverser: "
        + traverser
        + " batchHint"
        + batchHint;
  }
}
