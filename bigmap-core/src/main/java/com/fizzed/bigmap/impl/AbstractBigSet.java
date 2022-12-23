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

import com.fizzed.bigmap.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Iterator;
import java.util.UUID;

abstract public class AbstractBigSet<V> implements BigSet<V> {

    final private BigMap<V,None> map;
    protected BigObjectListener listener;

    public AbstractBigSet(
            BigMap<V,None> map) {

        this.map = map;
    }

    @Override
    public BigObjectListener getListener() {
        return this.listener;
    }

    @Override
    public void setListener(BigObjectListener listener) {
        this.listener = listener;
    }

    public UUID getId() {
        return this.map.getId();
    }

    @Override
    public Path getPath() {
        return this.map.getPath();
    }

    @Override
    public boolean isPersistent() {
        return this.map.isPersistent();
    }

    @Override
    final public BigObjectCloser getCloser() {
        return this.map.getCloser();
    }

    @Override
    final public boolean isClosed() {
        return this.map.isClosed();
    }

    @Override
    public void open() {
        this.map.open();

        if (this.listener != null) {
            this.listener.onOpened(this);
        }
    }

    @Override
    final synchronized public void close() throws IOException {
        this.map.close();

        if (this.listener != null) {
            this.listener.onClosed(this);
        }
    }

    @Override
    public ByteCodec<V> getValueCodec() {
        return this.map.getKeyCodec();
    }

    @Override
    public Comparator<V> getValueComparator() {
        return this.map.getKeyComparator();
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
        return this.map.containsKey(o);
    }

    @Override
    public Iterator<V> iterator() {
//        return this.map.keySet().iterator();
        final Iterator<V> it = this.map.keySet().iterator();
        // NOTE: for auto closing of objects, its critical we maintain an iterator to have a reference back to the
        // original set, so that the set doesn't qualify for garbage collection in the case where the set is actually
        // no longer referenced, but someone is still iterating over its iterator!
        return new Iterator<V>() {
            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public V next() {
                return it.next();
            }
        };
    }

    @Override
    public Object[] toArray() {
        return this.map.keySet().toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return this.map.keySet().toArray(a);
    }

    @Override
    public boolean add(V o) {
        if (this.map.containsKey(o)) {
            return false;
        }
        this.map.set(o, None.NONE);
        return true;
    }

    @Override
    public boolean remove(Object o) {
        if (!this.map.containsKey(o)) {
            return false;
        }
        this.map.delete((V)o);
        return true;
    }

    @Override
    public void delete(Object o) {
        this.map.delete((V)o);
    }

    @Override
    public void clear() {
        this.map.clear();
    }

}