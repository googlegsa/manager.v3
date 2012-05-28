// Copyright 2011 Google Inc.
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

package org.slf4j;

/**
 * Stubbed out SLF4J LoggerFactory for the benefit of mime-util.
 */
public class LoggerFactory {
  public static org.slf4j.Logger getLogger(Class clazz) {
    return getLogger(clazz.getName());
  }

  public static org.slf4j.Logger getLogger(String className) {
    return new LoggerImpl(java.util.logging.Logger.getLogger(className));
  }
}
