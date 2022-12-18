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
import com.fizzed.bigmap.impl.AbstractBigSetBuilder;
import com.fizzed.bigmap.impl.BigMapHelper;
import com.fizzed.bigmap.impl.None;

import java.nio.file.Path;
import java.util.Objects;
import java.util.UUID;

public class TokyoBigSetBuilder<V> extends AbstractBigSetBuilder<V,TokyoBigSetBuilder<V>> {

    public TokyoBigSet<V> build() {
        final UUID id = UUID.randomUUID();
        final Path path = BigMapHelper.resolveScratchPath(this.scratchDirectory, false, id, "bigset-tokyo");
        // take the path, append the map name, then the extension tokyo needs
        final Path file = BigMapHelper.appendFileName(path, this.name, ".tcb");

        final TokyoBigMap<V,None> map = new TokyoBigMap<>(id, file, this.valueCodec, this.valueComparator, ByteCodecs.noneCodec());

        final TokyoBigSet<V> set = new TokyoBigSet<>(map);
        set.setListener(this.registry);
        set.open();
        return set;
    }

}