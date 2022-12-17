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
package com.fizzed.bigmap.tokyocabinet;

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

public class TokyoBigLinkedSetBuilder<V> extends AbstractBigObjectBuilder {

    public TokyoBigLinkedSetBuilder<V> registerForGarbageMonitoring() {
        super._registerForGarbageMonitoring();
        return this;
    }

    public TokyoBigLinkedSetBuilder<V> registerForGarbageMonitoring(BigObjectRegistry registry) {
        super._registerForGarbageMonitoring(registry);
        return this;
    }

    public TokyoBigLinkedSetBuilder<V> setScratchDirectory(Path scratchDirectory) {
        super._setScratchDirectory(scratchDirectory);
        return this;
    }
    
    public <V2> TokyoBigLinkedSetBuilder<V2> setValueType(Class<V2> valueType) {
        super._setKeyType(valueType);
        return (TokyoBigLinkedSetBuilder<V2>)this;
    }

    public <V2> TokyoBigLinkedSetBuilder<V2> setValueType(Class<V2> valueType, Comparator<V2> valueComparator) {
        super._setKeyType(valueType, valueComparator);
        return (TokyoBigLinkedSetBuilder<V2>)this;
    }

    public <V2> TokyoBigLinkedSetBuilder<V2> setValueType(Class<V2> valueType, ByteCodec<V2> valueCodec) {
        super._setKeyType(valueType, valueCodec);
        return (TokyoBigLinkedSetBuilder<V2>)this;
    }

    public <V2> TokyoBigLinkedSetBuilder<V2> setValueType(Class<V2> valueType, ByteCodec<V2> valueCodec, Comparator<V2> valueComparator) {
        super._setKeyType(valueType, valueCodec, valueComparator);
        return (TokyoBigLinkedSetBuilder<V2>)this;
    }
    
    public TokyoBigLinkedSet<V> build() {
        final UUID id = UUID.randomUUID();
        final Path dir = BigMapHelper.resolveScratchDirectory(this.scratchDirectory, false, id, "biglinkedset-tokyo");

        final ByteCodec<Integer> integerByteCodec = ByteCodecs.integerCodec();
        final Comparator<Integer> integerComparator = Comparators.autoComparator(Integer.class);
        final TokyoBigMap<V,None> dataMap = new TokyoBigMap<>(UUID.randomUUID(), dir, "data", (ByteCodec<V>)this.keyCodec, (Comparator<V>)this.keyComparator, ByteCodecs.noneCodec());
        final TokyoBigMap<Integer,V> insertOrderToKeyMap = new TokyoBigMap<>(UUID.randomUUID(), dir, "i2k", integerByteCodec, integerComparator, (ByteCodec<V>)this.keyCodec);
        final TokyoBigMap<V,Integer> keyToInsertOrderMap = new TokyoBigMap<>(UUID.randomUUID(), dir, "k2i", (ByteCodec<V>)this.keyCodec, (Comparator<V>)this.keyComparator, integerByteCodec);

        final TokyoBigLinkedMap<V,None> map = new TokyoBigLinkedMap<>(id, dir, false, dataMap, insertOrderToKeyMap, keyToInsertOrderMap);

        final TokyoBigLinkedSet<V> set = new TokyoBigLinkedSet<>(map);
        set.setListener(this.registry);
        set.open();
        return set;
    }

}