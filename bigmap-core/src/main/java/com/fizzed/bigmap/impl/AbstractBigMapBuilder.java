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
package com.fizzed.bigmap.impl;

import com.fizzed.bigmap.BigObjectRegistry;
import com.fizzed.bigmap.ByteCodec;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.Objects;

import static com.fizzed.bigmap.ByteCodecs.resolveCodec;
import static com.fizzed.bigmap.Comparators.autoComparator;

abstract public class AbstractBigMapBuilder<K,V,T> extends AbstractBigObjectBuilder<T> {

    protected Class<K> keyClass;
    protected Class<V> valueClass;
    protected ByteCodec<K> keyCodec;
    protected Comparator<K> keyComparator;
    protected ByteCodec<V> valueCodec;

    public T setKeyType(Class<K> keyType) {
        this.setKeyType(keyType, resolveCodec(keyType));
        return (T)this;
    }

    public T setKeyType(Class<K> keyType, Comparator<K> keyComparator) {
        this.setKeyType(keyType, resolveCodec(keyType), keyComparator);
        return (T)this;
    }

    public T setKeyType(Class<K> keyType, ByteCodec<K> keyCodec) {
        this.setKeyType(keyType, keyCodec, autoComparator(keyType));
        return (T)this;
    }

    public T setKeyType(Class<K> keyType, ByteCodec<K> keyCodec, Comparator<K> keyComparator) {
        Objects.requireNonNull(keyType, "keyType was null");
        Objects.requireNonNull(keyCodec, "keyCodec was null");
        // keyComparator is optional
        this.keyClass = keyType;
        this.keyCodec = keyCodec;
        this.keyComparator = keyComparator;
        return (T)this;
    }

    public T setValueType(Class<V> valueType) {
        this.setValueType(valueType, resolveCodec(valueType));
        return (T)this;
    }

    public T setValueType(Class<V> valueType, ByteCodec<V> valueCodec) {
        this.valueClass = valueType;
        this.valueCodec = valueCodec;
        return (T)this;
    }

}