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

import com.fizzed.bigmap.*;
import com.fizzed.bigmap.impl.AbstractBigObjectBuilder;
import com.fizzed.bigmap.impl.BigMapHelper;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.UUID;

public class TokyoBigLinkedMapBuilder<K,V> extends AbstractBigObjectBuilder {

    public TokyoBigLinkedMapBuilder<K,V> registerForGarbageMonitoring() {
        super._registerForGarbageMonitoring();
        return this;
    }

    public TokyoBigLinkedMapBuilder<K,V> registerForGarbageMonitoring(BigObjectRegistry registry) {
        super._registerForGarbageMonitoring(registry);
        return this;
    }

    public TokyoBigLinkedMapBuilder<K,V> setScratchDirectory(Path scratchDirectory) {
        super._setScratchDirectory(scratchDirectory);
        return this;
    }
    
    public <K2> TokyoBigLinkedMapBuilder<K2,V> setKeyType(Class<K2> keyType) {
        super._setKeyType(keyType);
        return (TokyoBigLinkedMapBuilder<K2,V>)this;
    }

    public <K2> TokyoBigLinkedMapBuilder<K2,V> setKeyType(Class<K2> keyType, Comparator<K2> keyComparator) {
        super._setKeyType(keyType, keyComparator);
        return (TokyoBigLinkedMapBuilder<K2,V>)this;
    }

    public <K2> TokyoBigLinkedMapBuilder<K2,V> setKeyType(Class<K2> keyType, ByteCodec<K2> keyCodec) {
        super._setKeyType(keyType, keyCodec);
        return (TokyoBigLinkedMapBuilder<K2,V>)this;
    }

    public <K2> TokyoBigLinkedMapBuilder<K2,V> setKeyType(Class<K2> keyType, ByteCodec<K2> keyCodec, Comparator<K2> keyComparator) {
        super._setKeyType(keyType, keyCodec, keyComparator);
        return (TokyoBigLinkedMapBuilder<K2,V>)this;
    }

    public <V2> TokyoBigLinkedMapBuilder<K,V2> setValueType(Class<V2> valueType) {
        super._setValueType(valueType);
        return (TokyoBigLinkedMapBuilder<K,V2>)this;
    }

    public <V2> TokyoBigLinkedMapBuilder<K,V2> setValueType(Class<V2> valueType, ByteCodec<V2> valueCodec) {
        super._setValueType(valueType, valueCodec);
        return (TokyoBigLinkedMapBuilder<K,V2>)this;
    }
    
    public TokyoBigLinkedMap<K,V> build() {
        final UUID id = UUID.randomUUID();
        final Path dir = BigMapHelper.resolveScratchDirectory(this.scratchDirectory, false, id, "biglinkedmap-tokyo");

        final ByteCodec<Integer> integerByteCodec = ByteCodecs.integerCodec();
        final Comparator<Integer> integerComparator = Comparators.autoComparator(Integer.class);
        final TokyoBigMap<K,V> dataMap = new TokyoBigMap<>(UUID.randomUUID(), dir, "data", (ByteCodec<K>)this.keyCodec, (Comparator<K>)this.keyComparator, (ByteCodec<V>)this.valueCodec);
        final TokyoBigMap<Integer,K> insertOrderToKeyMap = new TokyoBigMap<>(UUID.randomUUID(), dir, "i2k", integerByteCodec, integerComparator, (ByteCodec<K>)this.keyCodec);
        final TokyoBigMap<K,Integer> keyToInsertOrderMap = new TokyoBigMap<>(UUID.randomUUID(), dir, "k2i", (ByteCodec<K>)this.keyCodec, (Comparator<K>)this.keyComparator, integerByteCodec);

        final TokyoBigLinkedMap<K,V> map = new TokyoBigLinkedMap<>(id, dir, false, dataMap, insertOrderToKeyMap, keyToInsertOrderMap);
        map.setListener(this.registry);
        map.open();
        return map;
    }

}