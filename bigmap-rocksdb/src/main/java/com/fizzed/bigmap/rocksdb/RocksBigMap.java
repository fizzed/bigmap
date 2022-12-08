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
package com.fizzed.bigmap.rocksdb;

import com.fizzed.bigmap.BigMapDataException;
import com.fizzed.bigmap.BigMapNonScalableException;
import com.fizzed.bigmap.ByteCodec;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;

import javax.print.attribute.UnmodifiableSetException;
import java.nio.file.Path;
import java.util.*;

import static com.fizzed.bigmap.BigMapHelper.sizeOf;

public class RocksBigMap<K,V> extends AbstractRocksBigCollection<K> implements SortedMap<K,V> {

    protected final ByteCodec<V> valueCodec;
    
    protected RocksBigMap(
//            boolean persistent,
//            boolean counts,
            Path directory,
//            long cacheSize,
            ByteCodec<K> keyCodec,
            Comparator<K> keyComparator,
            ByteCodec<V> valueCodec) {
        
        super(directory, keyCodec, keyComparator);
        
        Objects.requireNonNull(valueCodec, "valueCodec was null");
        
        this.valueCodec = valueCodec;
    }
    
    public long getValueByteSize() {
        return this.valueByteSize;
    }
    
    @Override
    public boolean containsKey(Object key) {
        return super.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        this.checkIfClosed();
        
        throw new BigMapNonScalableException("Poor performance for checking if map contains a value. Method unsupported.");
    }

    @Override
    public V get(Object key) {
        this.checkIfClosed();
        
        byte[] keyBytes = this.keyCodec.serialize((K)key);

        try {
            byte[] valueBytes = this.db.get(keyBytes);

            return this.valueCodec.deserialize(valueBytes);
        }
        catch (RocksDBException e) {
            throw new BigMapDataException(e);
        }
    }

    @Override
    public V put(K key, V value) {
        this.checkIfClosed();
        
        byte[] keyBytes = this.keyCodec.serialize(key);
        
        return this.putValue(keyBytes, value);
    }

    protected V putKey(K key, byte[] valueBytes) {
        byte[] keyBytes = this.keyCodec.serialize(key);
        
        byte[] oldValueBytes = this.putBytes(keyBytes, valueBytes);
        
        return this.valueCodec.deserialize(oldValueBytes);
    }

    protected V putValue(byte[] keyBytes, V value) {
        byte[] valueBytes = this.valueCodec.serialize(value);
       
        byte[] oldValueBytes = this.putBytes(keyBytes, valueBytes);
        
        return this.valueCodec.deserialize(oldValueBytes);
    }

    @Override
    public V remove(Object key) {
        this.checkIfClosed();

        byte[] keyBytes = this.keyCodec.serialize((K)key);

        try {
            byte[] valueBytes = this.db.get(keyBytes);

            if (valueBytes != null) {
                // remove the key, then deduct its info
                this.db.delete(keyBytes);
                this.size--;
                this.keyByteSize -= sizeOf(keyBytes);
                this.valueByteSize -= sizeOf(valueBytes);
            }

            return this.valueCodec.deserialize(valueBytes);
        }
        catch (RocksDBException e) {
            throw new BigMapDataException(e);
        }
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
        return this.keyComparator;
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
        return super.firstKey();
    }

    @Override
    public K lastKey() {
        throw new UnsupportedOperationException();
        /*this.checkIfClosed();

        DBIterator it = this.db.iterator();
        it.seekToLast();
        Entry<byte[],byte[]> lastEntry = it.prev();
        if (lastEntry != null) {
            return this.keyCodec.deserialize(lastEntry.getKey());
        }
        return null;*/
    }

    @Override
    public Set<K> keySet() {
        this.checkIfClosed();
        
        //return new LevelBigSet<>(this);
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<V> values() {
        this.checkIfClosed();
        
        return new ValueCollectionView();
    }

    @Override
    public Set<Entry<K,V>> entrySet() {
        this.checkIfClosed();
        
//        return new EntrySetView();
        throw new UnsupportedOperationException();
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
            return RocksBigMap.this.keyCodec.deserialize(keyBytes);
        }

        @Override
        public V getValue() {
            return RocksBigMap.this.valueCodec.deserialize(valueBytes);
        }

        @Override
        public V setValue(V value) {
            return RocksBigMap.this.putValue(keyBytes, value);
        }
        
    }
    
    class ValueCollectionView implements Collection<V> {

        @Override
        public int size() {
            return RocksBigMap.this.size();
        }

        @Override
        public boolean isEmpty() {
            return RocksBigMap.this.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            throw new BigMapNonScalableException("Checking if a value exists is a bad performance decision");
        }

        @Override
        public Iterator<V> iterator() {
            final RocksIterator _it = RocksBigMap.this.db.newIterator();
            _it.seekToFirst();
            final RocksForwardIterator it = new RocksForwardIterator(_it);
            return new Iterator<V>() {
                @Override
                public boolean hasNext() {
                    return it.hasNext();
                }

                @Override
                public V next() {
                    final RocksKeyValue kv = it.next();

                    return RocksBigMap.this.valueCodec.deserialize(kv.getValue());
                }
            };
        }

        @Override
        public Object[] toArray() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public <T> T[] toArray(T[] a) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean add(V e) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean addAll(Collection<? extends V> c) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void clear() {
            RocksBigMap.this.clear();
        }
        
    }
    
    /*class EntrySetView implements Set<Entry<K,V>> {
        @Override
        public int size() {
            return RocksBigMap.this.size();
        }

        @Override
        public boolean isEmpty() {
            return RocksBigMap.this.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Iterator<Entry<K,V>> iterator() {
            final DBIterator it = RocksBigMap.this.db.iterator();
            it.seekToFirst();

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
            RocksBigMap.this.clear();
        }
                
    }*/
    
}