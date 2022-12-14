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

import com.fizzed.bigmap.*;
import com.fizzed.bigmap.impl.AbstractBigMap;
import com.fizzed.bigmap.impl.ByteArrayBigMap;
import com.fizzed.bigmap.impl.KeyValueBytes;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBException;
import org.iq80.leveldb.Options;
import static org.iq80.leveldb.impl.Iq80DBFactory.factory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;

public class LevelBigMap<K,V> extends AbstractBigMap<K,V> implements ByteArrayBigMap<K,V>, BigSortedMap<K,V> {

    protected Options options;
    protected DB db;

    protected LevelBigMap(
            Path directory,
            ByteCodec<K> keyCodec,
            Comparator<K> keyComparator,
            ByteCodec<V> valueCodec) {
        
        super(directory, false, keyCodec, keyComparator, valueCodec);
        
        Objects.requireNonNull(valueCodec, "valueCodec was null");
    }

    @Override
    protected void _open() {
        this.options = new Options();
        //this.options.compressionType(CompressionType.NONE);
        this.options.createIfMissing(true);
        //this.options.comparator(new LevelJavaComparator(this.keyCodec, this.keyComparator));
        //this.options.cacheSize(this.cacheSize);

        try {
            // build database, initialize stats we track
            this.db = factory.open(this.directory.toFile(), this.options);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void _close() throws IOException {
        if (this.db != null) {
            this.db.close();
        }
    }

    @Override
    public boolean isClosed() {
        return this.db == null;
    }

    @Override
    public byte[] _get(byte[] keyBytes) {
        try {
            return this.db.get(keyBytes);
        }
        catch (DBException e) {
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
        catch (DBException e) {
            throw new BigMapDataException(e);
        }
    }

    @Override
    public boolean _containsKey(byte[] keyBytes) {
        try {
            return this.db.get(keyBytes) != null;
        }
        catch (DBException e) {
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
        catch (DBException e) {
            throw new BigMapDataException(e);
        }
    }

    @Override
    public Iterator<KeyValueBytes> _forwardIterator() {
        return LevelForwardIterator.build(this.db);
    }

}