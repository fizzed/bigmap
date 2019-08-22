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
import com.fizzed.bigmap.ByteCodec;
import com.fizzed.bigmap.ByteCodecs;
import static com.fizzed.bigmap.ByteCodecs.byteArrayCodec;
import java.nio.file.Path;
import java.util.Map;
import java.util.SortedSet;
import org.iq80.leveldb.DBIterator;

public class LevelBigSet<K> implements SortedSet<K> {

    protected final LevelBigMap<K,?> map;
    
    protected LevelBigSet(
            Path directory,
            long cacheSize,
            ByteCodec<K> keyCodec,
            Comparator<K> keyComparator) {
        
        this(new LevelBigMap<K,byte[]>(
            directory, cacheSize, keyCodec, keyComparator, byteArrayCodec()));
    }
    
    protected LevelBigSet(
            LevelBigMap<K,?> map) {
        
        this.map = map;
    }

    public long getKeyByteSize() {
        return this.map.getKeyByteSize();
    }
    
    @Override
    public Comparator<? super K> comparator() {
        return this.map.comparator();
    }

    @Override
    public K first() {
        return this.map.firstKey();
    }

    @Override
    public K last() {
        return this.map.lastKey();
    }

    @Override
    public int size() {
        return this.map.size();
    }

    @Override
    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return this.map.containsKey((K)o);
    }

    @Override
    public Iterator<K> iterator() {
        final DBIterator it = this.map.db.iterator();
        return new Iterator<K>() {
            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public K next() {
                // NOTE: this throws a NoSuchElementException is no element exists
                Map.Entry<byte[],byte[]> next = it.next();
                if (next != null) {
                    return LevelBigSet.this.map.keyCodec.deserialize(next.getKey());
                }
                return null;
            }
        };
    }

    @Override
    public boolean add(K key) {
        return this.map.putKey(key, ByteCodecs.ZERO_BYTES) == null;
    }

    @Override
    public boolean remove(Object o) {
        return this.map.remove((K)o) != null;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        if (c != null) {
            for (Object k : c) {
                if (!this.contains((K)k)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends K> c) {
        boolean modified = false;
        
        if (c != null) {
            for (K k : c) {
                boolean m = this.add(k);
                if (m) {
                    modified = true;
                }
            }
        }
        
        return modified;
    }
        
    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean modified = false;
        
        if (c != null) {
            for (Object k : c) {
                boolean m = this.remove((K)k);
                if (m) {
                    modified = true;
                }
            }
        }
        
        return modified;
    }

    @Override
    public void clear() {
        this.map.clear();
    }
    
    @Override
    public SortedSet<K> subSet(K fromElement, K toElement) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public SortedSet<K> headSet(K toElement) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public SortedSet<K> tailSet(K fromElement) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public Object[] toArray() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <T> T[] toArray(T[] a) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}