package de.kaliburg.morefair.core;

import com.github.benmanes.caffeine.cache.LoadingCache;
import java.util.concurrent.Semaphore;
import lombok.Getter;

public class AbstractCacheableService {

  @Getter
  protected final Semaphore cacheSemaphore = new Semaphore(1);

  protected <K, V> V getMessageEntityFromCache(LoadingCache<K, V> cache,
      K key) {
    try {
      cacheSemaphore.acquire();
      try {
        return cache.get(key);
      } finally {
        cacheSemaphore.release();
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    return null;
  }
}
