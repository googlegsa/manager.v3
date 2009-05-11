// Copyright (C) 2009 Google Inc.
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

package com.google.enterprise.connector.logging;

import java.util.HashMap;
import java.util.Stack;
import java.util.EmptyStackException;

/**
 * Traditional Mapped Diagnostic Context with NDC-like Nesting enhancements.
 */
public class MDC {

  protected static ThreadLocal<HashMap<String, String>> context =
      new ThreadLocal<HashMap<String, String>>();

  private static ThreadLocal<Stack<HashMap<String, String>>> stack =
      new ThreadLocal<Stack<HashMap<String, String>>>();

  /**
   * Put a context value as identified by key
   * into the current thread's context map.
   *
   * @param key
   * @param value
   */
  public static void put(String key, String value) {
    getContext().put(key, value);
  }

  /**
   * Get the context identified by the key parameter.
   *
   * @param key
   * @return String value associated with that key,
   *         or empty string if not found.
   */
  public static String get(String key) {
    String value = getContext().get(key);
    return (value == null) ? "" : value;
  }

  /**
   * Remove the the context identified by the key parameter.
   *
   * @param key
   */
  public static void remove(String key) {
    getContext().remove(key);
  }

  /**
   * Clear all entries in the MDC.
   */
  public static void clear() {
    getContext().clear();
  }


  /**
   * Push a copy of the current mapped diagnostic context.
   * A subsequent call to <code>pop()</code> will restore
   * the context to the state it was when it was pushed.
   * <p>If you push the NDC context, you should employ a
   * <code>try {...} finally { NDC.pop(); }</code> construct to
   * avoid Throwables bypassing the corresponding <code>pop()</code>.
   */
  @SuppressWarnings("unchecked")    // clone() returns Object.
  public static void push() {
    getContextStack().push((HashMap<String, String>)(getContext().clone()));
  }

  /**
   * Restore the diagnositc context to last pushed state.
   */
  public static void pop() {
    Stack<HashMap<String, String>> stack = getContextStack();
    if (!stack.empty()) {
      try {
        context.set(stack.pop());
      } catch (EmptyStackException e) {
        // Shouldn't get here after testing stack.empty().
        // I certainly won't try to log a message here in the logging package.
        assert(false);
      }
    }
  }

  /**
   * Remove all the diagnostic context from the thread.
   */
  public static void remove() {
    HashMap<String, String> mdcContext = context.get();
    if (mdcContext != null) {
      mdcContext.clear();
    }
    context.remove();
    Stack<HashMap<String, String>> mdcStack = stack.get();
    if (mdcStack != null) {
      mdcStack.clear();
    }
    stack.remove();
  }

  /**
   * Return this thread's MDC context.  If this thread has no
   * MDC context, create one.
   */
  protected static HashMap<String, String> getContext() {
    HashMap<String, String> mdcContext = context.get();
    if (mdcContext == null) {
      mdcContext = new HashMap<String, String>(4);
      context.set(mdcContext);
    }
    return mdcContext;
  }

  /**
   * Return this thread's MDC context stack.  If this thread has no
   * context stack, create one.
   */
  private static Stack<HashMap<String, String>> getContextStack() {
    Stack<HashMap<String, String>> mdcStack = stack.get();
    if (mdcStack == null) {
      mdcStack = new Stack<HashMap<String, String>>();
      stack.set(mdcStack);
    }
    return mdcStack;
  }
 }
