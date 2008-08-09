// Copyright (C) 2008 Google Inc.
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

package com.google.enterprise.connector.pusher;

import java.io.IOException;
import java.util.logging.FileHandler;

/**
 * Subclass of the logging FileHandler that allows retrieval of the pattern
 * used to construct log file names.
 */
public class FeedFileHandler extends FileHandler {

  private String pattern;

  /*
   * Wrapped constructors that save away the supplied pattern.
   */
  public FeedFileHandler() throws IOException, SecurityException {
    // Default java.util.logging.FileHandler pattern according to the javadoc.
    this("%h/java%u.log");
  }

  public FeedFileHandler(String pattern)
      throws IOException, SecurityException {
    super(pattern);
    this.pattern = pattern;
  }

  public FeedFileHandler(String pattern, boolean append)
      throws IOException, SecurityException {
    super(pattern, append);
    this.pattern = pattern;
  }

  public FeedFileHandler(String pattern, int limit, int count)
      throws IOException, SecurityException {
    super(pattern, limit, count);
    this.pattern = pattern;
  }

  public FeedFileHandler(String pattern, int limit, int count, boolean append)
      throws IOException, SecurityException {
    super(pattern, limit, count, append);
    this.pattern = pattern;
  }

  /**
   * Return the pattern string used by this logging FileHandler.
   */
  public String getPattern() {
    return pattern;
  }
}
