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

import com.fizzed.bigmap.impl.BigMapEntrySet;
import com.fizzed.bigmap.impl.BigMapKeySet;
import com.fizzed.bigmap.impl.BigMapValueCollection;
import com.fizzed.bigmap.impl.MapMutableValue;

import java.io.Closeable;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface BigMap<K,V> extends Map<K,V>, BigObject {

    ByteCodec<K> getKeyCodec();

    Comparator<K> getKeyComparator();

    ByteCodec<V> getValueCodec();

    @Override
    default boolean isEmpty() {
        return this.size() <= 0;
    }

    @Override
    default boolean containsValue(Object value) {
        this.checkIfClosed();

        throw new BigMapNonScalableException("Poor performance for checking if map contains a value. Method unsupported.");
    }

    /**
     * IF YOU DO NOT NEED THE RETURN VALUE, PLEASE USE {@link #set(Object, Object)}
     *
     * With most implementations of a bigmap, its two operations to put a value AND return back the old value. In
     * most cases, you ignore the returned value and it just costs more time/effort than is needed.
     *
     * Otherwise, this method works identical to a standard Map implementation.
     */
    @Override
    V put(K key, V value);

    /**
     * Typically, this is more efficient than a {@link #put(Object, Object)} since the old value is not returned.
     * @param key
     * @param value
     */
    void set(K key, V value);

    /**
     * IF YOU DO NOT NEED THE RETURN VALUE, PLEASE USE {@link #delete(Object)}
     *
     * With most implementations of a bigmap, its two operations to remove a value AND return back the old value. In
     * most cases, you ignore the returned value and it just costs more time/effort than is needed.
     *
     * Otherwise, this method works identical to a standard Map implementation.
     */
    @Override
    V remove(Object key);

    /**
     * Typically, this is more efficient than a {@link #put(Object, Object)} since the old value is not returned.
     * @param key
     */
    void delete(K key);

    /**
     * BE CAREFUL WITH MODIFYING ANY RETURN VALUE FROM THIS METHOD. Unlike traditional in-memory maps, the BigMap will
     * not know you modified the return value, unless you also "put" the value back in, so it can be serialized again
     * into bytes.  If you plan on modifying the value, we suggest the {@link #getMutable(Object)} method to make applying
     * the changes happen automatically with a try-with-resources block.
     *
     * Otherwise, this method works identical to a standard Map implementation.
     */
    @Override
    V get(Object key);

    /**
     * Gets a value from the map, wrapped in a <code>MutableValue</code> instance to make it easier to make changes to
     * the returned value, and then have them auto-committed back to the map when the <code>MutableValue</code> is closed.
     * We suggest using a try-with-resources block to simplify your code.
     * <code>
     * try (MutableValue<MyObject> value = map.get("mykey")) {
     *
     *     // value.get() will get the real value
     *     // make any changes to the return value, you need
     *
     * } // once closed, the value will be "put" back into the map again for you to persist the new bytes
     * </code>
     *
     * @param key the key whose associated value is to be returned
     * @return The value associated with the key wrapped inside a <code>MutableValue</code>. If the key does not exist
     *      the map, the MutableValue will still be returned, but will have a null value inside of it.
     */
    default MutableValue<V> getMutable(K key) {
        final V value = this.get(key);
        return new MapMutableValue<>(this, key, value);
    }

    /**
     * BE CAREFUL WITH MODIFYING ANY RETURN VALUE FROM THIS METHOD. Unlike traditional in-memory maps, the BigMap will
     * not know you modified the return value, unless you also "put" the value back in, so it can be serialized again
     * into bytes.  If you plan on modifying the value, we suggest the #computeIfAbsentMutable method to make applying
     * the changes happen automatically with a try-with-resources block.
     *
     * Otherwise, this method works identical to a standard Map implementation.
     */
    @Override
    default V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        return Map.super.computeIfAbsent(key, mappingFunction);
    }

    default MutableValue<V> computeIfAbsentMutable(K key, Function<? super K, ? extends V> mappingFunction) {
        final V value = Map.super.computeIfAbsent(key, mappingFunction);
        return new MapMutableValue<>(this, key, value);
    }

    @Override
    default V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return Map.super.computeIfPresent(key, remappingFunction);
    }

    /**
     * BE CAREFUL WITH MODIFYING ANY RETURN VALUE FROM THIS METHOD. Unlike traditional in-memory maps, the BigMap will
     * not know you modified the return value, unless you also "put" the value back in, so it can be serialized again
     * into bytes.  If you plan on modifying the value, we suggest the #computeIfAbsentMutable method to make applying
     * the changes happen automatically with a try-with-resources block.
     *
     * Otherwise, this method works identical to a standard Map implementation.
     */
    default MutableValue<V> computeIfPresentMutable(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        final V value = Map.super.computeIfPresent(key, remappingFunction);
        return new MapMutableValue<>(this, key, value);
    }

    @Override
    default void putAll(Map<? extends K, ? extends V> m) {
        if (m != null) {
            m.forEach((k, v) -> {
                this.set(k, v);
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