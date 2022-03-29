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
package com.fizzed.bigmap.leveldb;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicLong;
import javax.print.attribute.UnmodifiableSetException;

public class LevelBigLinkedMap<K, V> implements Map<K, V> {

    final private LevelBigMap<K, V> wrappedMap;
    final private LevelBigMap<K, Long> keyToInsertionOrderMap;
    final private LevelBigMap<Long, K> insertionToOrderToKeyMap;

    private AtomicLong c = new AtomicLong(0);

    LevelBigLinkedMap(Comparator<?> keyComparator) {

        // LevelBigMap builder
        final LevelBigMap<K, V> wrappedMap = new LevelBigMapBuilder()
                .setScratchDirectory(Paths.get("target"))
                .setKeyType(Object.class, keyComparator)
                .setValueType(Object.class)
                .build();

        this.wrappedMap = wrappedMap;

        final LevelBigMap<K, Long> keyToInsertionOrderMap = new LevelBigMapBuilder()
                .setScratchDirectory(Paths.get("targetK2Order"))
                .setKeyType(Object.class, keyComparator)
                .setValueType(Long.class)
                .build();

        this.keyToInsertionOrderMap = keyToInsertionOrderMap;

        final LevelBigMap<Long, K> insertionToOrderToKeyMap = new LevelBigMapBuilder()
                .setScratchDirectory(Paths.get("targetOrder2K"))
                .setKeyType(Long.class, keyComparator)
                .setValueType(Object.class)
                .build();

        this.insertionToOrderToKeyMap = insertionToOrderToKeyMap;
    }

    @Override
    public int size() {
        return this.wrappedMap.size();
    }

    @Override
    public boolean isEmpty() {
        return LevelBigLinkedMap.this.wrappedMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean containsValue(Object arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public V get(Object key) {
        return this.wrappedMap.get(key);
    }

    @Override
    public V put(K arg0, V arg1) {
        if (this.wrappedMap.get(arg0) == null) {
            long index = c.getAndIncrement();
            this.insertionToOrderToKeyMap.put(index, arg0);
            this.keyToInsertionOrderMap.put(arg0, index);
        }

        return this.wrappedMap.put(arg0, arg1);
    }

    @Override
    public V remove(Object key) {
        Long insertionOrder = this.keyToInsertionOrderMap.remove(key);
        this.insertionToOrderToKeyMap.remove(insertionOrder);
        return this.wrappedMap.remove(key);

    }

    public K firstKey() {
        Long firstInsertionOrder = this.insertionToOrderToKeyMap.firstKey();
        return this.insertionToOrderToKeyMap.get(firstInsertionOrder);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void clear() {
        this.wrappedMap.clear();
        this.keyToInsertionOrderMap.clear();
        this.insertionToOrderToKeyMap.clear();
    }

    @Override
    public Set<K> keySet() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Collection<V> values() {
        this.wrappedMap.checkIfClosed();

        return new ValueCollectionView();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {

        return new LevelBigLinkedMap.EntrySetView();
    }

    class EntrySetView implements Set<Entry<K, V>> {

        @Override
        public int size() {
            return LevelBigLinkedMap.this.size();
        }

        @Override
        public boolean isEmpty() {
            return LevelBigLinkedMap.this.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Iterator<Entry<K, V>> iterator() {

            final Iterator itInsOrder = LevelBigLinkedMap.this.insertionToOrderToKeyMap.entrySet().iterator();

            return new Iterator<Entry<K, V>>() {
                @Override
                public boolean hasNext() {
                    return itInsOrder.hasNext();
                }

                @Override
                public Entry<K, V> next() {
                    Entry<K, V> nextInsOrder = (Entry<K, V>) itInsOrder.next();
                    if (nextInsOrder != null) {

                        return new Entry<K, V>() {
                            @Override
                            public K getKey() {
                                return (K) nextInsOrder.getValue();
                            }

                            @Override
                            public V getValue() {
                                return (V) wrappedMap.get((K) nextInsOrder.getValue());
                            }

                            @Override
                            public V setValue(V value) {
                                throw new UnsupportedOperationException("Not supported yet.");
                            }
                        };
                    }
                    return null;
                }
            };
        }

        @Override
        public Object[] toArray() {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> T[] toArray(T[] a) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean add(Entry<K, V> e) {
            throw new UnmodifiableSetException();
        }

        @Override
        public boolean remove(Object o) {
            throw new UnmodifiableSetException();
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean addAll(Collection<? extends Entry<K, V>> c) {
            throw new UnmodifiableSetException();
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            throw new UnmodifiableSetException();
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            throw new UnmodifiableSetException();
        }

        @Override
        public void clear() {
            LevelBigLinkedMap.this.clear();
        }

    }

    class ValueCollectionView implements Collection<V> {

        @Override
        public int size() {
            return LevelBigLinkedMap.this.wrappedMap.size();
        }

        @Override
        public boolean isEmpty() {
            return LevelBigLinkedMap.this.isEmpty();
        }

        @Override
        public Iterator<V> iterator() {

            final Iterator itInsOrder = LevelBigLinkedMap.this.insertionToOrderToKeyMap.entrySet().iterator();

            return new Iterator<V>() {
                @Override
                public boolean hasNext() {
                    return itInsOrder.hasNext();
                }

                @Override
                public V next() {

                    Entry<K, V> nextInsOrder = (Entry<K, V>) itInsOrder.next();

                    if (nextInsOrder != null) {

                        K key = (K) nextInsOrder.getValue();

                        return LevelBigLinkedMap.this.wrappedMap.get(key);

                    }
                    return null;
                }
            };
        }

        @Override
        public Object[] toArray() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public <T> T[] toArray(T[] a) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean add(V e) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean addAll(Collection<? extends V> c) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void clear() {
            LevelBigLinkedMap.this.clear();
        }

        @Override
        public boolean contains(Object arg0) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }

}
