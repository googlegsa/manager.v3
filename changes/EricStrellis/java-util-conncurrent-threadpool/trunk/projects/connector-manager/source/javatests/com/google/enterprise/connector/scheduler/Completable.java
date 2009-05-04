package com.google.enterprise.connector.scheduler;

/**
 * A {@link Runnable} that supports cancellation.
 * @author EricStrellis@gmail.com (Eric Strellis)
 */
public interface Completable extends Runnable{
  public void onCompletion();
}
