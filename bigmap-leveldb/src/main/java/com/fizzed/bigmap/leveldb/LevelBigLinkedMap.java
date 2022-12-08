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

import com.fizzed.bigmap.ByteCodec;
import com.fizzed.bigmap.ByteCodecs;
import com.fizzed.bigmap.Comparators;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import javax.print.attribute.UnmodifiableSetException;

public class LevelBigLinkedMap<K,V> implements SortedMap<K,V>, Closeable {

    final private LevelBigMap<K,V> dataMap;
    final private LevelBigMap<Integer,K> insertOrderToKeyMap;  // for iterating in the order inserted
    final private LevelBigMap<K,Integer> keyToInsertOrderMap;  // for deleting from insertOrder map?   should we care?
    final private AtomicInteger insertCounter = new AtomicInteger(0);
  
    LevelBigLinkedMap(
            boolean persistent,
            boolean counts,
            Path dataDir,
            Path i2kDir,
            Path k2iDir,
            long cacheSize,
            ByteCodec<K> keyCodec,
            Comparator<K> keyComparator,
            ByteCodec<V> valueCodec) {

        // 80% of cache goes to the data map
        this.dataMap = new LevelBigMap(persistent, counts, dataDir, (long)(cacheSize*0.8d), keyCodec, keyComparator, valueCodec);

        // 10%+10% goes to each of the insert order maps
        this.insertOrderToKeyMap = new LevelBigMap(persistent, counts, i2kDir, (long)(cacheSize*0.1d), ByteCodecs.integerCodec(), Comparators.autoComparator(Integer.class), keyCodec);

        this.keyToInsertOrderMap = new LevelBigMap(persistent, counts, k2iDir, (long)(cacheSize*0.1d), keyCodec, keyComparator, ByteCodecs.integerCodec());
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
    public Comparator<? super K> comparator() {
        return this.dataMap.comparator();
    }

    @Override
    public SortedMap<K, V> subMap(K fromKey, K toKey) {
        return this.dataMap.subMap(fromKey, toKey);
    }

    @Override
    public SortedMap<K, V> headMap(K toKey) {
        return this.dataMap.headMap(toKey);
    }

    @Override
    public SortedMap<K, V> tailMap(K fromKey) {
        return this.dataMap.tailMap(fromKey);
    }

    public K firstKey() {
        final Integer insertOrder = this.insertOrderToKeyMap.firstKey();
        if (insertOrder != null) {
            return this.insertOrderToKeyMap.get(insertOrder);
        }
        return null;
    }

    @Override
    public K lastKey() {
        final Integer insertOrder = this.insertOrderToKeyMap.lastKey();
        if (insertOrder != null) {
            return this.insertOrderToKeyMap.get(insertOrder);
        }
        return null;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        if (m != null) {
            m.forEach((k, v) -> {
                this.put(k, v);
            });
        }
    }

    @Override
    public void clear() {
        this.dataMap.clear();
        this.keyToInsertOrderMap.clear();
        this.insertOrderToKeyMap.clear();
        this.insertCounter.set(0);
    }

    @Override
    public Set<K> keySet() {
        return new KeySetView();
    }

    @Override
    public Collection<V> values() {
        return new ValueCollectionView();
    }

    @Override
    public Set<Entry<K,V>> entrySet() {
        return new EntrySetView();
    }

    @Override
    public void close() throws IOException {
        this.dataMap.close();
        this.insertOrderToKeyMap.close();
        this.keyToInsertOrderMap.close();
    }

    class KeySetView implements Set<K> {

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
            return LevelBigLinkedMap.this.containsKey(o);
        }

        @Override
        public Iterator<K> iterator() {
            final Iterator<Entry<Integer,K>> it = LevelBigLinkedMap.this.insertOrderToKeyMap.entrySet().iterator();
            return new Iterator<K>() {
                @Override
                public boolean hasNext() {
                    return it.hasNext();
                }

                @Override
                public K next() {
                    Entry<Integer,K> entry = it.next();
                    if (entry != null) {
                        return entry.getValue();
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
        public boolean add(K e) {
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
        public boolean addAll(Collection<? extends K> c) {
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
            final Iterator<Entry<Integer,K>> it = LevelBigLinkedMap.this.insertOrderToKeyMap.entrySet().iterator();

            return new Iterator<Entry<K,V>>() {
                @Override
                public boolean hasNext() {
                    return it.hasNext();
                }

                @Override
                public Entry<K,V> next() {
                    Entry<Integer,K> entry = it.next();
                    if (entry != null) {
                        final K key = entry.getValue();
                        final V value = LevelBigLinkedMap.this.dataMap.get(key);
                        return new Entry<K,V>() {
                            @Override
                            public K getKey() {
                                return key;
                            }

                            @Override
                            public V getValue() {
                                return value;
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
            return LevelBigLinkedMap.this.dataMap.size();
        }

        @Override
        public boolean isEmpty() {
            return LevelBigLinkedMap.this.isEmpty();
        }

        @Override
        public Iterator<V> iterator() {
            final Iterator<Entry<Integer,K>> it = LevelBigLinkedMap.this.insertOrderToKeyMap.entrySet().iterator();

            return new Iterator<V>() {
                @Override
                public boolean hasNext() {
                    return it.hasNext();
                }

                @Override
                public V next() {
                    final Entry<Integer,K> entry = it.next();
                    if (entry != null) {
                        final K key = entry.getValue();
                        return LevelBigLinkedMap.this.dataMap.get(key);
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
