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

import com.fizzed.bigmap.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Objects;

import static com.fizzed.bigmap.ByteCodecs.autoCodec;
import static com.fizzed.bigmap.Comparators.autoComparator;

public class RocksBigMapBuilder<K,V> extends AbstractBigMapBuilder {

    public RocksBigMapBuilder<K,V> setScratchDirectory(Path scratchDirectory) {
        super._setScratchDirectory(scratchDirectory);
        return this;
    }

    public <K2> RocksBigMapBuilder<K2,V> setKeyType(Class<K2> keyType) {
        super._setKeyType(keyType);
        return (RocksBigMapBuilder<K2,V>)this;
    }

    public <K2> RocksBigMapBuilder<K2,V> setKeyType(Class<K2> keyType, Comparator<K2> keyComparator) {
        super._setKeyType(keyType, keyComparator);
        return (RocksBigMapBuilder<K2,V>)this;
    }

    public <K2> RocksBigMapBuilder<K2,V> setKeyType(Class<K2> keyType, ByteCodec<K2> keyCodec) {
        super._setKeyType(keyType, keyCodec);
        return (RocksBigMapBuilder<K2,V>)this;
    }

    public <K2> RocksBigMapBuilder<K2,V> setKeyType(Class<K2> keyType, ByteCodec<K2> keyCodec, Comparator<K2> keyComparator) {
        super._setKeyType(keyType, keyCodec, keyComparator);
        return (RocksBigMapBuilder<K2,V>)this;
    }

    public <V2> RocksBigMapBuilder<K,V2> setValueType(Class<V2> valueType) {
        super._setValueType(valueType);
        return (RocksBigMapBuilder<K,V2>)this;
    }

    public <V2> RocksBigMapBuilder<K,V2> setValueType(Class<V2> valueType, ByteCodec<V2> valueCodec) {
        super._setValueType(valueType, valueCodec);
        return (RocksBigMapBuilder<K,V2>)this;
    }
    
    public RocksBigMap<K,V> build() {
        final Path dir = BigMapHelper.resolveScratchDirectory(this.scratchDirectory, false, "rocksbigmap");

        return new RocksBigMap<>(dir, (ByteCodec<K>)this.keyCodec, (Comparator<K>)this.keyComparator, (ByteCodec<V>)this.valueCodec);
    }

}