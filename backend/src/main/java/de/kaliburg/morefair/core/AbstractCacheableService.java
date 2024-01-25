package de.kaliburg.morefair.core;

import com.github.benmanes.caffeine.cache.LoadingCache;
import de.kaliburg.morefair.core.concurrency.CriticalRegion;
import lombok.Getter;

public class AbstractCacheableService {

  @Getter
  protected final CriticalRegion cacheSemaphore = new CriticalRegion(1);

  protected <K, V> V getValueFromCacheSync(LoadingCache<K, V> cache, K key) {
    try (var ignored = cacheSemaphore.enter()) {
      return cache.get(key);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    return null;
  }
}
