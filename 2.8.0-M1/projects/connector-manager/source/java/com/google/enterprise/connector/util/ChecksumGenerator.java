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

package com.google.enterprise.connector.util;

import java.io.IOException;
import java.io.InputStream;

/**
 * Provide checksums.
 */
public interface ChecksumGenerator {
  /**
   * @param in
   * @return a checksum of bytes read from {@code in}.
   * @throws IOException
   */
  String getChecksum(InputStream in) throws IOException;

  /**
   * @param input
   * @return a checksum for the UTF_8 bytes of {@code input}.
   */
  String getChecksum(String input);
}
