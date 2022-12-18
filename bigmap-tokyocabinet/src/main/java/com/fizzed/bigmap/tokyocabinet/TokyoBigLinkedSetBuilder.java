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

import com.fizzed.bigmap.ByteCodec;
import com.fizzed.bigmap.ByteCodecs;
import com.fizzed.bigmap.Comparators;
import com.fizzed.bigmap.impl.AbstractBigSetBuilder;
import com.fizzed.bigmap.impl.BigMapHelper;
import com.fizzed.bigmap.impl.None;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;

public class TokyoBigLinkedSetBuilder<V> extends AbstractBigSetBuilder<V,TokyoBigLinkedSetBuilder<V>> {

    public TokyoBigLinkedSet<V> build() {
        final UUID id = UUID.randomUUID();
        final Path path = BigMapHelper.resolveScratchPath(this.scratchDirectory, false, id, "biglinkedset-tokyo");
        // take the path, append the map name, then the extension tokyo needs
        final Path dataFile = BigMapHelper.appendFileName(path, this.name, ".tcb");
        final Path i2kFile = BigMapHelper.appendFileName(path, this.name, "i2k", ".tcb");
        final Path k2iFile = BigMapHelper.appendFileName(path, this.name, "k2i", ".tcb");

        final ByteCodec<Integer> integerByteCodec = ByteCodecs.integerCodec();
        final Comparator<Integer> integerComparator = Comparators.autoComparator(Integer.class);
        final TokyoBigMap<V,None> dataMap = new TokyoBigMap<>(UUID.randomUUID(), dataFile, this.valueCodec, this.valueComparator, ByteCodecs.noneCodec());
        final TokyoBigMap<Integer,V> insertOrderToKeyMap = new TokyoBigMap<>(UUID.randomUUID(), i2kFile, integerByteCodec, integerComparator, this.valueCodec);
        final TokyoBigMap<V,Integer> keyToInsertOrderMap = new TokyoBigMap<>(UUID.randomUUID(), k2iFile, this.valueCodec, this.valueComparator, integerByteCodec);

        final TokyoBigLinkedMap<V,None> map = new TokyoBigLinkedMap<>(id, dataFile, false, dataMap, insertOrderToKeyMap, keyToInsertOrderMap);

        final TokyoBigLinkedSet<V> set = new TokyoBigLinkedSet<>(map);
        set.setListener(this.registry);
        set.open();
        return set;
    }

}