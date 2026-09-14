package com.todolist.repo;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class Cache<K, V> {

    private final int maxSize;
    private final Map<K, V> store;

    public Cache(int maxSize) {
        this.maxSize = maxSize;
        this.store = new LinkedHashMap<>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                return size() > Cache.this.maxSize;
            }
        };
    }

    public void put(K key, V value) {
        store.put(key, value);
    }

    public Optional<V> get(K key) {
        return Optional.ofNullable(store.get(key));
    }

    public void evict(K key) {
        store.remove(key);
    }

    public int size() {
        return store.size();
    }

    public void clear() {
        store.clear();
    }
}
