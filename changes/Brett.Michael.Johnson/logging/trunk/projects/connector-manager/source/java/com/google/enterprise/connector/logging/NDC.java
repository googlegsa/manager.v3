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

import java.util.Stack;
import java.util.EmptyStackException;

/**
 * Traditional Nested Diagnostic Context.
 */
public class NDC {
  private static final String EMPTY_STRING = "";
  private static ThreadLocal<Stack<String>> stack =
      new ThreadLocal<Stack<String>>();

  /**
   * Clear any nested diagnostic information, if any.
   */
  static void clear() {
    getContextStack().clear();
  }

  /**
   * Clone the diagnostic context for the current thread.
   */
  @SuppressWarnings("unchecked")    // clone() returns Object.
  static Stack<String> cloneStack() {
    return (Stack<String>)(getContextStack().clone());
  }

  /**
   * Get the NDC context at the top of the stack.
   */
  static String get() {
    return peek();
  }

  /**
   * Get the current depth of the context stack.
   */
  static int getDepth() {
    return getContextStack().size();
  }

  /**
   * Inherit the diagnostic context from anther thread.
   */
  static void inherit(Stack<String> newStack) {
    remove();
    stack.set(newStack);
  }

  /**
   * Peek at the context at the top of the stack.
   */
  static String peek() {
    try {
      return getContextStack().peek();
    } catch (EmptyStackException e) {
      return EMPTY_STRING;
    }
  }

  /**
   * Push new diagnostic context information for the current thread.
   *
   */
  public static void push(String message) {
    getContextStack().push(message);
  }

  /**
   * Push new diagnostic context information for the current thread.
   * The new context information is formed by appending the supplied
   * message to the current context.
   */
  public static void pushAppend(String message) {
    String current = peek();
    if (current.length() > 0) {
      push(current + " " + message);
    } else {
      push(message);
    }
  }


  /**
   * Remove the String at the top of the context stack and return it.
   */
  public static String pop() {
    try {
      return getContextStack().pop();
    } catch (EmptyStackException e) {
      return EMPTY_STRING;
    }
  }

  /**
   * Remove the diagnostic context from the thread.
   */
  public static void remove() {
    Stack<String> mdcStack = stack.get();
    if (mdcStack != null) {
      mdcStack.clear();
    }
    stack.remove();
  }

  /**
   * Return this thread's context stack.  If this thread has no
   * context stack, create one.
   */
  private static Stack<String> getContextStack() {
    Stack<String> mdcStack = stack.get();
    if (mdcStack == null) {
      mdcStack = new Stack<String>();
      stack.set(mdcStack);
    }
    return mdcStack;
  }
 }
