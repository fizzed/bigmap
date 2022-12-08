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

import com.fizzed.bigmap.ByteCodec;
import com.fizzed.crux.util.StopWatch;
import org.h2.mvstore.MVStore;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import oshi.SystemInfo;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class PerfRocksDBMap<K,V> implements Map<K,V> {

    private final RocksDB db;
    private final ByteCodec<K> keyCodec;
    private final ByteCodec<V> valueCodec;

    public PerfRocksDBMap(
            ByteCodec<K> keyCodec,
            ByteCodec<V> valueCodec) {

        try {
            RocksDB.loadLibrary();
            final Options options = new Options();
            options.setCreateIfMissing(true);
            Path dbDir = Paths.get("target/rocks-" + UUID.randomUUID());
            Files.createDirectories(dbDir.toAbsolutePath());
            // apparently these help control memory usage

            ;
//        try {

            db = RocksDB.open(options, dbDir.toAbsolutePath().toString());
//        } catch(IOException | RocksDBException ex) {
//            log.error("Error initializng RocksDB, check configurations and permissions, exception: {}, message: {}, stackTrace: {}",
//                ex.getCause(), ex.getMessage(), ex.getStackTrace());
//        }
            //log.info("RocksDB initialized and ready to use");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        this.keyCodec = keyCodec;
        this.valueCodec = valueCodec;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean containsKey(Object key) {
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        return false;
    }

    @Override
    public V get(Object key) {
        try {
            byte[] keyBytes = this.keyCodec.serialize((K) key);
            byte[] valueBytes = db.get(keyBytes);
            if (valueBytes != null) {
                return this.valueCodec.deserialize(valueBytes);
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public V put(K key, V value) {
        try {
            byte[] keyBytes = this.keyCodec.serialize(key);
            byte[] valueBytes = this.valueCodec.serialize(value);
            db.put(keyBytes, valueBytes);
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public V remove(Object key) {
        return null;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {

    }

    @Override
    public void clear() {

    }

    @Override
    public Set<K> keySet() {
        return null;
    }

    @Override
    public Collection<V> values() {
        return null;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return null;
    }
}