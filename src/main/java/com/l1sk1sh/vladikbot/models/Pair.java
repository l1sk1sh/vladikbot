package com.l1sk1sh.vladikbot.models;

/**
 * @author Oliver Johnson
 * Changes from original source:
 * - Reformatted code
 * @author John Grosh
 */
public class Pair<K, V> {
    private final K key;
    private final V value;

    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public final K getKey() {
        return key;
    }

    public final V getValue() {
        return value;
    }
}
