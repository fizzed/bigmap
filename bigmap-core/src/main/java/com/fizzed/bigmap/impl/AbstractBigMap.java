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
package com.fizzed.bigmap.impl;

import com.fizzed.bigmap.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;

abstract public class AbstractBigMap<K,V> implements BigMap<K,V> {

    protected final UUID id;
    protected final Path path;
    protected final boolean persistent;
    protected final ByteCodec<K> keyCodec;
    protected final Comparator<K> keyComparator;
    protected final ByteCodec<V> valueCodec;
    protected int size;
    protected long keyByteSize;
    protected long valueByteSize;
    protected BigObjectListener listener;
    protected BigObjectCloser closer;

    public AbstractBigMap(
            UUID id,
            Path path,
            boolean persistent,
            ByteCodec<K> keyCodec,
            Comparator<K> keyComparator,
            ByteCodec<V> valueCodec) {
        
        Objects.requireNonNull(id, "id was null");
        Objects.requireNonNull(path, "path was null");
        Objects.requireNonNull(keyCodec, "keyCodec was null");
        //Objects.requireNonNull(keyComparator, "keyComparator was null");

        this.id = id;
        this.path = path;
        this.persistent = persistent;
        this.keyCodec = keyCodec;
        this.keyComparator = keyComparator;
        this.valueCodec = valueCodec;
    }

    @Override
    public BigObjectListener getListener() {
        return this.listener;
    }

    @Override
    public void setListener(BigObjectListener listener) {
        this.listener = listener;
    }

    @Override
    public UUID getId() { return this.id; }

    @Override
    public Path getPath() {
        return this.path;
    }

    @Override
    public boolean isPersistent() {
        return this.persistent;
    }

    @Override
    final public boolean isClosed() {
        return this.closer == null || this.closer.isClosed();
    }

    @Override
    public ByteCodec<K> getKeyCodec() {
        return this.keyCodec;
    }

    @Override
    public Comparator<K> getKeyComparator() {
        return this.keyComparator;
    }

    @Override
    public ByteCodec<V> getValueCodec() {
        return this.valueCodec;
    }

    @Override
    public long getKeyByteSize() {
        return this.keyByteSize;
    }

    @Override
    public long getValueByteSize() {
        return this.valueByteSize;
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public void open() {
        try {
            this.close();
        } catch (IOException e) {
            // do nothing
        }

        try {
            this._open();

            this.size = 0;
            this.keyByteSize = 0L;
            this.valueByteSize = 0L;
        } catch (Exception e) {
            throw new BigMapDataException(e);
        }

        if (this.listener != null) {
            this.listener.onOpened(this);
        }
    }

    abstract protected void _open();
    
//    protected void loadCounts() throws IOException {
//        try (DBIterator it = this.db.iterator()) {
//            it.seekToFirst();
//            while (it.hasNext()) {
//                Entry<byte[],byte[]> entry = it.next();
//                this.size++;
//                this.keyByteSize += entry.getKey().length;
//                this.valueByteSize += entry.getValue().length;
//            }
//        }
//    }

    @Override
    final public BigObjectCloser getCloser() {
        return this.closer;
    }

    @Override
    final synchronized public void close() throws IOException {
        if (this.closer != null) {
            this.closer.close();
            this.closer = null;
        }

        if (this.listener != null) {
            this.listener.onClosed(this);
        }
    }

    @Override
    public void clear() {
        try {
            this.close();
            this.open();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void _entryAdded(long keyByteSize, long valueByteSize) {
        this.size++;
        this.keyByteSize += keyByteSize;
        this.valueByteSize += valueByteSize;
    }

    public void _entryRemoved(long keyByteSize, long valueByteSize) {
        this.size--;
        this.keyByteSize -= keyByteSize;
        this.valueByteSize -= valueByteSize;
    }

}