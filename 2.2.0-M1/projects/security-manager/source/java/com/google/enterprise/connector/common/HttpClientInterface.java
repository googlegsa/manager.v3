// Copyright 2008 Google Inc.  All Rights Reserved.
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

package com.google.enterprise.connector.common;

import java.net.URL;
import java.util.List;

/**
 * An abstraction to hide HttpClient behind.
 * This allows HTTP transport to be mocked for testing.
 */
public interface HttpClientInterface {
  /**
   * Create a new HTTP GET exchange object.
   *
   * @param url The URL to send the request to.
   * @return A new HTTP exchange object.
   */
  public HttpExchange getExchange(URL url);

  /**
   * Create a new HTTP POST exchange object.
   *
   * @param url The URL to send the request to.
   * @param parameters The POST parameters.
   * @return A new HTTP exchange object.
   */
  public HttpExchange postExchange(URL url, List<StringPair> parameters);
}
