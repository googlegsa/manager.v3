// Copyright 2007 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.spi;

/**
 * The carrier type of the list returned by the
 * {@link AuthorizationManager}.authorizeDocids
 * method.
 */
public class AuthorizationResponse {

  /**
   * Authorization Status codes.
   * <ul>
   * <li>{@code PERMIT} means that authorization is granted.</li>
   * <li>{@code DENY} means that authorization is positively denied.</li>
   * <li>{@code INDETERMINATE} means that permission is neither granted nor
   * denied. If a consumer receives this code, it may decide to try other means
   * to get a positive decision, permit or deny.</li>
   * </ul>
   * Note: at present (Connector Manager 2.0, GSA 6.2 and earlier),
   * the {@code INDETERMINATE} status is treated exactly the same as the
   * {@code DENY} status.  This is expected to change in the near future.
   *
   * @since 2.4
   */
  public enum Status {
    PERMIT, DENY, INDETERMINATE
  }

  private final String docid;
  private final Status status;

  /**
   * Makes an AuthorizationResponse. If {@code valid} is true, then
   * {@code status} is set to {@code Status.PERMIT}. If {@code valid} is false,
   * {@code status} is set to {@code Status.DENY}.
   *
   * @param valid Indicates that authorization was successful (valid)
   * @param docid The docid for which authorization succeeded - should not be
   *        null or empty
   */
  public AuthorizationResponse(boolean valid, String docid) {
    if (docid == null) {
      throw new IllegalArgumentException();
    }
    this.docid = docid;
    this.status = valid ? Status.PERMIT : Status.DENY;
  }

  /**
   * Makes an AuthorizationResponse.
   *
   * @param status the {@code Status} of this response
   * @param docid The docid for which authorization succeeded - should not be
   *        null or empty
   * @since 2.4
   */
  public AuthorizationResponse(Status status, String docid) {
    if (docid == null) {
      throw new IllegalArgumentException();
    }
    this.docid = docid;
    this.status = status;
  }

  /**
   * Tests whether authorization was valid (permitted).
   *
   * @return true if {@code status} is {@code PERMIT}, false otherwise.
   */
  public boolean isValid() {
    return (status == Status.PERMIT);
  }

  /**
   * Gets the docid.
   *
   * @return docid - should not be null or empty
   */
  public String getDocid() {
    return docid;
  }

  /**
   * Gets the status.
   *
   * @return status the {@code Status}
   * @since 2.4
   */
  public Status getStatus() {
    return status;
  }

  /**
   * Returns a hash code value for the object.
   *
   * @return a hash code value for this object.
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((docid == null) ? 0 : docid.hashCode());
    result = prime * result + ((status == null) ? 0 : status.hashCode());
    return result;
  }

  /**
   * Indicates whether some other object is "equal to" this one. Implemented by
   * running equals on the docid string and comparing the status.
   *
   * @return true if this object is the same as the obj argument; false
   *         otherwise.
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    AuthorizationResponse other = (AuthorizationResponse) obj;
    if (docid == null) {
      if (other.docid != null) {
        return false;
      }
    } else if (!docid.equals(other.docid)) {
      return false;
    }
    if (status == null) {
      if (other.status != null) {
        return false;
      }
    } else if (!status.equals(other.status)) {
      return false;
    }
    return true;
  }
}
