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

import com.fizzed.bigmap.ByteCodec;
import com.fizzed.bigmap.ByteCodecs;
import com.fizzed.bigmap.Comparators;
import com.fizzed.bigmap.impl.AbstractBigSetBuilder;
import com.fizzed.bigmap.impl.BigMapHelper;
import com.fizzed.bigmap.impl.None;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.UUID;

public class LevelBigLinkedSetBuilder<V> extends AbstractBigSetBuilder<V,LevelBigLinkedSetBuilder<V>> {

    public LevelBigLinkedSet<V> build() {
        final UUID id = UUID.randomUUID();
        final Path dir = BigMapHelper.resolveScratchPath(this.scratchDirectory, false, id, "biglinkedset-level");

        // we need 3 subdir paths
        final Path dataDir = dir.resolve("data");
        final Path i2kDir = dir.resolve("i2k");
        final Path k2iDir = dir.resolve("k2i");

        final ByteCodec<Integer> integerByteCodec = ByteCodecs.integerCodec();
        final Comparator<Integer> integerComparator = Comparators.autoComparator(Integer.class);
        final LevelBigMap<V,None> dataMap = new LevelBigMap<>(UUID.randomUUID(), dataDir, this.valueCodec, this.valueComparator, ByteCodecs.noneCodec());
        final LevelBigMap<Integer,V> insertOrderToKeyMap = new LevelBigMap<>(UUID.randomUUID(), i2kDir, integerByteCodec, integerComparator, this.valueCodec);
        final LevelBigMap<V,Integer> keyToInsertOrderMap = new LevelBigMap<>(UUID.randomUUID(), k2iDir, this.valueCodec, this.valueComparator, integerByteCodec);

        final LevelBigLinkedMap<V,None> map = new LevelBigLinkedMap<>(id, dir, false, dataMap, insertOrderToKeyMap, keyToInsertOrderMap);

        final LevelBigLinkedSet<V> set = new LevelBigLinkedSet<>(map);
        set.setListener(this.registry);
        set.open();
        return set;
    }

}