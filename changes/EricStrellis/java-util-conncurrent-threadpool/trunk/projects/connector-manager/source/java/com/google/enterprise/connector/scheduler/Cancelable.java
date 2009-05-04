package com.google.enterprise.connector.scheduler;

/**
 * A {@link Runnable} that supports cancellation.
 * @author EricStrellis@gmail.com (Eric Strellis)
 */
public interface Cancelable extends Runnable {
  /**
   * Cancel the operation performed by this {@link Runnable}.
   * While this {@link Runnable#run} method is running in one thread this
   * may be called in another so implementors must provide any needed
   * synchronization.
   */
  public void cancel();
}
