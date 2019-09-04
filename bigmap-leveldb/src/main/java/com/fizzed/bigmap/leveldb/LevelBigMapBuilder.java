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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import com.fizzed.bigmap.ByteCodec;
import static com.fizzed.bigmap.ByteCodecs.autoCodec;
import static com.fizzed.bigmap.Comparators.autoComparator;
import java.util.Comparator;
import java.util.Objects;

public class LevelBigMapBuilder<K,V> {
 
    protected Path scratchDirectory;
    protected boolean persistent;
    protected long cacheSize;
    protected ByteCodec<?> keyCodec;
    protected Comparator<?> keyComparator;
    protected ByteCodec<?> valueCodec;
    
    public LevelBigMapBuilder() {
        this.persistent = false;
        this.cacheSize = 30 * 1048576L;  // 30 MB by default
    }
    
    public LevelBigMapBuilder<K,V> setPersistent(boolean persistent) {
        this.persistent = persistent;
        return this;
    }
    
    public LevelBigMapBuilder<K,V> setScratchDirectory(Path scratchDirectory) {
        this.scratchDirectory = scratchDirectory;
        return this;
    }

    public LevelBigMapBuilder<K,V> setCacheSize(long cacheSize) {
        this.cacheSize = cacheSize;
        return this;
    }
    
    public <N> LevelBigMapBuilder<N,V> setKeyType(Class<N> keyType) {
        return this.setKeyType(keyType, autoCodec(keyType));
    }

    public <N> LevelBigMapBuilder<N,V> setKeyType(Class<N> keyType, Comparator<N> keyComparator) {
        return this.setKeyType(keyType, autoCodec(keyType), keyComparator);
    }

    public <N> LevelBigMapBuilder<N,V> setKeyType(Class<N> keyType, ByteCodec<N> keyCodec) {
        return this.setKeyType(keyType, keyCodec, autoComparator(keyType));
    }

    public <N> LevelBigMapBuilder<N,V> setKeyType(Class<N> keyType, ByteCodec<N> keyCodec, Comparator<N> keyComparator) {
        Objects.requireNonNull(keyType, "keyType was null");
        Objects.requireNonNull(keyCodec, "keyCodec was null");
        Objects.requireNonNull(keyComparator, "keyComparator was null");
        this.keyCodec = keyCodec;
        this.keyComparator = keyComparator;
        return (LevelBigMapBuilder<N,V>)this;
    }

    public <N> LevelBigMapBuilder<K,N> setValueType(Class<N> valueType) {
        return this.setValueType(valueType, autoCodec(valueType));
    }

    public <N> LevelBigMapBuilder<K,N> setValueType(Class<N> valueType, ByteCodec<N> valueCodec) {
        this.valueCodec = valueCodec;
        return (LevelBigMapBuilder<K,N>)this;
    }
    
    public LevelBigMap<K,V> build() {
        UUID uuid = UUID.randomUUID();
        Path resolvedScratchDir = this.scratchDirectory != null
            ? this.scratchDirectory : Paths.get(".");
        
        Path directory = resolvedScratchDir;
        if (!this.persistent) {
            directory = resolvedScratchDir.resolve("levelbigmap-" + uuid);
        }
        
        return new LevelBigMap(this.persistent, directory, this.cacheSize, this.keyCodec, this.keyComparator, this.valueCodec);
    }
    
}