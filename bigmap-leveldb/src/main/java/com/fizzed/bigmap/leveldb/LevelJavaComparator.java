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

import org.iq80.leveldb.DBComparator;
import com.fizzed.bigmap.ByteCodec;
import java.util.Comparator;
import java.util.Objects;

public class LevelJavaComparator<T> implements DBComparator {

    private final ByteCodec<T> codec;
    private final Comparator<T> comparator;

    public LevelJavaComparator(ByteCodec<T> codec, Comparator<T> comparator) {
        Objects.requireNonNull(codec, "codec was null");
        Objects.requireNonNull(comparator, "comparator was null");
        this.codec = codec;
        this.comparator = comparator;
    }
    
    @Override
    public String name() {
        return "jvm-comparator";
    }

    @Override
    public byte[] findShortestSeparator(byte[] start, byte[] limit) {
        return start;
    }

    @Override
    public byte[] findShortSuccessor(byte[] key) {
        return key;
    }

    @Override
    public int compare(byte[] o1, byte[] o2) {
        T t1 = this.codec.deserialize(o1);
        T t2 = this.codec.deserialize(o2);
        return this.comparator.compare(t1, t2);
    }
    
}