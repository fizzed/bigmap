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
import com.fizzed.bigmap.impl.AbstractBigMapBuilder;
import com.fizzed.bigmap.impl.BigMapHelper;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.UUID;

public class LevelBigLinkedMapBuilder<K,V> extends AbstractBigMapBuilder<K,V,LevelBigLinkedMapBuilder<K,V>> {

    public LevelBigLinkedMap<K,V> build() {
        final UUID id = UUID.randomUUID();
        final Path dir = BigMapHelper.resolveScratchPath(this.scratchDirectory, false, id, "biglinkedmap-level");

        // we need 3 subdir paths
        final Path dataDir = dir.resolve("data");
        final Path i2kDir = dir.resolve("i2k");
        final Path k2iDir = dir.resolve("k2i");

        final ByteCodec<Integer> integerByteCodec = ByteCodecs.integerCodec();
        final Comparator<Integer> integerComparator = Comparators.autoComparator(Integer.class);
        final LevelBigMap<K,V> dataMap = new LevelBigMap<>(UUID.randomUUID(), dataDir, this.keyCodec, this.keyComparator, this.valueCodec);
        final LevelBigMap<Integer,K> insertOrderToKeyMap = new LevelBigMap<>(UUID.randomUUID(), i2kDir, integerByteCodec, integerComparator, this.keyCodec);
        final LevelBigMap<K,Integer> keyToInsertOrderMap = new LevelBigMap<>(UUID.randomUUID(), k2iDir, this.keyCodec, this.keyComparator, integerByteCodec);

        final LevelBigLinkedMap<K,V> map = new LevelBigLinkedMap<>(id, dir, false, dataMap, insertOrderToKeyMap, keyToInsertOrderMap);
        map.setListener(this.registry);
        map.open();
        return map;
    }

}