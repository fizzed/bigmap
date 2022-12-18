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
import com.fizzed.bigmap.impl.AbstractBigMapBuilder;
import com.fizzed.bigmap.impl.BigMapHelper;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;

public class TokyoBigLinkedMapBuilder<K,V> extends AbstractBigMapBuilder<K,V,TokyoBigLinkedMapBuilder<K,V>> {

    public TokyoBigLinkedMap<K,V> build() {
        final UUID id = UUID.randomUUID();
        final Path path = BigMapHelper.resolveScratchPath(this.scratchDirectory, false, id, "biglinkedmap-tokyo");
        // take the path, append the map name, then the extension tokyo needs
        final Path dataFile = BigMapHelper.appendFileName(path, this.name, ".tcb");
        final Path i2kFile = BigMapHelper.appendFileName(path, this.name, "i2k", ".tcb");
        final Path k2iFile = BigMapHelper.appendFileName(path, this.name, "k2i", ".tcb");

        final ByteCodec<Integer> integerByteCodec = ByteCodecs.integerCodec();
        final Comparator<Integer> integerComparator = Comparators.autoComparator(Integer.class);
        final TokyoBigMap<K,V> dataMap = new TokyoBigMap<>(UUID.randomUUID(), dataFile, this.keyCodec, this.keyComparator, this.valueCodec);
        final TokyoBigMap<Integer,K> insertOrderToKeyMap = new TokyoBigMap<>(UUID.randomUUID(), i2kFile, integerByteCodec, integerComparator, this.keyCodec);
        final TokyoBigMap<K,Integer> keyToInsertOrderMap = new TokyoBigMap<>(UUID.randomUUID(), k2iFile, this.keyCodec, this.keyComparator, integerByteCodec);

        final TokyoBigLinkedMap<K,V> map = new TokyoBigLinkedMap<>(id, dataFile, false, dataMap, insertOrderToKeyMap, keyToInsertOrderMap);
        map.setListener(this.registry);
        map.open();
        return map;
    }

}