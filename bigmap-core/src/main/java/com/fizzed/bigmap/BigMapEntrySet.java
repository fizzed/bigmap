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

import javax.print.attribute.UnmodifiableSetException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

public class BigMapEntrySet<K,V> implements Set<Entry<K,V>> {

    private final BigMap<K,V> map;

    public BigMapEntrySet(BigMap<K,V> map) {
        this.map = map;
    }

    @Override
    public int size() {
        return this.map.size();
    }

    @Override
    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        this.map.clear();
    }

    public Iterator<Entry<K,V>> iterator() {
        final Iterator<KeyValueBytes> iterator = this.map._forwardIterator();

        return new Iterator<Entry<K,V>>() {
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
                        return map.getKeyCodec().deserialize(kvb.getKey());
                    }

                    @Override
                    public V getValue() {
                        return map.getValueCodec().deserialize(kvb.getValue());
                    }

                    @Override
                    public V setValue(V value) {
                        byte[] valueBytes = map.getValueCodec().serialize(value);

                        byte[] oldValueBytes = map._put(kvb.getKey(), valueBytes);

                        return map.getValueCodec().deserialize(oldValueBytes);
                    }
                };
            }
        };
    }

    @Override
    public Object[] toArray() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean add(Entry<K,V> e) {
        throw new UnmodifiableSetException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnmodifiableSetException();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends Entry<K,V>> c) {
        throw new UnmodifiableSetException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnmodifiableSetException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnmodifiableSetException();
    }
    
}