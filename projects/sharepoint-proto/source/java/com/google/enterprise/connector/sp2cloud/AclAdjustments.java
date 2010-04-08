package com.google.enterprise.connector.sp2cloud;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AclAdjustments {
  private final List<CloudAce> inserts;
  private final List<CloudAce> updates;
  private final List<CloudAce> deletes;

  AclAdjustments(List<CloudAce> inserts, List<CloudAce> updates,
      List<CloudAce> deletes) {
    this.inserts = Collections.unmodifiableList(
        new ArrayList<CloudAce>(inserts));
    this.updates = Collections.unmodifiableList(
        new ArrayList<CloudAce>(updates));
    this.deletes = Collections.unmodifiableList(
        new ArrayList<CloudAce>(deletes));
  }

  List<CloudAce> getInserts() {
    return inserts;
  }

  List<CloudAce> getUpdates() {
    return updates;
  }

  List<CloudAce> getDeletes() {
    return deletes;
  }

  @Override
  public String toString() {
    return "AclAdjustments:"
        + " inserts: " + inserts
        + " updates: " + updates
        + " deletes: " + deletes;
  }
}
