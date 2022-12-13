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
package com.fizzed.bigmap;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

abstract public class AbstractBigLinkedMap<K,V> implements BigMap<K,V> {

    final private Path directory;
    final private BigMap<K,V> dataMap;
    final private BigSortedMap<Integer,K> insertOrderToKeyMap;  // for iterating in the order inserted
    final private BigMap<K,Integer> keyToInsertOrderMap;        // for deleting from insertOrder map?   should we care?
    final private AtomicInteger insertCounter = new AtomicInteger(0);

    public AbstractBigLinkedMap(
            Path directory,
            BigMap<K,V> dataMap,
            BigSortedMap<Integer,K> insertOrderToKeyMap,
            BigMap<K,Integer> keyToInsertOrderMap) {

        this.directory = directory;
        this.dataMap = dataMap;
        this.insertOrderToKeyMap = insertOrderToKeyMap;
        this.keyToInsertOrderMap = keyToInsertOrderMap;
    }

    @Override
    public void close() throws IOException {
        this.dataMap.close();
        this.insertOrderToKeyMap.close();
        this.keyToInsertOrderMap.close();
    }

    @Override
    public Path getDirectory() {
        return this.directory;
    }

    @Override
    public long getKeyByteSize() {
        return this.dataMap.getKeyByteSize();
    }

    @Override
    public long getValueByteSize() {
        return this.dataMap.getValueByteSize();
    }

    @Override
    public boolean isClosed() {
        return this.dataMap.isClosed();
    }

    @Override
    public void checkIfClosed() {
        this.dataMap.checkIfClosed();
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