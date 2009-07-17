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

/**
 * Traditional Mapped Diagnostic Context (MDC).  The interface is similar
 * to the MDC class  presented by the widely-used log4j.
 *
 * <p>The MDC class is similar to the {@link NDC} class except that it is
 * based on a Map instead of a Stack.  The MDC is managed on a per thread basis.
 * The Map contains key/value pairs that might be useful for distinguishing
 * different threads running similar tasks.  The {@link LayoutPattern} class
 * can insert the value associated with specified keys as it generates its
 * log messages.
 *
 * <p>A thread may assign an MDC mapping using {@link #put(String,String)},
 * and retrieve the value associated with a key in the map by calling
 * {@link #get(String)}.  An individual key/value association may be removed
 * from the MDC by calling {@link #remove(String)}.
 *
 * <p>the entire contents of the map may be cleared by calling {@link #clear}.
 * This discards any key/value associations in the map, but retains the
 * ThreadLocal used to maintain the map.  This is useful if the thread will
 * be reused in the future, and desire diagnostic context.  For instance,
 * threads in a thread-pool handling servlet requests could create a new
 * context when a thread is taken from the pool and given a request
 * packet.  The context item  could uniquely identify the request
 * by requester, port, cookie, etc.  Once the request has been
 * serviced, the thread could clear the MDC map just before being
 * returned to the thread-pool.
 *
 * <p>When a thread will be unlikely to use a diagnostic context in
 * the future, it should call {@link #remove()} to release the ThreadLocal
 * resources used by the context.  This is especially important if
 * when the thread exits, so these resources may get garbage collected.
 */
public class MDC {
  protected static final ThreadLocal<HashMap<String, String>> context =
      new ThreadLocal<HashMap<String, String>>();

  /**
   * Put a context <code>value</code> as identified by <code>key</code>
   * into the current thread's context map.
   *
   * <p>If the current thread does not have a context map it is
   * created as a side effect.
   *
   * @param key
   * @param value
   */
  public static void put(String key, String value) {
    getContext().put(key, value);
  }

  /**
   * Get the context identified by the <code>key<code> parameter.
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
   * Remove the the context identified by the <code>key</code> parameter.
   *
   * @param key
   */
  public static void remove(String key) {
    getContext().remove(key);
  }

  /**
   * Clear all entries in the MDC, but maintain the MDC ThreadLocal.
   * This is useful for threads that will be reused with a diagnostic
   * context, such as those in a thread-pool.
   */
  public static void clear() {
    getContext().clear();
  }

  /**
   * Remove all the diagnostic context from the thread.  This clears
   * the MDC, and removes the MDC ThreadLocal.
   *
   * <p>Each thread that created a diagnostic context by calling
   * {@link #put put()} should call this method before exiting. Otherwise,
   * the memory used by the thread cannot be reclaimed by the VM.
   */
  public static void remove() {
    context.remove();
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
}
