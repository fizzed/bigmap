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

import com.fizzed.bigmap.BigMap;

import java.util.*;

import static com.fizzed.bigmap.impl.BigMapHelper.sizeOf;

public interface ByteArrayBigMap<K,V> extends BigMap<K,V> {

    @Override
    default V get(Object key) {
        this.checkIfClosed();

        Objects.requireNonNull(key, "key was null");

        final byte[] keyBytes = this.getKeyCodec().serialize((K)key);

        final byte[] valueBytes = this._get(keyBytes);

        return this.getValueCodec().deserialize(valueBytes);
    }

    byte[] _get(byte[] keyBytes);

    @Override
    default V put(K key, V value) {
        this.checkIfClosed();

        Objects.requireNonNull(key, "key was null");
        Objects.requireNonNull(value, "value was null");

        final byte[] keyBytes = this.getKeyCodec().serialize(key);
        final byte[] valueBytes = this.getValueCodec().serialize(value);

        final byte[] oldValueBytes = this._put(keyBytes, valueBytes);

        // existing entry
        if (oldValueBytes != null) {
            this._entryRemoved(0, sizeOf(oldValueBytes));
            this._entryAdded(0, sizeOf(valueBytes));
            return this.getValueCodec().deserialize(oldValueBytes);
        }
        else {
            // new entry
            this._entryAdded(sizeOf(keyBytes), sizeOf(valueBytes));
            return null;
        }
    }

    byte[] _put(byte[] keyBytes, byte[] valueBytes);

    @Override
    default boolean containsKey(Object key) {
        this.checkIfClosed();

        byte[] keyBytes = this.getKeyCodec().serialize((K)key);

        return this._containsKey(keyBytes);
    }

    boolean _containsKey(byte[] keyBytes);

    @Override
    default V remove(Object key) {
        this.checkIfClosed();

        Objects.requireNonNull(key, "key was null");

        byte[] keyBytes = this.getKeyCodec().serialize((K)key);

        byte[] valueBytes = this._remove(keyBytes);

        if (valueBytes != null) {
            this._entryRemoved(sizeOf(keyBytes), sizeOf(valueBytes));
        }

        return this.getValueCodec().deserialize(valueBytes);
    }

    byte[] _remove(byte[] keyBytes);

    void _entryAdded(long keyByteSize, long valueByteSize);

    void _entryRemoved(long keyByteSize, long valueByteSize);

    @Override
    default Iterator<Entry<K,V>> forwardIterator() {
        final Iterator<KeyValueBytes> iterator = this._forwardIterator();

        return new Iterator<Entry<K, V>>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public Entry<K,V> next() {
                final KeyValueBytes kvb = iterator.next();

                return new Entry<K,V>() {
                    @Override
                    public K getKey() {
                        return ByteArrayBigMap.this.getKeyCodec().deserialize(kvb.getKey());
                    }

                    @Override
                    public V getValue() {
                        return ByteArrayBigMap.this.getValueCodec().deserialize(kvb.getValue());
                    }

                    @Override
                    public V setValue(V value) {
                        final byte[] valueBytes = ByteArrayBigMap.this.getValueCodec().serialize(value);

                        final byte[] oldValueBytes = ByteArrayBigMap.this._put(kvb.getKey(), valueBytes);

                        if (oldValueBytes != null) {
                            return ByteArrayBigMap.this.getValueCodec().deserialize(oldValueBytes);
                        }

                        return null;
                    }
                };
            }
        };
    }

    Iterator<KeyValueBytes> _forwardIterator();

}