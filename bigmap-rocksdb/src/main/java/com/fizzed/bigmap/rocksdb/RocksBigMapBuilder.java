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

import com.fizzed.bigmap.impl.AbstractBigMapBuilder;
import com.fizzed.bigmap.impl.BigMapHelper;

import java.nio.file.Path;
import java.util.UUID;

public class RocksBigMapBuilder<K,V> extends AbstractBigMapBuilder<K,V,RocksBigMapBuilder<K,V>> {

    public RocksBigMap<K,V> build() {
        final UUID id = UUID.randomUUID();
        final Path dir = BigMapHelper.resolveScratchPath(this.scratchDirectory, false, id, "bigmap-rocks");

        final RocksBigMap<K,V> map = new RocksBigMap<>(id, dir, this.keyCodec, this.keyComparator, this.valueCodec);
        map.setListener(this.registry);
        map.open();
        return map;
    }

}