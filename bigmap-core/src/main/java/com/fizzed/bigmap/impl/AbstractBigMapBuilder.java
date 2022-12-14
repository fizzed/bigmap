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

import java.nio.file.Path;
import java.util.Comparator;
import java.util.Objects;

import static com.fizzed.bigmap.ByteCodecs.autoCodec;
import static com.fizzed.bigmap.Comparators.autoComparator;

public class AbstractBigMapBuilder {

    protected Path scratchDirectory;
    protected Class<?> keyClass;
    protected Class<?> valueClass;
    protected ByteCodec<?> keyCodec;
    protected Comparator<?> keyComparator;
    protected ByteCodec<?> valueCodec;

    public AbstractBigMapBuilder() {
    }

    protected void _setScratchDirectory(Path scratchDirectory) {
        this.scratchDirectory = scratchDirectory;
    }

    protected void _setKeyType(Class<?> keyType) {
        this._setKeyType(keyType, autoCodec(keyType));
    }

    protected void _setKeyType(Class<?> keyType, Comparator<?> keyComparator) {
        this._setKeyType(keyType, autoCodec(keyType), keyComparator);
    }

    protected void _setKeyType(Class<?> keyType, ByteCodec<?> keyCodec) {
        this._setKeyType(keyType, keyCodec, autoComparator(keyType));
    }

    protected void _setKeyType(Class<?> keyType, ByteCodec<?> keyCodec, Comparator<?> keyComparator) {
        Objects.requireNonNull(keyType, "keyType was null");
        Objects.requireNonNull(keyCodec, "keyCodec was null");
        Objects.requireNonNull(keyComparator, "keyComparator was null");
        this.keyClass = keyType;
        this.keyCodec = keyCodec;
        this.keyComparator = keyComparator;
    }

    protected void _setValueType(Class<?> valueType) {
        this._setValueType(valueType, autoCodec(valueType));
    }

    protected void _setValueType(Class<?> valueType, ByteCodec<?> valueCodec) {
        this.valueClass = valueType;
        this.valueCodec = valueCodec;
    }

}