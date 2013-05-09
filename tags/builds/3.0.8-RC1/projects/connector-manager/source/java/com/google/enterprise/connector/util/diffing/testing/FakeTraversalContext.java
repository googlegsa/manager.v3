// Copyright 2009 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.util.diffing.testing;

import com.google.enterprise.connector.spi.SimpleTraversalContext;
import com.google.enterprise.connector.spi.TraversalContext;

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Fake TraversalContext that implements the functions needed for testing.
 *
 * @since 2.8
 */
/* TODO: Deprecate this in favor of SimpleTraversalContext. */
public class FakeTraversalContext extends SimpleTraversalContext {
  public static final long DEFAULT_MAXIMUM_DOCUMENT_SIZE = 3000000L;
  public static final String TAR_DOT_GZ_EXTENSION = "tar.gz";
  private static final String TAR_DOT_GZ_MIME_TYPE = "application/x-gzip";

  AtomicBoolean allowAllMimeTypes = new AtomicBoolean();

  public FakeTraversalContext() {
    this(DEFAULT_MAXIMUM_DOCUMENT_SIZE);
  }

  public FakeTraversalContext(long maxDocumentSize) {
    setMaxDocumentSize(maxDocumentSize);
    setSupportsInheritedAcls(true);
    setSupportsDenyAcls(true);
    setTraversalTimeLimitSeconds(120);
  }

  public void allowAllMimeTypes(boolean newValue) {
    allowAllMimeTypes.set(newValue);
  }

  @Override
  public int mimeTypeSupportLevel(String mimeType) {
    if (allowAllMimeTypes.get()) {
      return 1;
    } else if (TAR_DOT_GZ_MIME_TYPE.equals(mimeType)) {
      return -1;
    } else {
      return 1;
    }
  }

  /**
   * Returns lexically first provided mime type.
   */
  @Override
  public String preferredMimeType(Set<String> mimeTypes) {
    if (mimeTypes.size() < 1) {
      throw new IllegalArgumentException("mimeTypes must have at least 1 entry");
    }
    String[] mta = mimeTypes.toArray(new String[mimeTypes.size()]);
    Arrays.sort(mta);
    return mta[0];
  }
}
