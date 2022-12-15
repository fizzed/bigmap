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
import com.fizzed.bigmap.impl.None;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.UUID;

public class LevelBigSetBuilder<V> extends AbstractBigObjectBuilder {

    public LevelBigSetBuilder<V> registerForGarbageMonitoring() {
        super._registerForGarbageMonitoring();
        return this;
    }

    public LevelBigSetBuilder<V> registerForGarbageMonitoring(BigObjectRegistry registry) {
        super._registerForGarbageMonitoring(registry);
        return this;
    }

    public LevelBigSetBuilder<V> setScratchDirectory(Path scratchDirectory) {
        super._setScratchDirectory(scratchDirectory);
        return this;
    }
    
    public <V2> LevelBigSetBuilder<V2> setValueType(Class<V2> valueType) {
        super._setKeyType(valueType);
        return (LevelBigSetBuilder<V2>)this;
    }

    public <V2> LevelBigSetBuilder<V2> setValueType(Class<V2> valueType, Comparator<V2> valueComparator) {
        super._setKeyType(valueType, valueComparator);
        return (LevelBigSetBuilder<V2>)this;
    }

    public <V2> LevelBigSetBuilder<V2> setValueType(Class<V2> valueType, ByteCodec<V2> valueCodec) {
        super._setKeyType(valueType, valueCodec);
        return (LevelBigSetBuilder<V2>)this;
    }

    public <V2> LevelBigSetBuilder<V2> setValueType(Class<V2> valueType, ByteCodec<V2> valueCodec, Comparator<V2> valueComparator) {
        super._setKeyType(valueType, valueCodec, valueComparator);
        return (LevelBigSetBuilder<V2>)this;
    }
    
    public LevelBigSet<V> build() {
        final UUID id = UUID.randomUUID();
        final Path dir = BigMapHelper.resolveScratchDirectory(this.scratchDirectory, false, id, "bigset-level");

        final LevelBigMap<V,None> map = new LevelBigMap<>(id, dir, (ByteCodec<V>)this.keyCodec, (Comparator<V>)this.keyComparator, ByteCodecs.noneCodec());

        final LevelBigSet<V> set = new LevelBigSet<>(map);
        set.setListener(this.registry);
        set.open();
        return set;
    }

}