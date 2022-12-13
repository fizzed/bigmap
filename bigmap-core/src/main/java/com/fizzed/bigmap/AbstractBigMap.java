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
package com.fizzed.bigmap;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Objects;

abstract public class AbstractBigMap<K,V> implements BigMap<K,V> {

    protected final Path directory;
    protected final boolean persistent;
    protected final ByteCodec<K> keyCodec;
    protected final Comparator<K> keyComparator;
    protected final ByteCodec<V> valueCodec;
    protected int size;
    protected long keyByteSize;
    protected long valueByteSize;

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public AbstractBigMap(
            Path directory,
            boolean persistent,
            ByteCodec<K> keyCodec,
            Comparator<K> keyComparator,
            ByteCodec<V> valueCodec) {
        
        Objects.requireNonNull(directory, "directory was null");
        Objects.requireNonNull(keyCodec, "keyCodec was null");
        Objects.requireNonNull(keyComparator, "keyComparator was null");

        this.directory = directory;
        this.persistent = persistent;
        this.keyCodec = keyCodec;
        this.keyComparator = keyComparator;
        this.valueCodec = valueCodec;
        this.open();
    }

    public Path getDirectory() {
        return this.directory;
    }

    public boolean isPersistent() {
        return persistent;
    }

    public ByteCodec<K> getKeyCodec() {
        return this.keyCodec;
    }

    public Comparator<K> getKeyComparator() {
        return this.keyComparator;
    }

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
    public void checkIfClosed() {
        if (this.isClosed()) {
            throw new IllegalStateException("Underlying database is closed. Unable to perform map operations.");
        }
    }

    protected void open() {
        try {
            this.close();
        } catch (IOException e) {
            // do nothing
        }

        try {
            if (this.directory != null) {
                Files.createDirectories(this.directory.toAbsolutePath());
            }

            this._open();

            this.size = 0;
            this.keyByteSize = 0L;
            this.valueByteSize = 0L;
        } catch (Exception e) {
            throw new BigMapDataException(e);
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
    public void close() throws IOException {
        if (!this.isClosed()) {
            this._close();
        }

        if (!this.persistent) {
            this.destroy();
        }
    }

    abstract protected void _close() throws IOException;
    
    protected void destroy() throws IOException {
        this.size = 0;
        this.keyByteSize = 0L;
        this.valueByteSize = 0L;
        
        // remove existing database...
        try {
            if (Files.exists(this.directory)) {
                Files.list(this.directory).forEach(file -> {
                    try {
                        Files.delete(file);
                    } catch (IOException e) {
                        throw new UncheckedIOException("Unable to delete existing database file!", e);
                    }
                });
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to list existing database directory!", e);
        }
    }

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