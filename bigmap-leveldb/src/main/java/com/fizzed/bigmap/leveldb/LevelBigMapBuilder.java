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

import com.fizzed.bigmap.BigObjectRegistry;
import com.fizzed.bigmap.impl.AbstractBigObjectBuilder;
import com.fizzed.bigmap.impl.BigMapHelper;
import com.fizzed.bigmap.ByteCodec;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.UUID;

public class LevelBigMapBuilder<K,V> extends AbstractBigObjectBuilder {

    public LevelBigMapBuilder<K,V> registerForGarbageMonitoring() {
        super._registerForGarbageMonitoring();
        return this;
    }

    public LevelBigMapBuilder<K,V> registerForGarbageMonitoring(BigObjectRegistry registry) {
        super._registerForGarbageMonitoring(registry);
        return this;
    }

    public LevelBigMapBuilder<K,V> setScratchDirectory(Path scratchDirectory) {
        super._setScratchDirectory(scratchDirectory);
        return this;
    }

    public <K2> LevelBigMapBuilder<K2,V> setKeyType(Class<K2> keyType) {
        super._setKeyType(keyType);
        return (LevelBigMapBuilder<K2,V>)this;
    }

    public <K2> LevelBigMapBuilder<K2,V> setKeyType(Class<K2> keyType, Comparator<K2> keyComparator) {
        super._setKeyType(keyType, keyComparator);
        return (LevelBigMapBuilder<K2,V>)this;
    }

    public <K2> LevelBigMapBuilder<K2,V> setKeyType(Class<K2> keyType, ByteCodec<K2> keyCodec) {
        super._setKeyType(keyType, keyCodec);
        return (LevelBigMapBuilder<K2,V>)this;
    }

    public <K2> LevelBigMapBuilder<K2,V> setKeyType(Class<K2> keyType, ByteCodec<K2> keyCodec, Comparator<K2> keyComparator) {
        super._setKeyType(keyType, keyCodec, keyComparator);
        return (LevelBigMapBuilder<K2,V>)this;
    }

    public <V2> LevelBigMapBuilder<K,V2> setValueType(Class<V2> valueType) {
        super._setValueType(valueType);
        return (LevelBigMapBuilder<K,V2>)this;
    }

    public <V2> LevelBigMapBuilder<K,V2> setValueType(Class<V2> valueType, ByteCodec<V2> valueCodec) {
        super._setValueType(valueType, valueCodec);
        return (LevelBigMapBuilder<K,V2>)this;
    }
    
    public LevelBigMap<K,V> build() {
        final UUID id = UUID.randomUUID();
        final Path dir = BigMapHelper.resolveScratchDirectory(this.scratchDirectory, false, id, "bigmap-level");

        final LevelBigMap<K,V> map = new LevelBigMap<>(id, dir, (ByteCodec<K>)this.keyCodec, (Comparator<K>)this.keyComparator, (ByteCodec<V>)this.valueCodec);
        map.setListener(this.registry);
        map.open();
        return map;
    }

}