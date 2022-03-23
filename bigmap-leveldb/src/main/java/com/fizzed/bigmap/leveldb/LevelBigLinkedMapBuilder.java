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

// New Big Linked Map
public class LevelBigLinkedMapBuilder<K,V> {
 
    protected Path scratchDirectory;
    protected boolean persistent;
    protected boolean counts;
    protected long cacheSize;
    protected ByteCodec<?> keyCodec;
    protected Comparator<?> keyComparator;
    protected ByteCodec<?> valueCodec;
    
    public LevelBigLinkedMapBuilder() {
        this.persistent = false;
        this.counts = true;
        this.cacheSize = 30 * 1048576L;  // 30 MB by default
    }
    
    public LevelBigLinkedMapBuilder<K,V> setPersistent(boolean persistent) {
        this.persistent = persistent;
        return this;
    }
    
    public LevelBigLinkedMapBuilder<K,V> setCounts(boolean counts) {
        this.counts = counts;
        return this;
    }
    
    public LevelBigLinkedMapBuilder<K,V> setScratchDirectory(Path scratchDirectory) {
        this.scratchDirectory = scratchDirectory;
        return this;
    }

    public LevelBigLinkedMapBuilder<K,V> setCacheSize(long cacheSize) {
        this.cacheSize = cacheSize;
        return this;
    }
    
    public <N> LevelBigLinkedMapBuilder<N,V> setKeyType(Class<N> keyType) {
        return this.setKeyType(keyType, autoCodec(keyType));
    }

    public <N> LevelBigLinkedMapBuilder<N,V> setKeyType(Class<N> keyType, Comparator<N> keyComparator) {
        return this.setKeyType(keyType, autoCodec(keyType), keyComparator);
    }

    public <N> LevelBigLinkedMapBuilder<N,V> setKeyType(Class<N> keyType, ByteCodec<N> keyCodec) {
        return this.setKeyType(keyType, keyCodec, autoComparator(keyType));
    }

    public <N> LevelBigLinkedMapBuilder<N,V> setKeyType(Class<N> keyType, ByteCodec<N> keyCodec, Comparator<N> keyComparator) {
        Objects.requireNonNull(keyType, "keyType was null");
        Objects.requireNonNull(keyCodec, "keyCodec was null");
        Objects.requireNonNull(keyComparator, "keyComparator was null");
        this.keyCodec = keyCodec;
        this.keyComparator = keyComparator;
        return (LevelBigLinkedMapBuilder<N,V>)this;
    }

    public <N> LevelBigLinkedMapBuilder<K,N> setValueType(Class<N> valueType) {
        return this.setValueType(valueType, autoCodec(valueType));
    }

    public <N> LevelBigLinkedMapBuilder<K,N> setValueType(Class<N> valueType, ByteCodec<N> valueCodec) {
        this.valueCodec = valueCodec;
        return (LevelBigLinkedMapBuilder<K,N>)this;
    }
    
    public LevelBigLinkedMap<K,V> build() {
        UUID uuid = UUID.randomUUID();
        Path resolvedScratchDir = this.scratchDirectory != null
            ? this.scratchDirectory : Paths.get(".");
        
        Path directory = resolvedScratchDir;
        if (!this.persistent) {
            directory = resolvedScratchDir.resolve("levelbigmap-" + uuid);
        }
        
        return new LevelBigLinkedMap(this.persistent, this.counts, directory, this.cacheSize, this.keyCodec, this.keyComparator, this.valueCodec);
    }
    
}