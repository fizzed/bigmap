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

import com.fizzed.bigmap.BigMapCodec;
import com.fizzed.bigmap.BigMapCodecs;
import com.fizzed.bigmap.FSTBigMapCodec;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import org.iq80.leveldb.CompressionType;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;
import static org.iq80.leveldb.impl.Iq80DBFactory.factory;

/**
 *
 * @author jjlauer
 */
public class LevelBigMapBuilder {
 
    private Path directory;
    
    public LevelBigMapBuilder setDirectory(Path directory) {
        this.directory = directory;
        return this;
    }
    
    public <K,V> LevelBigMap<K,V> build(Class<K> keyType, Class<V> valueType) {
        UUID uuid = UUID.randomUUID();
        Path _directory = this.directory != null ? this.directory : Paths.get(".");
        Path dbFile = _directory.resolve("level-" + uuid);
        
        Options options = new Options();
        options.compressionType(CompressionType.NONE);
        options.createIfMissing(true);
        options.cacheSize(50 * 1048576);   // 100MB cache

        try {
            DB db = factory.open(dbFile.toFile(), options);
            BigMapCodec<K> keyCodec = BigMapCodecs.of(keyType);
            BigMapCodec<V> valueCodec = BigMapCodecs.of(valueType);
            return new LevelBigMap(db, keyCodec, valueCodec);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        
//        options.comparator(new DBComparator() {
//            @Override
//            public String name() {
//                return "simple";
//            }
//
//            @Override
//            public byte[] findShortestSeparator(byte[] start, byte[] limit) {
//                return start;
//            }
//
//            @Override
//            public byte[] findShortSuccessor(byte[] key) {
//                return key;
//            }
//
//            @Override
//            public int compare(byte[] o1, byte[] o2) {
//                
//            }
//        })
        
    }
    
}