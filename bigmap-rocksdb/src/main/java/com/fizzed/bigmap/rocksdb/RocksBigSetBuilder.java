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

import com.fizzed.bigmap.ByteCodecs;
import com.fizzed.bigmap.impl.AbstractBigSetBuilder;
import com.fizzed.bigmap.impl.BigMapHelper;
import com.fizzed.bigmap.impl.None;

import java.nio.file.Path;
import java.util.UUID;

public class RocksBigSetBuilder<V> extends AbstractBigSetBuilder<V,RocksBigSetBuilder<V>> {

    public RocksBigSet<V> build() {
        final UUID id = UUID.randomUUID();
        final Path dir = BigMapHelper.resolveScratchPath(this.scratchDirectory, false, id, "bigset-rocks");

        final RocksBigMap<V,None> map = new RocksBigMap<>(id, dir, this.valueCodec, this.valueComparator, ByteCodecs.noneCodec());

        final RocksBigSet<V> set = new RocksBigSet<>(map);
        set.setListener(this.registry);
        set.open();
        return set;
    }

}