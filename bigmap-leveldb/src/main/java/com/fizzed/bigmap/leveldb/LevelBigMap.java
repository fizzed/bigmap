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

import com.fizzed.bigmap.BigMapCodec;
import static com.fizzed.bigmap.BigMapHelper.sizeOf;
import com.fizzed.bigmap.BigMapNonScalableException;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import javax.print.attribute.UnmodifiableSetException;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBIterator;

public class LevelBigMap<K,V> implements SortedMap<K,V> {

    private final DB db;
    private final BigMapCodec<K> keyCodec;
    private final BigMapCodec<V> valueCodec;
    private int size;
    // these are accumulating values...
    private long keyByteSize;
    private long valueByteSize;

    public LevelBigMap(DB db, BigMapCodec<K> keyCodec, BigMapCodec<V> valueCodec) {
        Objects.requireNonNull(db, "db was null");
        Objects.requireNonNull(keyCodec, "keyCodec was null");
        Objects.requireNonNull(valueCodec, "valueCodec was null");
        this.db = db;
        this.keyCodec = keyCodec;
        this.valueCodec = valueCodec;
    }

    public long getKeyByteSize() {
        return this.keyByteSize;
    }

    public long getValueByteSize() {
        return this.valueByteSize;
    }
    
    @Override
    public int size() {
        return this.size;
    }

    @Override
    public boolean isEmpty() {
        return this.size <= 0;
    }

    @Override
    public void clear() {
        // TODO: implement clear of db...
    }
    
    @Override
    public boolean containsKey(Object key) {
        byte[] keyBytes = this.keyCodec.serialize((K)key);

        byte[] valueBytes = this.db.get(keyBytes);
        
        // skip deserialization!
        return valueBytes != null;
    }

    @Override
    public boolean containsValue(Object value) {
        throw new BigMapNonScalableException("Poor performance for checking if map contains a value. Method unsupported.");
    }

    @Override
    public V get(Object key) {
        byte[] keyBytes = this.keyCodec.serialize((K)key);

        byte[] valueBytes = this.db.get(keyBytes);

        return this.valueCodec.deserialize(valueBytes);
    }

    @Override
    public V put(K key, V value) {
        byte[] keyBytes = this.keyCodec.serialize(key);
        
        return this.put(keyBytes, value);
    }

    private V put(byte[] keyBytes, V value) {
        byte[] valueBytes = this.valueCodec.serialize(value);
        
        // get current value
        byte[] oldValueBytes = this.db.get(keyBytes);

        // put the new value
        this.db.put(keyBytes, valueBytes);

        // new key?
        if (oldValueBytes != null) {
            this.keyByteSize += sizeOf(keyBytes);
            this.valueByteSize -= sizeOf(oldValueBytes);
        }
        else {
            this.size++;
        }

        this.valueByteSize += sizeOf(valueBytes);

        return this.valueCodec.deserialize(oldValueBytes);
    }

    @Override
    public V remove(Object key) {
        byte[] keyBytes = this.keyCodec.serialize((K)key);

        byte[] valueBytes = this.db.get(keyBytes);
        
        if (valueBytes != null) {
            this.db.delete(keyBytes);
            
            this.size--;
        }

        return this.valueCodec.deserialize(valueBytes);
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
    public Comparator<? super K> comparator() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public SortedMap<K, V> subMap(K fromKey, K toKey) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public SortedMap<K, V> headMap(K toKey) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public SortedMap<K, V> tailMap(K fromKey) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public K firstKey() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public K lastKey() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Set<K> keySet() {
        return new KeySetView();
    }

    @Override
    public Collection<V> values() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Set<Entry<K,V>> entrySet() {
        return new EntrySetView();
    }
    
    class EntryView implements Entry<K,V> {

        private final byte[] keyBytes;
        private final byte[] valueBytes;

        public EntryView(byte[] keyBytes, byte[] valueBytes) {
            this.keyBytes = keyBytes;
            this.valueBytes = valueBytes;
        }
        
        @Override
        public K getKey() {
            return LevelBigMap.this.keyCodec.deserialize(keyBytes);
        }

        @Override
        public V getValue() {
            return LevelBigMap.this.valueCodec.deserialize(valueBytes);
        }

        @Override
        public V setValue(V value) {
            return LevelBigMap.this.put(keyBytes, value);
        }
        
    }
    
    class KeySetView implements Set<K> {
        @Override
        public int size() {
            return LevelBigMap.this.size();
        }

        @Override
        public boolean isEmpty() {
            return LevelBigMap.this.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return LevelBigMap.this.containsKey(o);
        }

        @Override
        public Iterator<K> iterator() {
            final DBIterator it = LevelBigMap.this.db.iterator();
            return new Iterator<K>() {
                @Override
                public boolean hasNext() {
                    return it.hasNext();
                }

                @Override
                public K next() {
                    Entry<byte[],byte[]> next = it.next();
                    if (next != null) {
                        return LevelBigMap.this.keyCodec.deserialize(next.getKey());
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
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
            throw new UnmodifiableSetException();
        }       
    }
    
    class EntrySetView implements Set<Entry<K,V>> {
        @Override
        public int size() {
            return LevelBigMap.this.size();
        }

        @Override
        public boolean isEmpty() {
            return LevelBigMap.this.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return LevelBigMap.this.containsKey(o);
        }

        @Override
        public Iterator<Entry<K,V>> iterator() {
            final DBIterator it = LevelBigMap.this.db.iterator();

            return new Iterator<Entry<K, V>>() {
                @Override
                public boolean hasNext() {
                    return it.hasNext();
                }

                @Override
                public Entry<K,V> next() {
                    Entry<byte[],byte[]> next = it.next();
                    if (next != null) {
                        return new EntryView(next.getKey(), next.getValue());
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
        public boolean add(Entry<K,V> e) {
            throw new UnmodifiableSetException();
        }

        @Override
        public boolean remove(Object o) {
            throw new UnmodifiableSetException();
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean addAll(Collection<? extends Entry<K,V>> c) {
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
            throw new UnmodifiableSetException();
        }
                
    }
    
}