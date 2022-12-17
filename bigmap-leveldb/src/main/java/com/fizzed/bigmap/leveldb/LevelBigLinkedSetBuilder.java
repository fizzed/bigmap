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
import com.fizzed.bigmap.ByteCodec;
import com.fizzed.bigmap.ByteCodecs;
import com.fizzed.bigmap.Comparators;
import com.fizzed.bigmap.impl.AbstractBigObjectBuilder;
import com.fizzed.bigmap.impl.BigMapHelper;
import com.fizzed.bigmap.impl.None;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.UUID;

public class LevelBigLinkedSetBuilder<V> extends AbstractBigObjectBuilder {

    public LevelBigLinkedSetBuilder<V> registerForGarbageMonitoring() {
        super._registerForGarbageMonitoring();
        return this;
    }

    public LevelBigLinkedSetBuilder<V> registerForGarbageMonitoring(BigObjectRegistry registry) {
        super._registerForGarbageMonitoring(registry);
        return this;
    }

    public LevelBigLinkedSetBuilder<V> setScratchDirectory(Path scratchDirectory) {
        super._setScratchDirectory(scratchDirectory);
        return this;
    }
    
    public <V2> LevelBigLinkedSetBuilder<V2> setValueType(Class<V2> valueType) {
        super._setKeyType(valueType);
        return (LevelBigLinkedSetBuilder<V2>)this;
    }

    public <V2> LevelBigLinkedSetBuilder<V2> setValueType(Class<V2> valueType, Comparator<V2> valueComparator) {
        super._setKeyType(valueType, valueComparator);
        return (LevelBigLinkedSetBuilder<V2>)this;
    }

    public <V2> LevelBigLinkedSetBuilder<V2> setValueType(Class<V2> valueType, ByteCodec<V2> valueCodec) {
        super._setKeyType(valueType, valueCodec);
        return (LevelBigLinkedSetBuilder<V2>)this;
    }

    public <V2> LevelBigLinkedSetBuilder<V2> setValueType(Class<V2> valueType, ByteCodec<V2> valueCodec, Comparator<V2> valueComparator) {
        super._setKeyType(valueType, valueCodec, valueComparator);
        return (LevelBigLinkedSetBuilder<V2>)this;
    }
    
    public LevelBigLinkedSet<V> build() {
        final UUID id = UUID.randomUUID();
        final Path dir = BigMapHelper.resolveScratchDirectory(this.scratchDirectory, false, id, "biglinkedset-level");

        // we need 3 subdir paths
        final Path dataDir = dir.resolve("data");
        final Path i2kDir = dir.resolve("i2k");
        final Path k2iDir = dir.resolve("k2i");

        final ByteCodec<Integer> integerByteCodec = ByteCodecs.integerCodec();
        final Comparator<Integer> integerComparator = Comparators.autoComparator(Integer.class);
        final LevelBigMap<V,None> dataMap = new LevelBigMap<>(UUID.randomUUID(), dataDir, (ByteCodec<V>)this.keyCodec, (Comparator<V>)this.keyComparator, ByteCodecs.noneCodec());
        final LevelBigMap<Integer,V> insertOrderToKeyMap = new LevelBigMap<>(UUID.randomUUID(), i2kDir, integerByteCodec, integerComparator, (ByteCodec<V>)this.keyCodec);
        final LevelBigMap<V,Integer> keyToInsertOrderMap = new LevelBigMap<>(UUID.randomUUID(), k2iDir, (ByteCodec<V>)this.keyCodec, (Comparator<V>)this.keyComparator, integerByteCodec);

        final LevelBigLinkedMap<V,None> map = new LevelBigLinkedMap<>(id, dir, false, dataMap, insertOrderToKeyMap, keyToInsertOrderMap);

        final LevelBigLinkedSet<V> set = new LevelBigLinkedSet<>(map);
        set.setListener(this.registry);
        set.open();
        return set;
    }

}