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

import com.fizzed.bigmap.*;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;

import java.nio.file.Path;
import java.util.*;

public class RocksBigMap<K,V> extends AbstractRocksBigCollection<K> implements BigSortedMap<K,V> {

    protected final ByteCodec<V> valueCodec;
    
    protected RocksBigMap(
            Path directory,
            ByteCodec<K> keyCodec,
            Comparator<K> keyComparator,
            ByteCodec<V> valueCodec) {
        
        super(directory, keyCodec, keyComparator);
        
        Objects.requireNonNull(valueCodec, "valueCodec was null");
        
        this.valueCodec = valueCodec;
    }

    @Override
    public ByteCodec<V> getValueCodec() {
        return valueCodec;
    }

    public long getValueByteSize() {
        return this.valueByteSize;
    }

    @Override
    public byte[] _get(byte[] keyBytes) {
        try {
            return this.db.get(keyBytes);
        }
        catch (RocksDBException e) {
            throw new BigMapDataException(e);
        }
    }

    @Override
    public byte[] _put(byte[] keyBytes, byte[] valueBytes) {
        try {
            byte[] oldValueBytes = this.db.get(keyBytes);

            this.db.put(keyBytes, valueBytes);

            return oldValueBytes;
        }
        catch (RocksDBException e) {
            throw new BigMapDataException(e);
        }
    }

    @Override
    public boolean _containsKey(byte[] keyBytes) {
        try {
            return this.db.get(keyBytes) != null;
        }
        catch (RocksDBException e) {
            throw new BigMapDataException(e);
        }
    }

    @Override
    public byte[] _remove(byte[] keyBytes) {
        byte[] valueBytes = this._get(keyBytes);

        try {
            this.db.delete(keyBytes);

            return valueBytes;
        }
        catch (RocksDBException e) {
            throw new BigMapDataException(e);
        }
    }

    @Override
    public Iterator<KeyValueBytes> _forwardIterator() {
        return RocksForwardIterator.build(this.db);
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
    }

}