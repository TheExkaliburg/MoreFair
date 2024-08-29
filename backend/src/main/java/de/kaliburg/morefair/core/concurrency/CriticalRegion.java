package de.kaliburg.morefair.core.concurrency;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class is a utility class for exception safe usage of a semaphore. When returns
 * {@code semaphore.acquire() } the current thread successfully entered the critical region, but it
 * has to call {@code semaphore.release() } on exit to make room for other threads to enter the
 * critical region. If this call is not guaranteed, thread starvation can occur (or even deadlock)
 * since threads trying to acquire will wait forever.
 *
 * <p>This class solves this problem by returning an {@link AutoCloseable} object on entering the
 * critical region which calls {@code semaphore.release() } on close. Using this in conjunction with
 * a try with resources statement guarantees close() to be called.
 *
 * <p>Example:
 * <pre>
 * CriticalRegion criticalRegion = new CriticalRegion(4);
 * try( Permit permit = criticalRegion.enter() ) {
 *    // do critical things here ..
 * } catch (InterruptedException e) {
 *    System.err.println("Thread interrupted before permit into critical region was acquired");
 *    e.printStackTrace();
 * }
 * </pre>
 *
 * @author hageldave
 */
public class CriticalRegion {

  private final Semaphore semaphore;

  /**
   * Creates a CriticalRegion with the specified number of permits, and fair 'entrance' into the
   * region. See {@link Semaphore#Semaphore(int)} for the exact meaning of this parameter.
   *
   * @param permits num of concurrent threads that can enter the region simultaneously.
   */
  public CriticalRegion(int permits) {
    semaphore = new Semaphore(permits, true);
  }

  /**
   * Attempts entering the critical region, blocking until entered.
   *
   * @return Permit (use this as a resource in try with resources statement)
   * @throws InterruptedException if the current thread is interrupted
   */
  public Permit enter() throws InterruptedException {
    return new Permit();
  }


  /**
   * {@link AutoCloseable} that calls {@code semaphore.release() } on close. This is acquired
   * through {@link CriticalRegion#enter()}
   *
   * @author haegele
   */
  public class Permit implements AutoCloseable {

    private final AtomicBoolean isClosed = new AtomicBoolean(false);

    private Permit() throws InterruptedException {
      semaphore.acquire();
    }

    @Override
    public void close() {
      if (isClosed.compareAndSet(false, true)) {
        semaphore.release();
      }
    }
  }
}
