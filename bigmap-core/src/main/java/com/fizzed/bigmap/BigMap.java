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
import java.util.*;

import static com.fizzed.bigmap.BigMapHelper.sizeOf;

public interface BigMap<K,V> extends Map<K,V>, Closeable {

    void checkIfClosed();

    ByteCodec<K> getKeyCodec();

    ByteCodec<V> getValueCodec();

    @Override
    default boolean containsValue(Object value) {
        this.checkIfClosed();

        throw new BigMapNonScalableException("Poor performance for checking if map contains a value. Method unsupported.");
    }

    @Override
    default void putAll(Map<? extends K, ? extends V> m) {
        if (m != null) {
            m.forEach((k, v) -> {
                this.put(k, v);
            });
        }
    }

    Iterator<Entry<K,V>> forwardIterator();

    @Override
    default Collection<V> values() {
        this.checkIfClosed();

        return new BigMapValueCollection<>(this);
    }

    @Override
    default Set<K> keySet() {
        this.checkIfClosed();

        return new BigMapKeySet<>(this);
    }

    @Override
    default Set<Entry<K,V>> entrySet() {
        this.checkIfClosed();

        return new BigMapEntrySet<>(this);
    }

}