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
import java.util.Set;
import java.util.Map.Entry;

public class BigMapKeySet<K,V> implements Set<K> {

    private final BigMap<K,V> map;

    public BigMapKeySet(BigMap<K,V> map) {
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
    public boolean remove(Object key) {
        return this.map.remove(key) != null;
    }

    @Override
    public boolean contains(Object key) {
        return this.map.containsKey(key);
    }

    @Override
    public void clear() {
        this.map.clear();
    }

    public Iterator<K> iterator() {
        final Iterator<Entry<K,V>> iterator = this.map.forwardIterator();

        return new Iterator<K>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public K next() {
                final Entry<K,V> entry = iterator.next();

                return entry.getKey();
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
    public boolean add(K e) {
        throw new UnmodifiableSetException();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean addAll(Collection<? extends K> c) {
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