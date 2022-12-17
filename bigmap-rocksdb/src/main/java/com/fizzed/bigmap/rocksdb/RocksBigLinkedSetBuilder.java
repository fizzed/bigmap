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

public class RocksBigLinkedSetBuilder<V> extends AbstractBigObjectBuilder {

    public RocksBigLinkedSetBuilder<V> registerForGarbageMonitoring() {
        super._registerForGarbageMonitoring();
        return this;
    }

    public RocksBigLinkedSetBuilder<V> registerForGarbageMonitoring(BigObjectRegistry registry) {
        super._registerForGarbageMonitoring(registry);
        return this;
    }

    public RocksBigLinkedSetBuilder<V> setScratchDirectory(Path scratchDirectory) {
        super._setScratchDirectory(scratchDirectory);
        return this;
    }
    
    public <V2> RocksBigLinkedSetBuilder<V2> setValueType(Class<V2> valueType) {
        super._setKeyType(valueType);
        return (RocksBigLinkedSetBuilder<V2>)this;
    }

    public <V2> RocksBigLinkedSetBuilder<V2> setValueType(Class<V2> valueType, Comparator<V2> valueComparator) {
        super._setKeyType(valueType, valueComparator);
        return (RocksBigLinkedSetBuilder<V2>)this;
    }

    public <V2> RocksBigLinkedSetBuilder<V2> setValueType(Class<V2> valueType, ByteCodec<V2> valueCodec) {
        super._setKeyType(valueType, valueCodec);
        return (RocksBigLinkedSetBuilder<V2>)this;
    }

    public <V2> RocksBigLinkedSetBuilder<V2> setValueType(Class<V2> valueType, ByteCodec<V2> valueCodec, Comparator<V2> valueComparator) {
        super._setKeyType(valueType, valueCodec, valueComparator);
        return (RocksBigLinkedSetBuilder<V2>)this;
    }
    
    public RocksBigLinkedSet<V> build() {
        final UUID id = UUID.randomUUID();
        final Path dir = BigMapHelper.resolveScratchDirectory(this.scratchDirectory, false, id, "biglinkedset-rocks");

        // we need 3 subdir paths
        final Path dataDir = dir.resolve("data");
        final Path i2kDir = dir.resolve("i2k");
        final Path k2iDir = dir.resolve("k2i");

        final ByteCodec<Integer> integerByteCodec = ByteCodecs.integerCodec();
        final Comparator<Integer> integerComparator = Comparators.autoComparator(Integer.class);
        final RocksBigMap<V,None> dataMap = new RocksBigMap<>(UUID.randomUUID(), dataDir, (ByteCodec<V>)this.keyCodec, (Comparator<V>)this.keyComparator, ByteCodecs.noneCodec());
        final RocksBigMap<Integer,V> insertOrderToKeyMap = new RocksBigMap<>(UUID.randomUUID(), i2kDir, integerByteCodec, integerComparator, (ByteCodec<V>)this.keyCodec);
        final RocksBigMap<V,Integer> keyToInsertOrderMap = new RocksBigMap<>(UUID.randomUUID(), k2iDir, (ByteCodec<V>)this.keyCodec, (Comparator<V>)this.keyComparator, integerByteCodec);

        final RocksBigLinkedMap<V,None> map = new RocksBigLinkedMap<>(id, dir, false, dataMap, insertOrderToKeyMap, keyToInsertOrderMap);

        final RocksBigLinkedSet<V> set = new RocksBigLinkedSet<>(map);
        set.setListener(this.registry);
        set.open();
        return set;
    }

}