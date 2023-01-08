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
package com.fizzed.bigmap.tkrzw;

import com.fizzed.bigmap.*;
import com.fizzed.bigmap.impl.AbstractBigMap;
import com.fizzed.bigmap.impl.ByteArrayBigMap;
import com.fizzed.bigmap.impl.KeyValueBytes;
import tkrzw.DBM;
import tkrzw.Status;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;
import java.util.UUID;

public class TkrzwBigMap<K,V> extends AbstractBigMap<K,V> implements ByteArrayBigMap<K,V>, BigSortedMap<K,V> {

    protected DBM db;

    protected TkrzwBigMap(
            UUID id,
            Path file,
            ByteCodec<K> keyCodec,
            Comparator<K> keyComparator,
            ByteCodec<V> valueCodec) {
        
        super(id, file, false, keyCodec, keyComparator, valueCodec);
        
        Objects.requireNonNull(valueCodec, "valueCodec was null");
    }

    public DBM getDb() {
        return this.db;
    }

    @Override
    protected void _open() {
        this.db = new DBM();
        try {
            Files.createDirectories(this.path.getParent());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Status status;

        status = this.db.open(this.path.toAbsolutePath().toString(), true);
        if (!status.isOK()){
            throw new RuntimeException("Tkrzw open error: " + status.getMessage());
        }
        this.closer = new TkrzwBigObjectCloser(this.id, this.persistent, this.path, this.db);
    }

    @Override
    public byte[] _get(byte[] keyBytes) {
        return this.db.get(keyBytes);
    }

    @Override
    public byte[] _put(byte[] keyBytes, byte[] valueBytes) {
        byte[] oldValueBytes = this.db.get(keyBytes);

        this._set(keyBytes, valueBytes);

        return oldValueBytes;
    }

    @Override
    public void _set(byte[] keyBytes, byte[] valueBytes) {
        final Status status = this.db.set(keyBytes, valueBytes);
        if (!status.isOK()) {
            throw new BigMapDataException("Set failed " + status.getMessage());
        }
    }

    @Override
    public boolean _containsKey(byte[] keyBytes) {
        return this.db.get(keyBytes) != null;
    }

    @Override
    public byte[] _remove(byte[] keyBytes) {
        byte[] valueBytes = this.db.get(keyBytes);

        final Status status = this.db.remove(keyBytes);

        if (!status.isOK()) {
            // if no record is found?
            if (status.getCode() == Status.Code.NOT_FOUND_ERROR) {
                return null;
            }
            throw new BigMapDataException("Remove failed " + status.getMessage());
        }

        return valueBytes;
    }

    @Override
    public void _delete(byte[] keyBytes) {
        final Status status = this.db.remove(keyBytes);

        if (!status.isOK() && status.getCode() != Status.Code.NOT_FOUND_ERROR) {
            throw new BigMapDataException("Remove failed " + status.getMessage());
        }
    }

    @Override
    public Iterator<KeyValueBytes> _forwardIterator() {
        return TkrzwForwardIterator.build(this.db);
    }

}