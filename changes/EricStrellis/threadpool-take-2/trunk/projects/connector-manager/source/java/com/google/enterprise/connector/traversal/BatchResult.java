package com.google.enterprise.connector.traversal;

public class BatchResult {
  final TraversalDelayPolicy delayPolicy;
  final int countProcessed;

  public BatchResult(TraversalDelayPolicy delayPolicy, int countProcessed) {
    this.delayPolicy = delayPolicy;
    this.countProcessed = countProcessed;
  }

  public TraversalDelayPolicy getDelayPolicy() {
    return delayPolicy;
  }

  public int getCountProcessed() {
    return countProcessed;
  }

  // TODO(strellis): Remove this when Traverser returns one of these
  public static BatchResult newBatchResultFromLegacyBatchResult(int legacyBatchResult) {
      int countProcessed = legacyBatchResult < 0 ? 0 : legacyBatchResult;
      TraversalDelayPolicy delayPolicy =
        TraversalDelayPolicy.getTraversalDealyPolicyFromLegacyBatchResult(legacyBatchResult);
      return new BatchResult(delayPolicy, countProcessed);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + countProcessed;
    result = prime * result + ((delayPolicy == null) ? 0 : delayPolicy.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    BatchResult other = (BatchResult) obj;
    if (countProcessed != other.countProcessed) return false;
    if (delayPolicy == null) {
      if (other.delayPolicy != null) return false;
    } else if (!delayPolicy.equals(other.delayPolicy)) return false;
    return true;
  }

  @Override
  public String toString() {
    return "BatchResult: delayPolicy = " + delayPolicy + " countProcessed = " + countProcessed;
  }
}
