// Copyright 2010 Google Inc. All Rights Reserved.

package com.google.enterprise.connector.sp2cloud;

/**
 * Checks if users and groups are provisioned.
 */
public interface ProvisionChecker {
  /**
   * Returns true if the specified user is provisioned in this
   * {@link ProvisionChecker} objects domain.
   */
  boolean isUserProvisioned(String userName) throws Exception;
  /**
   * Returns true if the specified group is provisioned in this
   * {@link ProvisionChecker} objects domain.
   */
  boolean isGroupProvisioned(String groupName) throws Exception;
}
