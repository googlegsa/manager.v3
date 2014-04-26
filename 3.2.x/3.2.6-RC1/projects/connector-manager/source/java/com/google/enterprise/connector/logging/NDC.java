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

import java.util.EmptyStackException;
import java.util.Stack;

/**
 * Traditional Nested Diagnostic Context.  The interface is similar
 * to the NDC class presented by the widely-used log4j.
 *
 * <p>This maintains a stack of diagnostic context messages that
 * is maintained on a per-thread basis.  It is primarily used
 * to add per-thread context while logging.
 *
 * <p>When entering a new context, a thread might call {@link #push(String)}
 * or {@link #pushAppend(String)} to add a new context message at the top
 * of the stack.  When leaving the context, the thread should call
 * {@link #pop} to restore the previous context message.
 *
 * <p>If the context message stack is no longer needed, it may be
 * cleared by calling {@link #clear}.  This discards any messages
 * left on the stack, but retains the ThreadLocal used to maintain
 * the stack.  This is useful if the thread will be reused in the
 * future, and desire diagnostic context.  For instance, threads
 * in a thread-pool handling servlet requests could create a new
 * context when a thread is taken from the pool and given a request
 * packet.  The context message could uniquely identify the request
 * by requester, port, cookie, etc.  Once the request has been
 * serviced, the thread could clear the NDC stack just before being
 * returned to the thread-pool.
 *
 * <p>When a thread will be unlikely to use a diagnostic context in
 * the future, it should call {@link #remove} to release the ThreadLocal
 * resources used by the context.  This is especially important if
 * when the thread exits, so these resources may get garbage collected.
 */
 public class NDC {
   private static final String EMPTY_STRING = "";
   private static final ThreadLocal<Stack<String>> stack =
       new ThreadLocal<Stack<String>>();

  /**
   * Clear any nested diagnostic information, if any, but maintain the
   * NDC ThreadLocal.  This is useful for threads that will be reused
   * with a diagnostic context, such as those in a thread-pool.
   */
  public static void clear() {
    getContextStack().clear();
  }

  /**
   * Return the current nesting depth of this diagnostic context.
   */
  public static int getDepth() {
    return getContextStack().size();
  }

  /**
   * Push new diagnostic context information for the current thread.
   *
   * @param message The new diagnostic context information.
   */
  public static void push(String message) {
    getContextStack().push(message);
  }

  /**
   * Push new diagnostic context information for the current thread.
   * The new context information is formed by appending the supplied
   * message to the current context.  If there is no current context,
   * the new context is simply the supplied message.
   *
   * @param message The new diagnostic context information to append
   *        to the current context.
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
   * Append the supplied message to the current diagnostic context,
   * if one exists.  If there is no current context, do nothing.
   *
   * @param message The new diagnostic context information to append
   *        to the current context.
   */
  public static void append(String message) {
    try {
      String current = getContextStack().pop();
      if (current.length() > 0) {
        push(current + " " + message);
      } else {
        push(message);
      }
    } catch (EmptyStackException ignored) {
      // Do nothing.
    }
  }

  /**
   * Looks at the last diagnostic context at the top of the context
   * stack without removing it.
   *
   * @return The value that was pushed last.  If the stack is empty
   *         the empty string is returned.
   */
  public static String peek() {
    try {
      return getContextStack().peek();
    } catch (EmptyStackException e) {
      return EMPTY_STRING;
    }
  }

  /**
   * Remove the String at the top of the context stack and return it.
   *
   * @return The value that was pushed last.  If the stack is empty
   *         the empty string is returned.
   */
  public static String pop() {
    try {
      return getContextStack().pop();
    } catch (EmptyStackException e) {
      return EMPTY_STRING;
    }
  }

  /**
   * Remove the diagnostic context from the thread.  This clears the
   * context stack and removes the NDC ThreadLocal.
   *
   * <p>Each thread that created a diagnostic context by calling
   * {@link #push push()} should call this method before exiting.
   * Otherwise, the memory used by the thread cannot be reclaimed by the VM.
   */
  public static void remove() {
    stack.remove();
  }

  /**
   * Return this thread's context stack.  If this thread has no
   * context stack, create one.
   *
   * @return The contextStack.
   */
  private static Stack<String> getContextStack() {
    Stack<String> ndcStack = stack.get();
    if (ndcStack == null) {
      ndcStack = new Stack<String>();
      stack.set(ndcStack);
    }
    return ndcStack;
  }
}