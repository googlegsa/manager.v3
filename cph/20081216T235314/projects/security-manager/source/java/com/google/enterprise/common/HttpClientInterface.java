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

package com.google.enterprise.common;

import java.net.URL;
import java.util.List;

/**
 * An abstraction to hide HttpClient behind.
 * This allows HTTP transport to be mocked for testing.
 */
public interface HttpClientInterface {
  /**
   * Create a new HTTP exchange object.
   *
   * @param method The HTTP method for the request (GET or POST).
   * @param url The URL to send the request to.
   * @param parameters The POST parameters if this is a POST.
   *     For a GET request, this should be {@code null}.
   * @return A new HTTP exchange object.
   */
  public HttpExchange newExchange(String method, URL url, List<StringPair> parameters);
}
