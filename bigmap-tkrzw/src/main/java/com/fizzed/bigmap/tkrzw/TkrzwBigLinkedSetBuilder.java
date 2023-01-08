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
package com.fizzed.bigmap.tkrzw;

import com.fizzed.bigmap.ByteCodec;
import com.fizzed.bigmap.ByteCodecs;
import com.fizzed.bigmap.Comparators;
import com.fizzed.bigmap.impl.AbstractBigSetBuilder;
import com.fizzed.bigmap.impl.BigMapHelper;
import com.fizzed.bigmap.impl.None;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.UUID;

public class TkrzwBigLinkedSetBuilder<V> extends AbstractBigSetBuilder<V, TkrzwBigLinkedSetBuilder<V>> {

    public TkrzwBigLinkedSet<V> build() {
        final UUID id = UUID.randomUUID();
        final Path path = BigMapHelper.resolveScratchPath(this.scratchDirectory, false, id, "biglinkedset-tokyo");
        // take the path, append the map name, then the extension tokyo needs
        final Path dataFile = BigMapHelper.appendFileName(path, this.name, ".tkt");
        final Path i2kFile = BigMapHelper.appendFileName(path, this.name, "i2k", ".tkt");
        final Path k2iFile = BigMapHelper.appendFileName(path, this.name, "k2i", ".tkt");

        final ByteCodec<Integer> integerByteCodec = ByteCodecs.integerCodec();
        final Comparator<Integer> integerComparator = Comparators.autoComparator(Integer.class);
        final TkrzwBigMap<V,None> dataMap = new TkrzwBigMap<>(UUID.randomUUID(), dataFile, this.valueCodec, this.valueComparator, ByteCodecs.noneCodec());
        final TkrzwBigMap<Integer,V> insertOrderToKeyMap = new TkrzwBigMap<>(UUID.randomUUID(), i2kFile, integerByteCodec, integerComparator, this.valueCodec);
        final TkrzwBigMap<V,Integer> keyToInsertOrderMap = new TkrzwBigMap<>(UUID.randomUUID(), k2iFile, this.valueCodec, this.valueComparator, integerByteCodec);

        final TkrzwBigLinkedMap<V,None> map = new TkrzwBigLinkedMap<>(id, dataFile, false, dataMap, insertOrderToKeyMap, keyToInsertOrderMap);

        final TkrzwBigLinkedSet<V> set = new TkrzwBigLinkedSet<>(map);
        set.setListener(this.registry);
        set.open();
        return set;
    }

}