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

import com.fizzed.bigmap.ByteCodecs;
import com.fizzed.bigmap.impl.AbstractBigSetBuilder;
import com.fizzed.bigmap.impl.BigMapHelper;
import com.fizzed.bigmap.impl.None;

import java.nio.file.Path;
import java.util.UUID;

public class LevelBigSetBuilder<V> extends AbstractBigSetBuilder<V,LevelBigSetBuilder<V>> {

    public LevelBigSet<V> build() {
        final UUID id = UUID.randomUUID();
        final Path dir = BigMapHelper.resolveScratchPath(this.scratchDirectory, false, id, "bigset-level");

        final LevelBigMap<V,None> map = new LevelBigMap<>(id, dir, this.valueCodec, this.valueComparator, ByteCodecs.noneCodec());

        final LevelBigSet<V> set = new LevelBigSet<>(map);
        set.setListener(this.registry);
        set.open();
        return set;
    }

}