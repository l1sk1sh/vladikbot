package com.l1sk1sh.vladikbot.models;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @param <K> key type
 * @param <V> cache item type
 * @author l1sk1sh
 * Changes from original source:
 * - Reformatted code
 * @author John Grosh
 */
public class FixedCache<K, V> {
    private final Map<K, V> map;
    private final K[] keys;
    private int currIndex = 0;

    @SuppressWarnings("unchecked")
    public FixedCache(int size) {
        this.map = new HashMap<>();
        if (size < 1) {
            throw new IllegalArgumentException("Cache size must be at least 1!");
        }
        this.keys = (K[]) new Object[size];
    }

    public V put(K key, V value) {
        if (map.containsKey(key)) {
            return map.put(key, value);
        }
        if (keys[currIndex] != null) {
            map.remove(keys[currIndex]);
        }
        keys[currIndex] = key;
        currIndex = (currIndex + 1) % keys.length;
        return map.put(key, value);
    }

    public V pull(K key) {
        return map.remove(key);
    }

    @SuppressWarnings("unused")
    public V get(K key) {
        return map.get(key);
    }

    @SuppressWarnings("unused")
    public boolean contains(K key) {
        return map.containsKey(key);
    }

    public Collection<V> getValues() {
        return map.values();
    }
}
