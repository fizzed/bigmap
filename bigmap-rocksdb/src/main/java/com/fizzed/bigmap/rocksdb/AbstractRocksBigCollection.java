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
import com.fizzed.bigmap.ByteCodec;
import org.rocksdb.*;

import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Objects;
import static com.fizzed.bigmap.BigMapHelper.sizeOf;

public class AbstractRocksBigCollection<K> implements Closeable {

    protected final Path directory;
    protected final ByteCodec<K> keyCodec;
    protected final Comparator<K> keyComparator;
    private Options options;
    protected RocksDB db;
    protected int size;
    protected long keyByteSize;
    protected long valueByteSize;

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public AbstractRocksBigCollection(
            Path directory,
            ByteCodec<K> keyCodec,
            Comparator<K> keyComparator) {
        
        Objects.requireNonNull(directory, "directory was null");
        Objects.requireNonNull(keyCodec, "keyCodec was null");
        Objects.requireNonNull(keyComparator, "keyComparator was null");

        this.directory = directory;
        this.keyCodec = keyCodec;
        this.keyComparator = keyComparator;
        this.open();
    }

    public ByteCodec<K> getKeyCodec() {
        return keyCodec;
    }

    public Path getDirectory() {
        return directory;
    }
    
    protected void open() {
        try {
            this.close();
        } catch (IOException e) {
            // do nothing
        }

        this.options = new Options();
        //this.options.compressionType(CompressionType.NONE);
        this.options.setCreateIfMissing(true);
        this.options.setComparator(BuiltinComparator.BYTEWISE_COMPARATOR);
        // wow, this comparator causes a massive memory leak
        //this.options.setComparator(new RocksJavaComparator(this.keyCodec, this.keyComparator));
        this.options.setDisableAutoCompactions(true);

        try {
            Files.createDirectories(this.directory.toAbsolutePath());

            // build database, initialize stats we track
            this.db = RocksDB.open(this.options, this.directory.toAbsolutePath().toString());
            
            this.size = 0;
            this.keyByteSize = 0L;
            this.valueByteSize = 0L;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
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
        if (this.db != null) {
            this.db.close();
            
            //if (!this.persistent) {
                this.destroy();
            //}
            
            this.db = null;
        }
    }
    
    protected void destroy() throws IOException {
        // we want to DESTROY it too!
        //factory.destroy(this.directory.toFile(), this.options);

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

    public void checkIfClosed() {
        if (this.db == null) {
            throw new IllegalStateException("BigMap rocksdb database is closed!");
        }
    }
    
    public long getKeyByteSize() {
        return this.keyByteSize;
    }

    public int size() {
        return this.size;
    }

    public boolean isEmpty() {
        return this.size <= 0;
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

    public void clear() {
        this.checkIfClosed();
        try {
            this.close();
            this.destroy();
            this.open();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    protected K firstKey() {
        this.checkIfClosed();

        final RocksIterator it = this.db.newIterator();
        it.seekToFirst();

        if (!it.isValid()) {
            throw new NoSuchElementException();
        }

        byte[] keyBytes = it.key();
        if (keyBytes != null) {
            return this.keyCodec.deserialize(keyBytes);
        }

        return null;
    }

}