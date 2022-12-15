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

import com.fizzed.bigmap.*;
import com.fizzed.bigmap.impl.AbstractBigObjectBuilder;
import com.fizzed.bigmap.impl.BigMapHelper;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.UUID;

public class LevelBigLinkedMapBuilder<K,V> extends AbstractBigObjectBuilder {

    public LevelBigLinkedMapBuilder<K,V> registerForGarbageMonitoring() {
        super._registerForGarbageMonitoring();
        return this;
    }

    public LevelBigLinkedMapBuilder<K,V> registerForGarbageMonitoring(BigObjectRegistry registry) {
        super._registerForGarbageMonitoring(registry);
        return this;
    }

    public LevelBigLinkedMapBuilder<K,V> setScratchDirectory(Path scratchDirectory) {
        super._setScratchDirectory(scratchDirectory);
        return this;
    }
    
    public <K2> LevelBigLinkedMapBuilder<K2,V> setKeyType(Class<K2> keyType) {
        super._setKeyType(keyType);
        return (LevelBigLinkedMapBuilder<K2,V>)this;
    }

    public <K2> LevelBigLinkedMapBuilder<K2,V> setKeyType(Class<K2> keyType, Comparator<K2> keyComparator) {
        super._setKeyType(keyType, keyComparator);
        return (LevelBigLinkedMapBuilder<K2,V>)this;
    }

    public <K2> LevelBigLinkedMapBuilder<K2,V> setKeyType(Class<K2> keyType, ByteCodec<K2> keyCodec) {
        super._setKeyType(keyType, keyCodec);
        return (LevelBigLinkedMapBuilder<K2,V>)this;
    }

    public <K2> LevelBigLinkedMapBuilder<K2,V> setKeyType(Class<K2> keyType, ByteCodec<K2> keyCodec, Comparator<K2> keyComparator) {
        super._setKeyType(keyType, keyCodec, keyComparator);
        return (LevelBigLinkedMapBuilder<K2,V>)this;
    }

    public <V2> LevelBigLinkedMapBuilder<K,V2> setValueType(Class<V2> valueType) {
        super._setValueType(valueType);
        return (LevelBigLinkedMapBuilder<K,V2>)this;
    }

    public <V2> LevelBigLinkedMapBuilder<K,V2> setValueType(Class<V2> valueType, ByteCodec<V2> valueCodec) {
        super._setValueType(valueType, valueCodec);
        return (LevelBigLinkedMapBuilder<K,V2>)this;
    }
    
    public LevelBigLinkedMap<K,V> build() {
        final UUID id = UUID.randomUUID();
        final Path dir = BigMapHelper.resolveScratchDirectory(this.scratchDirectory, false, id, "biglinkedmap-level");

        // we need 3 subdir paths
        final Path dataDir = dir.resolve("data");
        final Path i2kDir = dir.resolve("i2k");
        final Path k2iDir = dir.resolve("k2i");

        final ByteCodec<Integer> integerByteCodec = ByteCodecs.integerCodec();
        final Comparator<Integer> integerComparator = Comparators.autoComparator(Integer.class);
        final LevelBigMap<K,V> dataMap = new LevelBigMap<>(UUID.randomUUID(), dataDir, (ByteCodec<K>)this.keyCodec, (Comparator<K>)this.keyComparator, (ByteCodec<V>)this.valueCodec);
        final LevelBigMap<Integer,K> insertOrderToKeyMap = new LevelBigMap<>(UUID.randomUUID(), i2kDir, integerByteCodec, integerComparator, (ByteCodec<K>)this.keyCodec);
        final LevelBigMap<K,Integer> keyToInsertOrderMap = new LevelBigMap<>(UUID.randomUUID(), k2iDir, (ByteCodec<K>)this.keyCodec, (Comparator<K>)this.keyComparator, integerByteCodec);

        final LevelBigLinkedMap<K,V> map = new LevelBigLinkedMap<>(id, dir, false, dataMap, insertOrderToKeyMap, keyToInsertOrderMap);
        map.setListener(this.registry);
        map.open();
        return map;
    }

}