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
import com.fizzed.bigmap.impl.AbstractBigMap;
import com.fizzed.bigmap.impl.ByteArrayBigMap;
import com.fizzed.bigmap.impl.KeyValueBytes;
import org.rocksdb.BuiltinComparator;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import java.nio.file.Path;
import java.util.*;

public class RocksBigMap<K,V> extends AbstractBigMap<K,V> implements ByteArrayBigMap<K,V>, BigSortedMap<K,V> {

    protected Options options;
    protected RocksDB db;

    protected RocksBigMap(
            UUID id,
            Path directory,
            ByteCodec<K> keyCodec,
            Comparator<K> keyComparator,
            ByteCodec<V> valueCodec) {
        
        super(id, directory, false, keyCodec, keyComparator, valueCodec);
        
        Objects.requireNonNull(valueCodec, "valueCodec was null");
    }

    @Override
    protected void _open() {
        this.options = new Options();
        //this.options.compressionType(CompressionType.NONE);
        this.options.setCreateIfMissing(true);
        this.options.setComparator(BuiltinComparator.BYTEWISE_COMPARATOR);
        // wow, this comparator causes a massive memory leak
        //this.options.setComparator(new RocksJavaComparator(this.keyCodec, this.keyComparator));
        this.options.setDisableAutoCompactions(true);

        try {
            // build database, initialize stats we track
            this.db = RocksDB.open(this.options, this.directory.toAbsolutePath().toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        this.closer = new RocksBigObjectCloser(this.id, this.persistent, this.directory, this.db);
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

}