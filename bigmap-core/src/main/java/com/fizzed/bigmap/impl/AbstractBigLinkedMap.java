/*
 * Copyright 2019 Fizzed, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.fizzed.bigmap.impl;

import com.fizzed.bigmap.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

abstract public class AbstractBigLinkedMap<K,V> implements BigMap<K,V> {

    protected final UUID id;
    protected final Path directory;
    protected final boolean persistent;
    protected final BigMap<K,V> dataMap;
    protected final BigSortedMap<Integer,K> insertOrderToKeyMap;  // for iterating in the order inserted
    protected final BigMap<K,Integer> keyToInsertOrderMap;        // for deleting from insertOrder map?   should we care?
    protected final AtomicInteger insertCounter = new AtomicInteger(0);
    protected BigObjectListener listener;
    protected BigObjectCloser closer;

    public AbstractBigLinkedMap(
            UUID id,
            Path directory,
            boolean persistent,
            BigMap<K,V> dataMap,
            BigSortedMap<Integer,K> insertOrderToKeyMap,
            BigMap<K,Integer> keyToInsertOrderMap) {

        this.id = id;
        this.directory = directory;
        this.persistent = persistent;
        this.dataMap = dataMap;
        this.insertOrderToKeyMap = insertOrderToKeyMap;
        this.keyToInsertOrderMap = keyToInsertOrderMap;
    }

    @Override
    public BigObjectListener getListener() {
        return this.listener;
    }

    @Override
    public void setListener(BigObjectListener listener) {
        this.listener = listener;
    }

    @Override
    public UUID getId() {
        return this.id;
    }

    @Override
    public Path getDirectory() {
        return this.directory;
    }

    @Override
    public boolean isPersistent() {
        return this.persistent;
    }

    @Override
    public void open() {
        this.dataMap.open();
        this.insertOrderToKeyMap.open();
        this.keyToInsertOrderMap.open();
        this.closer = new BigLinkedMapCloser(this.id, this.persistent, this.directory, this.dataMap, this.insertOrderToKeyMap, this.keyToInsertOrderMap);

        if (this.listener != null) {
            this.listener.onOpened(this);
        }
    }

    @Override
    final public void close() throws IOException {
        if (this.closer != null) {
            this.closer.close();
        }

        if (this.listener != null) {
            this.listener.onClosed(this);
        }
    }

    @Override
    final public BigObjectCloser getCloser() {
        return this.closer;
    }

    @Override
    final public boolean isClosed() {
        return this.closer == null || this.closer.isClosed();
    }

    @Override
    public long getKeyByteSize() {
        return this.dataMap.getKeyByteSize() + this.insertOrderToKeyMap.getKeyByteSize() + this.keyToInsertOrderMap.getKeyByteSize();
    }

    @Override
    public long getValueByteSize() {
        return this.dataMap.getValueByteSize() + this.insertOrderToKeyMap.getValueByteSize() + this.keyToInsertOrderMap.getValueByteSize();
    }

    @Override
    public ByteCodec<K> getKeyCodec() {
        return this.dataMap.getKeyCodec();
    }

    @Override
    public Comparator<K> getKeyComparator() {
        return this.dataMap.getKeyComparator();
    }

    @Override
    public ByteCodec<V> getValueCodec() {
        return this.dataMap.getValueCodec();
    }

    @Override
    public int size() {
        return this.dataMap.size();
    }

    @Override
    public boolean isEmpty() {
        return this.dataMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return this.dataMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return this.dataMap.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return this.dataMap.get(key);
    }

    @Override
    public V put(K key, V value) {
        // if the map is missing this key, then we want to track its insertion order
        if (!this.dataMap.containsKey(key)) {
            Integer insertOrder = this.insertCounter.incrementAndGet();
            this.insertOrderToKeyMap.put(insertOrder, key);
            this.keyToInsertOrderMap.put(key, insertOrder);
        }
        return this.dataMap.put(key, value);
    }

    @Override
    public V remove(Object key) {
        final Integer insertOrder = this.keyToInsertOrderMap.remove(key);
        if (insertOrder != null) {
            this.insertOrderToKeyMap.remove(insertOrder);
            return this.dataMap.remove(key);
        }
        return null;
    }

    @Override
    public void clear() {
        this.dataMap.clear();
        this.keyToInsertOrderMap.clear();
        this.insertOrderToKeyMap.clear();
        this.insertCounter.set(0);
    }

    @Override
    public Iterator<Entry<K,V>> forwardIterator() {
        final Iterator<Entry<Integer,K>> iterator = this.insertOrderToKeyMap.entrySet().iterator();
        return new Iterator<Entry<K,V>>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public Entry<K,V> next() {
                Entry<Integer,K> entry = iterator.next();
                if (entry != null) {
                    return new Entry<K,V>() {
                        @Override
                        public K getKey() {
                            return entry.getValue();
                        }

                        @Override
                        public V getValue() {
                            return dataMap.get(entry.getValue());
                        }

                        @Override
                        public V setValue(V value) {
                            return AbstractBigLinkedMap.this.put(entry.getValue(), value);
                        }
                    };
                }
                return null;
            }
        };
    }
}