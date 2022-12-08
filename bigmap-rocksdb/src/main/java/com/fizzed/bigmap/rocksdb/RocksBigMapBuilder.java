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

import com.fizzed.bigmap.BigMapHelper;
import com.fizzed.bigmap.ByteCodec;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Objects;

import static com.fizzed.bigmap.ByteCodecs.autoCodec;
import static com.fizzed.bigmap.Comparators.autoComparator;

public class RocksBigMapBuilder<K,V> {
 
    protected Path scratchDirectory;
//    protected boolean persistent;
//    protected boolean counts;
//    protected long cacheSize;
    protected Class<K> keyClass;
    protected Class<V> valueClass;
    protected ByteCodec<?> keyCodec;
    protected Comparator<?> keyComparator;
    protected ByteCodec<?> valueCodec;
    
    public RocksBigMapBuilder() {
//        this.persistent = false;
//        this.counts = true;
//        this.cacheSize = 2 * 1048576L;  // 2 MB by default
    }
    
//    public RocksBigMapBuilder<K,V> setPersistent(boolean persistent) {
//        this.persistent = persistent;
//        return this;
//    }
//
//    public RocksBigMapBuilder<K,V> setCounts(boolean counts) {
//        this.counts = counts;
//        return this;
//    }
    
    public RocksBigMapBuilder<K,V> setScratchDirectory(Path scratchDirectory) {
        this.scratchDirectory = scratchDirectory;
        return this;
    }

//    public RocksBigMapBuilder<K,V> setCacheSize(long cacheSize) {
//        this.cacheSize = cacheSize;
//        return this;
//    }
    
    public RocksBigMapBuilder<K,V> setKeyType(Class<K> keyType) {
        return this.setKeyType(keyType, autoCodec(keyType));
    }

    public RocksBigMapBuilder<K,V> setKeyType(Class<K> keyType, Comparator<K> keyComparator) {
        return this.setKeyType(keyType, autoCodec(keyType), keyComparator);
    }

    public RocksBigMapBuilder<K,V> setKeyType(Class<K> keyType, ByteCodec<K> keyCodec) {
        return this.setKeyType(keyType, keyCodec, autoComparator(keyType));
    }

    public RocksBigMapBuilder<K,V> setKeyType(Class<K> keyType, ByteCodec<K> keyCodec, Comparator<K> keyComparator) {
        Objects.requireNonNull(keyType, "keyType was null");
        Objects.requireNonNull(keyCodec, "keyCodec was null");
        Objects.requireNonNull(keyComparator, "keyComparator was null");
        this.keyClass = keyType;
        this.keyCodec = keyCodec;
        this.keyComparator = keyComparator;
        return (RocksBigMapBuilder<K,V>)this;
    }

    public RocksBigMapBuilder<K,V> setValueType(Class<V> valueType) {
        return this.setValueType(valueType, autoCodec(valueType));
    }

    public RocksBigMapBuilder<K,V> setValueType(Class<V> valueType, ByteCodec<V> valueCodec) {
        this.valueClass = valueType;
        this.valueCodec = valueCodec;
        return (RocksBigMapBuilder<K,V>)this;
    }
    
    public RocksBigMap<K,V> build() {
        final Path dir = BigMapHelper.resolveScratchDirectory(this.scratchDirectory, false, "rocksbigmap");

        return new RocksBigMap(dir, this.keyCodec, this.keyComparator, this.valueCodec);
    }

}