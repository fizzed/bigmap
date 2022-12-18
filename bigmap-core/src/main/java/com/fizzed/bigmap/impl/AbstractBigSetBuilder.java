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

import com.fizzed.bigmap.ByteCodec;

import java.util.Comparator;
import java.util.Objects;

import static com.fizzed.bigmap.ByteCodecs.resolveCodec;
import static com.fizzed.bigmap.Comparators.autoComparator;

abstract public class AbstractBigSetBuilder<V,T> extends AbstractBigObjectBuilder<T> {

    protected Class<V> valueClass;
    protected ByteCodec<V> valueCodec;
    protected Comparator<V> valueComparator;

    public T setValueType(Class<V> valueType) {
        this.setValueType(valueType, resolveCodec(valueType));
        return (T)this;
    }

    public T setValueType(Class<V> valueType, Comparator<V> valueComparator) {
        this.setValueType(valueType, resolveCodec(valueType), valueComparator);
        return (T)this;
    }

    public T setValueType(Class<V> valueType, ByteCodec<V> valueCodec) {
        this.setValueType(valueType, valueCodec, autoComparator(valueType));
        return (T)this;
    }

    public T setValueType(Class<V> valueType, ByteCodec<V> valueCodec, Comparator<V> valueComparator) {
        Objects.requireNonNull(valueType, "valueType was null");
        Objects.requireNonNull(valueCodec, "valueCodec was null");
        // valueComparator is optional
        this.valueClass = valueType;
        this.valueCodec = valueCodec;
        this.valueComparator = valueComparator;
        return (T)this;
    }

}