// Copyright (C) 2008 Google Inc.
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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * An abstraction for an HTTP exchange.  @see HttpClientInterface
 */
public interface HttpExchange {

  /**
   * Set the proxy to use for the exchange.
   * @param proxy The proxy host and port.
   */
  public void setProxy(String proxy);

  /**
   * Set credentials to use for basic auth.
   * @param username
   * @param password
   */
  public void setBasicAuthCredentials(String username, String password);

  /**
   * Set credentials to use for NTLM auth.
   * @param domain
   * @param username
   * @param password
   */
  public void setNtlmAuthCredentials(String domain, String username, String password);

  /**
   * Tell exchange whether to follow redirects.
   * Default is to not follow them.
   * @param followRedirects {@code true} means follow them.
   */
  public void setFollowRedirects(boolean followRedirects);

  /**
   * Get the HTTP method name.
   * @return The HTTP method string: e.g. GET or POST.
   */
  public String getHttpMethod();

  /**
   * Add a parameter to the exchange.
   * Works only for POST methods.
   * @param name The parameter's name.
   * @param value The parameter's value.
   */
  public void addParameter(String name, String value);

  /**
   * Set an HTTP request header field.
   *
   * Overrides any previous header of that name.
   * @param name The header's name (case insensitive).
   * @param value The header's value.
   */
  public void setRequestHeader(String name, String value);

  /**
   * Perform the HTTP exchange.
   * @return The status code from the exchange.
   * @throws IOException if there's a transport error
   */
  public int exchange() throws IOException;

  /**
   * Get the response entity (body) as a string.
   * @return The entity.
   * @throws IOException if there's a transport error
   */
  public String getResponseEntityAsString() throws IOException;

  /**
   * Get the response entity (body) as an input stream.
   * @return The entity input stream.
   * @throws IOException if there's a transport error
   */
  public InputStream getResponseEntityAsStream() throws IOException;

  /**
   * Get the value of a response header field.
   * @param name The name of the header.
   * @return The value of the named header, or null if no such header.
   */
  public String getResponseHeaderValue(String name);

  /**
   * Get the values of a group of response header fields.
   * @param name The name of the headers.
   * @return The values of all the headers with that name.
   *     The returned list is empty if there are no such headers.
   */
  public List<String> getResponseHeaderValues(String name);

  /**
   * Get the status code from the exchange.
   * @return The status code.
   */
  public int getStatusCode();

  /**
   * Set the entity of the request.
   * @param byteArrayRequestEntity The bytes to use as an entity.
   */
  public void setRequestBody(byte[] byteArrayRequestEntity);

  /**
   * Close the exchange and reclaim its resources.
   */
  public void close();
}
