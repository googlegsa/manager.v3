// Copyright 2007 Google Inc. All Rights Reserved.
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

package com.google.enterprise.connector.traversal;

/**
 * Provides context to the traversal process on how big is too big.
 */
public class FileSizeLimitInfo {

  // Hard maximum feed file for GSA is 1GB, but we can exceed our
  // maxFeedSize by the size of a single document, so cap our max
  // a little lower than the GSA hard maximum.
  private static final long MAXIMUM_FEED_FILE_SIZE = 900L * 1024 * 1024;

  // Hard maximum document size is what could fit in to the hard
  // maximum feed file size when Base64 encoded.
  private static final long MAXIMUM_DOCUMENT_SIZE =
      (MAXIMUM_FEED_FILE_SIZE / 4) * 3;

  // Default maximum document size is 30MB - maximum supported by GSA.
  private long maxDocumentSize = 30L * 1024 * 1024;

  // Default target feed size is 10MB - about 75-100 typical documents.
  private long maxFeedSize = 10 * 1024 * 1024;

  public void setMaxDocumentSize(long maxDocumentSize) {
    if (maxDocumentSize <= 0) {
      throw new IllegalArgumentException("maxDocumentSize must be positive.");
    }
    if (maxDocumentSize > MAXIMUM_DOCUMENT_SIZE) {
      this.maxDocumentSize = MAXIMUM_DOCUMENT_SIZE;
    } else {
      this.maxDocumentSize = maxDocumentSize;
    }
  }

  public long maxDocumentSize() {
    return maxDocumentSize;
  }

  public void setMaxFeedSize(long maxFeedSize) {
    if (maxFeedSize <= 0) {
      throw new IllegalArgumentException("maxFeedSize must be positive.");
    }
    if (maxFeedSize > MAXIMUM_FEED_FILE_SIZE) {
      this.maxFeedSize = MAXIMUM_FEED_FILE_SIZE;
    } else {
      this.maxFeedSize = maxFeedSize;
    }
  }

  public long maxFeedSize() {
    return maxFeedSize;
  }

  @Override
  public String toString() {
    return "FileSizeLimitInfo: maxDocumentSize = " + maxDocumentSize
           + ", maxFeedSize = " + maxFeedSize;
  }
}
