// Copyright 2009 Google Inc. All Rights Reserved.
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

package com.google.enterprise.connector.manager;

/**
 * The main interface into services that can be added to the {@link Context}.  A
 * service is a light weight process or operation that is started within the
 * scope of the Web Application when the {@link Context} is initialized and is
 * stopped when the {@link Context} is shutdown.  Services can start threads or
 * manage configuration.
 * <p>
 * Services are normally started and stopped in any order.  If a service needs
 * to declare its load order with respect to other services it should be done
 * within the OrderedServices bean.
 */
public interface ContextService {

  /**
   * Causes this service to begin execution or to affect its service on the
   * application.
   */
  public void start();

  /**
   * Causes this service to gracefully stop execution or cleanup/collect any
   * state related to the application.
   *
   * @param force suggestion to the service to stop as soon as possible.
   */
  public void stop(boolean force);

  /**
   * Returns the current running state of the service.  If the service is
   * running it can be assumed that it has been started.
   *
   * @return true if the service is currently running.
   */
  public boolean isRunning();

  /**
   * Returns the name of the service.  Services used within the same
   * {@link Context} should have unique names.
   *
   * @return the current name for the service.
   */
  public String getName();
}
