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

import com.fizzed.bigmap.MutableValue;

import java.io.IOException;
import java.util.Map;

public class MapMutableValue<K,V> implements MutableValue<V> {

    private final Map<K,V> map;
    private final K key;
    private V value;
    private final boolean wasInitiallyNull;
    private boolean closed;

    public MapMutableValue(Map<K,V> map, K key, V value) {
        this.map = map;
        this.key = key;
        this.value = value;
        this.wasInitiallyNull = value == null;
    }

    @Override
    protected void finalize() {
        if (!this.closed) {
            System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            System.err.println("MutableValue was not closed! Please check your code for improper usage!");
            System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        }
    }

    @Override
    public boolean isPresent() {
        return this.value != null;
    }

    @Override
    public V get() {
        return this.value;
    }

    @Override
    public void set(V value) {
        this.value = value;
    }

    @Override
    public void close() throws RuntimeException {
        this.closed = true;

        // now, if the value is/was null
        if (this.wasInitiallyNull && value == null) {
            // there's nothing to actually do
        } else if (value == null) {
            // this is essentially a delete of the key, since null values cannot be placed into BigMaps
            this.map.remove(this.key);
        } else {
            // otherwise, put the value back in
            this.map.put(this.key, this.value);
        }
    }

}