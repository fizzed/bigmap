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

import java.util.*;

import static com.fizzed.bigmap.BigMapHelper.sizeOf;

public interface BigSortedMap<K,V> extends BigMap<K,V>, SortedMap<K,V> {

    @Override
    default Comparator<? super K> comparator() {
        return this.getKeyComparator();
    }

    @Override
    default Set<K> keySet() {
        return BigMap.super.keySet();
    }

    @Override
    default Set<Entry<K, V>> entrySet() {
        return BigMap.super.entrySet();
    }

    @Override
    default Collection<V> values() {
        return BigMap.super.values();
    }

    @Override
    default SortedMap<K,V> subMap(K fromKey, K toKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    default SortedMap<K,V> headMap(K toKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    default SortedMap<K,V> tailMap(K fromKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    default K firstKey() {
        return this.keySet().iterator().next();
    }

    @Override
    default K lastKey() {
        throw new UnsupportedOperationException();
    }

}