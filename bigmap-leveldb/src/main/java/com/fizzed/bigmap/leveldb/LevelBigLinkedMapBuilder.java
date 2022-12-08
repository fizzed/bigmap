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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;

import static com.fizzed.bigmap.ByteCodecs.autoCodec;
import static com.fizzed.bigmap.Comparators.autoComparator;

public class LevelBigLinkedMapBuilder<K,V> {

    // leverage a bigmap builder for a linked version too
    protected final LevelBigMapBuilder<K,V> mapBuilder;

    public LevelBigLinkedMapBuilder() {
        this.mapBuilder = new LevelBigMapBuilder<>();
    }

    public LevelBigLinkedMapBuilder<K,V> setPersistent(boolean persistent) {
        this.mapBuilder.setPersistent(persistent);
        return this;
    }
    
    public LevelBigLinkedMapBuilder<K,V> setCounts(boolean counts) {
        this.mapBuilder.setCounts(counts);
        return this;
    }
    
    public LevelBigLinkedMapBuilder<K,V> setScratchDirectory(Path scratchDirectory) {
        this.mapBuilder.setScratchDirectory(scratchDirectory);
        return this;
    }

    public LevelBigLinkedMapBuilder<K,V> setCacheSize(long cacheSize) {
        this.mapBuilder.setCacheSize(cacheSize);
        return this;
    }
    
    public LevelBigLinkedMapBuilder<K,V> setKeyType(Class<K> keyType) {
        this.mapBuilder.setKeyType(keyType);
        return this;
    }

    public LevelBigLinkedMapBuilder<K,V> setKeyType(Class<K> keyType, Comparator<K> keyComparator) {
        this.mapBuilder.setKeyType(keyType, keyComparator);
        return this;
    }

    public LevelBigLinkedMapBuilder<K,V> setKeyType(Class<K> keyType, ByteCodec<K> keyCodec) {
        this.mapBuilder.setKeyType(keyType, keyCodec);
        return this;
    }

    public LevelBigLinkedMapBuilder<K,V> setKeyType(Class<K> keyType, ByteCodec<K> keyCodec, Comparator<K> keyComparator) {
        this.mapBuilder.setKeyType(keyType, keyCodec, keyComparator);
        return this;
    }

    public LevelBigLinkedMapBuilder<K,V> setValueType(Class<V> valueType) {
        this.mapBuilder.setValueType(valueType);
        return this;
    }

    public LevelBigLinkedMapBuilder<K,V> setValueType(Class<V> valueType, ByteCodec<V> valueCodec) {
        this.mapBuilder.setValueType(valueType, valueCodec);
        return this;
    }

    public LevelBigLinkedMap<K,V> build() {

        final Path dir = LevelBigMapHelper.prepFolderDirectoryPath(this.mapBuilder.scratchDirectory, this.mapBuilder.persistent, "levelbiglinkedmap");

        // we need 3 subdir paths
        final Path dataDir = dir.resolve("data");
        final Path i2kDir = dir.resolve("i2k");
        final Path k2iDir = dir.resolve("k2i");

        return new LevelBigLinkedMap(this.mapBuilder.persistent, this.mapBuilder.counts, dataDir, i2kDir, k2iDir, this.mapBuilder.cacheSize, this.mapBuilder.keyCodec, this.mapBuilder.keyComparator, this.mapBuilder.valueCodec);
    }

}