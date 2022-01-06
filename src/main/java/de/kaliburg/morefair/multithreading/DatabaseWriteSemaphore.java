package de.kaliburg.morefair.multithreading;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class DatabaseWriteSemaphore {
    private static DatabaseWriteSemaphore instance;
    private final Semaphore semaphore;

    public DatabaseWriteSemaphore() {
        semaphore = new Semaphore(1);
    }

    public static DatabaseWriteSemaphore getInstance() {
        if (instance == null) {
            instance = new DatabaseWriteSemaphore();
        }
        return instance;
    }

    public void acquire() throws InterruptedException {
        semaphore.acquire();
    }

    public void release() throws InterruptedException {
        semaphore.release();
    }

    public boolean tryAcquire() {
        return semaphore.tryAcquire();
    }

    public boolean tryAcquire(long timeout, TimeUnit unit) throws InterruptedException {
        return semaphore.tryAcquire(timeout, unit);
    }

    public void aquireAndAutoReleaseSilent(UninteruptableFunction f) {
        try {
            this.acquire();
            try {
                f.run();
            } finally {
                this.release();
            }
        } catch (InterruptedException e) {

        }
    }

    public void aquireAndAutoReleaseSilent(UninteruptableFunctionVoid f) {
        try {
            this.acquire();
            try {
                f.run();
            } finally {
                this.release();
            }
        } catch (InterruptedException e) {

        }
    }
}
