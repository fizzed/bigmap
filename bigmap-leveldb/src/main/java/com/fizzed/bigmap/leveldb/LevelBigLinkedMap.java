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

import static com.fizzed.bigmap.BigMapHelper.sizeOf;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.print.attribute.UnmodifiableSetException;
import org.iq80.leveldb.DBIterator;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicLong;
import org.iq80.leveldb.DBException;

public class LevelBigLinkedMap<K,V> implements Map<K,V> {

    static class InsertionOrder {

        private static final AtomicLong c = new AtomicLong(0);

        public static void increment() {
            c.getAndIncrement();
        }

        public static long value() {
            return c.get();
        }
    }
    
    final private LevelBigMap<K, V> wrappedMap;
    final private LevelBigMap<K, Long> keyToInsertionOrderMap;
    final private LevelBigMap<Long, K> insertionToOrderToKeyMap;

    LevelBigLinkedMap( Comparator<?> keyComparator ) {

        // LevelBigMap builder
        final LevelBigMap<K,V> wrappedMap = new LevelBigMapBuilder()
            .setScratchDirectory(Paths.get("target"))
            .setKeyType(Object.class, keyComparator)
            .setValueType(Object.class)
            .build();
        
        this.wrappedMap = wrappedMap;
        
        final LevelBigMap<K,Long> keyToInsertionOrderMap = new LevelBigMapBuilder()
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
        this.wrappedMap.checkIfClosed();
        
        byte[] keyBytes = this.wrappedMap.keyCodec.serialize((K)key);

        byte[] valueBytes = this.wrappedMap.db.get(keyBytes);

        return this.wrappedMap.valueCodec.deserialize(valueBytes);
    }

    @Override
    public V put(K arg0, V arg1) {
        if (this.wrappedMap.get(arg0) == null) {
            this.insertionToOrderToKeyMap.put(InsertionOrder.value(), arg0);
            this.keyToInsertionOrderMap.put(arg0, InsertionOrder.value());
            InsertionOrder.increment();
        }

        return this.wrappedMap.put(arg0, arg1);
    }

    @Override
    public V remove(Object key) {
        
        byte[] valueBytes = removeFromWrappedBigMap(key);
 
        removeFromSupportBigMap(key);

        return this.wrappedMap.valueCodec.deserialize(valueBytes);
    }

    private void removeFromSupportBigMap(Object key) throws DBException {
        this.keyToInsertionOrderMap.checkIfClosed();
        
        byte[] keyBytesKeyToInsertOrder = this.keyToInsertionOrderMap.keyCodec.serialize((K)key);

        byte[] valueBytesToInsertOrder = this.keyToInsertionOrderMap.db.get(keyBytesKeyToInsertOrder);
        
        Long insertionOrderNumber;
        insertionOrderNumber = this.keyToInsertionOrderMap.get(key);
        
        if (valueBytesToInsertOrder != null) {
            // remove the key, then deduct its info
            this.keyToInsertionOrderMap.db.delete(keyBytesKeyToInsertOrder);
            this.keyToInsertionOrderMap.size--;
            this.keyToInsertionOrderMap.keyByteSize -= sizeOf(keyBytesKeyToInsertOrder);
            this.keyToInsertionOrderMap.valueByteSize -= sizeOf(valueBytesToInsertOrder);
        }
        
        this.insertionToOrderToKeyMap.checkIfClosed();
        
        byte[] keyBytesInsertOrderKey = this.insertionToOrderToKeyMap.keyCodec.serialize(insertionOrderNumber);

        byte[] valueBytesInsertOrderKey = this.insertionToOrderToKeyMap.db.get(keyBytesInsertOrderKey);
        
        if (valueBytesInsertOrderKey != null) {
            // remove the key, then deduct its info
            this.insertionToOrderToKeyMap.db.delete(keyBytesInsertOrderKey);
            this.insertionToOrderToKeyMap.size--;
            this.insertionToOrderToKeyMap.keyByteSize -= sizeOf(keyBytesInsertOrderKey);
            this.insertionToOrderToKeyMap.valueByteSize -= sizeOf(valueBytesInsertOrderKey);
        }
    }

    private byte[] removeFromWrappedBigMap(Object key) throws DBException {
        this.wrappedMap.checkIfClosed();
        byte[] keyBytes = this.wrappedMap.keyCodec.serialize((K)key);
        byte[] valueBytes = this.wrappedMap.db.get(keyBytes);
        if (valueBytes != null) {
            // remove the key, then deduct its info
            this.wrappedMap.db.delete(keyBytes);
            this.wrappedMap.size--;
            this.wrappedMap.keyByteSize -= sizeOf(keyBytes);
            this.wrappedMap.valueByteSize -= sizeOf(valueBytes);
        }
        return valueBytes;
    }
    
    public K firstKey() {
        this.insertionToOrderToKeyMap.checkIfClosed();

        DBIterator itInsOrder = this.insertionToOrderToKeyMap.db.iterator();
        Entry<byte[], byte[]> firstEntry = itInsOrder.next();

        DBIterator it = this.wrappedMap.db.iterator();
        it.seek(firstEntry.getValue());
        Entry<byte[], byte[]> first = it.next();
        
        if (first != null) {
            return this.wrappedMap.keyCodec.deserialize(first.getKey());
        }
        return null;
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
        this.wrappedMap.checkIfClosed();
        
        return new LevelBigSet<>(this.wrappedMap);
    }

    @Override
    public Collection<V> values() {
        this.wrappedMap.checkIfClosed();
        
        return new ValueCollectionView();    
    }

    @Override
    public Set<Entry<K,V>> entrySet() {
        
        return new LevelBigLinkedMap.EntrySetView();
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
            return LevelBigLinkedMap.this.wrappedMap.keyCodec.deserialize(keyBytes);
        }

        @Override
        public V getValue() {
            return LevelBigLinkedMap.this.wrappedMap.valueCodec.deserialize(valueBytes);
        }

        @Override
        public V setValue(V value) {
            return LevelBigLinkedMap.this.wrappedMap.putValue(keyBytes, value);
        }
        
    }

    class EntrySetView implements Set<Entry<K,V>> {
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
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Iterator<Entry<K,V>> iterator() {
            
            final DBIterator itInsOrder = LevelBigLinkedMap.this.insertionToOrderToKeyMap.db.iterator();
            final DBIterator it = LevelBigLinkedMap.this.wrappedMap.db.iterator();

            return new Iterator<Entry<K, V>>() {
                @Override
                public boolean hasNext() {
                    return itInsOrder.hasNext();
                }

                @Override
                public Entry<K,V> next() {
                    Entry<byte[],byte[]> nextInsOrder = itInsOrder.next();
                   
                    it.seek(nextInsOrder.getValue());
                    
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
            final DBIterator itInsOrder = LevelBigLinkedMap.this.insertionToOrderToKeyMap.db.iterator();

            return new Iterator<V>() {
                @Override
                public boolean hasNext() {
                    return itInsOrder.hasNext();
                }

                @Override
                public V next() {
                    // NOTE: this throws a NoSuchElementException is no element exists
                    Entry<byte[], byte[]> nextInsOrder = itInsOrder.next();

                    final DBIterator it = LevelBigLinkedMap.this.wrappedMap.db.iterator();

                    it.seek(nextInsOrder.getValue());

                    Entry<byte[], byte[]> next = it.next();

                    if (next != null) {
                        return LevelBigLinkedMap.this.wrappedMap.valueCodec.deserialize(next.getValue());
                    }
                    return null;
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
            LevelBigLinkedMap.this.clear();
        }

        @Override
        public boolean contains(Object arg0) {
            throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }

    }

}