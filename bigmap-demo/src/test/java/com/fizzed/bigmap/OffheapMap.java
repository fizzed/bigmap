package com.fizzed.bigmap;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class OffheapMap<K,V> implements Map<K,V> {

    private final Path directory;
    private final Map<K,V> wrapped;

    public OffheapMap(Path directory, Map<K,V> wrapped) {
        this.directory = directory;
        this.wrapped = wrapped;
    }

    public Path getDirectory() {
        return directory;
    }

    @Override
    public int size() {
        return this.wrapped.size();
    }

    @Override
    public boolean isEmpty() {
        return this.wrapped.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return this.wrapped.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return this.wrapped.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return this.wrapped.get(key);
    }

    @Override
    public V put(K key, V value) {
        return this.wrapped.put(key, value);
    }

    @Override
    public V remove(Object key) {
        return this.wrapped.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        this.wrapped.putAll(m);
    }

    @Override
    public void clear() {
        this.wrapped.clear();
    }

    @Override
    public Set<K> keySet() {
        return this.wrapped.keySet();
    }

    @Override
    public Collection<V> values() {
        return this.wrapped.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return this.wrapped.entrySet();
    }
}