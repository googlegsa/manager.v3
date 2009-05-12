package com.google.enterprise.connector.traversal;

public enum TraversalDelayPolicy {
  IMMEDIATE,
  POLL,
  ERROR;

  //TODO (strellis): remove this when Traverser.runBatch is converted to
  //  return a structured result.
  static TraversalDelayPolicy getTraversalDealyPolicyFromLegacyBatchResult(int intPolicy){
    if (intPolicy > 0) {
      intPolicy = 0;
    }
    return values()[-intPolicy];
  }
}
