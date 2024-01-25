package de.kaliburg.morefair.core.caching;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import de.kaliburg.morefair.core.concurrency.CriticalRegion;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MultiIndexedLoadingCache<K, V> {

  private final LoadingCache<K, V> cache;
  private final Map<Class<?>, LoadingCache<Object, K>> indexes = new HashMap<>();
  private final Function<V, K> getPrimaryKeyFunction;
  private final Map<Class<?>, Function<V, Object>> indexFunctions;
  private final CriticalRegion criticalRegion = new CriticalRegion(1);

  private MultiIndexedLoadingCache(Builder<K, V> builder) {
    this.cache = builder.caffeine.build(builder.loadFunction::apply);
    this.getPrimaryKeyFunction = builder.getPrimaryKeyFunction;
    this.indexFunctions = builder.getIndexFunction;
    for (var index : builder.indexes.entrySet()) {
      LoadingCache<Object, K> lookup = builder.caffeine.build(
          id -> index.getValue().apply(id)
      );
      this.indexes.put(index.getKey(), lookup);
    }
  }

  public static <K, V> Builder<K, V> builder(Caffeine<Object, Object> caffeine,
      Function<V, K> getPrimaryKeyFunction, Function<K, V> loadFunction) {
    return new Builder<>(caffeine, getPrimaryKeyFunction, loadFunction);
  }

  public V get(K key) {
    try (var ignored = criticalRegion.enter()) {
      return cache.get(key);
    } catch (InterruptedException e) {
      log.error("Thread interrupted before permit into critical region was acquired", e);
      return null;
    }
  }

  public V lookup(Object o) {
    try (var ignored = criticalRegion.enter()) {
      K key = indexes.get(o.getClass()).get(o);

      return key != null ? cache.get(key) : null;
    } catch (InterruptedException e) {
      log.error("Thread interrupted before permit into critical region was acquired", e);
      return null;
    }
  }

  public V put(V value) {
    return put(() -> value);
  }

  public V put(Supplier<V> supplier) {
    try (var ignored = criticalRegion.enter()) {
      V value = supplier.get();
      
      if (value == null) {
        return null;
      }

      K key = getPrimaryKeyFunction.apply(value);

      cache.put(key, value);
      for (Entry<Class<?>, LoadingCache<Object, K>> classLoadingCacheEntry : indexes.entrySet()) {
        Function<V, Object> getLookupKey = indexFunctions.get(classLoadingCacheEntry.getKey());

        classLoadingCacheEntry.getValue()
            .put(getLookupKey.apply(value), key);
      }
      return value;
    } catch (InterruptedException e) {
      log.error("Thread interrupted before permit into critical region was acquired", e);
      return null;
    }
  }

  public static class Builder<K, V> {

    private final Map<Class<?>, Function<V, Object>> getIndexFunction = new HashMap<>();
    private final Map<Class<?>, Function<Object, K>> indexes = new HashMap<>();
    private final Caffeine<Object, Object> caffeine;
    private final Function<V, K> getPrimaryKeyFunction;
    private final Function<K, V> loadFunction;

    public Builder(Caffeine<Object, Object> caffeine, Function<V, K> getPrimaryKeyFunction,
        Function<K, V> loadFunction) {
      this.caffeine = caffeine;
      this.getPrimaryKeyFunction = getPrimaryKeyFunction;
      this.loadFunction = loadFunction;
    }

    public Builder<K, V> addLookup(Class<?> clazz, Function<V, Object> getIdFunction,
        Function<Object, V> loadFunction) {
      addLookupWithOptional(clazz, getIdFunction,
          id -> Optional.ofNullable(loadFunction.apply(id)));
      return this;
    }

    public Builder<K, V> addLookupWithOptional(Class<?> clazz, Function<V, Object> getIdFunction,
        Function<Object, Optional<V>> loadFunction) {
      Function<Object, K> lookupFunction = id -> loadFunction.apply(id).map(getPrimaryKeyFunction)
          .orElse(null);

      indexes.put(clazz, lookupFunction);
      getIndexFunction.put(clazz, getIdFunction);
      return this;
    }


    public MultiIndexedLoadingCache<K, V> build() {
      return new MultiIndexedLoadingCache<>(this);
    }
  }

}
