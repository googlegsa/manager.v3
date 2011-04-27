// Copyright (C) 2006 Google Inc.
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

package com.google.enterprise.connector.spi;

/**
 * The root of the SPI for connector instances. The connector manager will use
 * <a href="http://www.springframework.org/">Spring</a> to instantiate objects
 * that implement this interface. The implementor MUST provide a Spring XML
 * configuration file named {@code connectorInstance.xml} to control this
 * process. See the package documentation for more details.
 * <p/>
 * A {@code Connector} object is used as something against which to
 * authenticate, via the {@link #login()} method. By authenticating,
 * one gets a {@link Session}, which then gives
 * access to all other services. Note, this is presumably "superuser", one-time
 * only authentication, not authentication for a given user query session (which
 * takes place via the {@link AuthenticationManager}).
 */
public interface Connector {

  /**
   * Gets a session with sufficient privileges to perform all actions related to
   * the SPI. Credentials, if needed, are supplied externally through injection
   * via Spring or some other mechanism. If an exception is thrown, the
   * implementor should provide an explanatory message.
   *
   * @return an object implementing the {@link Session} interface
   * @throws RepositoryLoginException
   *           if there is a credentials-related problem
   * @throws RepositoryException
   *           if there is a more general problem, such as the system is
   *           unreachable or down
   */
  public Session login() throws RepositoryLoginException, RepositoryException;
}