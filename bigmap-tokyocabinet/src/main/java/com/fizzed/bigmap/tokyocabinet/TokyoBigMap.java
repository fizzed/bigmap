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
package com.fizzed.bigmap.tokyocabinet;

import com.fizzed.bigmap.*;
import com.fizzed.bigmap.impl.AbstractBigMap;
import com.fizzed.bigmap.impl.ByteArrayBigMap;
import com.fizzed.bigmap.impl.KeyValueBytes;
import tokyocabinet.BDB;
import tokyocabinet.HDB;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;
import java.util.UUID;

public class TokyoBigMap<K,V> extends AbstractBigMap<K,V> implements ByteArrayBigMap<K,V>, BigSortedMap<K,V> {

    protected BDB db;

    protected TokyoBigMap(
            UUID id,
            Path file,
            ByteCodec<K> keyCodec,
            Comparator<K> keyComparator,
            ByteCodec<V> valueCodec) {
        
        super(id, file, false, keyCodec, keyComparator, valueCodec);
        
        Objects.requireNonNull(valueCodec, "valueCodec was null");
    }

    public BDB getDb() {
        return this.db;
    }

    @Override
    protected void _open() {
        this.db = new BDB();
        try {
            Files.createDirectories(this.path.getParent());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // build database, initialize stats we track
        if (!this.db.open(this.path.toAbsolutePath().toString(), HDB.OWRITER | HDB.OCREAT)){
            int ecode = db.ecode();
            throw new RuntimeException("TokyoCabinet open error: " + this.db.errmsg(ecode));
        }
        this.closer = new TokyoBigObjectCloser(this.id, this.persistent, this.path, this.db);
    }

    @Override
    public byte[] _get(byte[] keyBytes) {
        return this.db.get(keyBytes);
    }

    @Override
    public byte[] _put(byte[] keyBytes, byte[] valueBytes) {
        byte[] oldValueBytes = this.db.get(keyBytes);

        this.db.put(keyBytes, valueBytes);

        return oldValueBytes;
    }

    @Override
    public void _set(byte[] keyBytes, byte[] valueBytes) {
        this.db.put(keyBytes, valueBytes);
    }

    @Override
    public boolean _containsKey(byte[] keyBytes) {
        return this.db.get(keyBytes) != null;
    }

    @Override
    public byte[] _remove(byte[] keyBytes) {
        byte[] valueBytes = this.db.get(keyBytes);

        if (this.db.out(keyBytes)) {
            return valueBytes;
        }

        return null;
    }

    @Override
    public void _delete(byte[] keyBytes) {
        this.db.out(keyBytes);
    }

    @Override
    public Iterator<KeyValueBytes> _forwardIterator() {
        return TokyoForwardIterator.build(this.db);
    }

}