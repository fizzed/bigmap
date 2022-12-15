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
package com.fizzed.bigmap;

import java.io.Closeable;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Comparator;
import java.util.Set;

public interface BigSet<V> extends Set<V>, BigObject {

    ByteCodec<V> getValueCodec();

    Comparator<V> getValueComparator();

    long getValueByteSize();

    @Override
    default boolean containsAll(Collection<?> c) {
        if (c != null) {
            for (Object v : c) {
                if (!this.contains(v)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    default boolean addAll(Collection<? extends V> c) {
        if (c != null) {
            for (V v : c) {
                this.add(v);
            }
        }
        return true;
    }

    @Override
    default boolean retainAll(Collection<?> c) {
        throw new BigMapNonScalableException("Not scalable to bulk retain values");
    }

    @Override
    default boolean removeAll(Collection<?> c) {
        if (c != null) {
            for (Object v : c) {
                this.remove(v);
            }
        }
        return true;
    }

}