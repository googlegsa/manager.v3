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
 * The response for most configuration commands.
 * 
 */
public class ConfigurerResponse {

  private String message = null;
  private String formSnippet = null;
  private int status;

  /**
   * Status code indicating that all is well.
   */
  public static final int STATUS_OK = 0;

  /**
   * Status code indicating that a named connector was not found.
   */
  public static final int STATUS_CONNECTOR_NOT_FOUND = 1;

  /**
   * Status code indicating that the caller should try again.
   */
  public static final int STATUS_TRY_AGAIN = 2;

  /**
   * The primary constructor. The common semantics of the field is described
   * below, but there may be special semantics depending on the call that
   * produces this type. See the Configurer interface methods for more details.
   * 
   * @param message A message to be included to the user along with the form.
   *        This message may be null or empty - no distinction is made between
   *        those cases. The message should be plain text - may not contain
   *        script directives.
   * @param formSnippet A sequence of &lt;tr&gt; elements, each of which
   *        contains two &lt;td&gt; fields, first is the description of
   *        configuration element, second is an HTML input field. The snippet
   *        may be null or empty.
   * @param status An integer status that should be one of the status constants
   *        defined here. The exact meanings and possible returns depend on the
   *        method call that produces the object.
   */
  public ConfigurerResponse(String message, String formSnippet, int status) {
    super();
    this.message = message;
    this.formSnippet = formSnippet;
    this.status = status;
  }

  /**
   * Gets the message
   * 
   * @return the message - may be null or empty
   */
  public String getMessage() {
    return message;
  }

  /**
   * Gets the form snippet.
   * 
   * @return the form snippet - may be null or empty
   */
  public String getFormSnippet() {
    return formSnippet;
  }

  /**
   * Gets the status.
   * 
   * @return the status - STATUS_OK indicates complete success. See the
   *         documentation for the methods that return this type for more
   *         details.
   */
  public int getStatus() {
    return status;
  }

}
