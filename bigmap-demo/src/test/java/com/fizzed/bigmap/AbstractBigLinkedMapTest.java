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

import org.junit.Test;

import java.io.IOException;
import java.util.*;

import static com.fizzed.bigmap.BigMapHelper.*;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

abstract public class AbstractBigLinkedMapTest {

    abstract public <K,V> Map<K,V> newMap(Class<K> keyType, Class<V> valueType);

    @Test
    public void putAndGet() {
        final Map<String,String> map = this.newMap(String.class, String.class);

        map.put("a", "1");

        assertThat(map, hasKey("a"));
        assertThat(map.get("a"), is("1"));
        assertThat(map.size(), is(1));
        assertThat(map.isEmpty(), is(false));

        map.remove("a");

        assertThat(map.get("a"), is(nullValue()));
        assertThat(map.size(), is(0));
        assertThat(map.isEmpty(), is(true));

        String removed = map.put("b", "2");

        assertThat(removed, is(nullValue()));
        assertThat(map.size(), is(1));

        String removed1 = map.put("b", "3");

        assertThat(removed1, is("2"));
        assertThat(map.size(), is(1));
    }

    @Test
    public void ordering() {
        final Map<Long,String> map = this.newMap(Long.class, String.class);

        map.put(5L, "5");
        map.put(1L, "1");
        map.put(3L, "3");
        map.put(2L, "2");

        assertThat(toValueList(map), hasItems("5", "1", "3", "2"));

        // replacing a value does not change its insert ordering
        map.put(5L, "5replaced");

        assertThat(toValueList(map), hasItems("5replaced", "1", "3", "2"));
    }

    @Test
    public void remove() throws IOException {
        final Map<Integer,String> map = this.newMap(Integer.class, String.class);

        map.put(4, "4");
        map.put(1, "1");
        map.put(1, "1replaced");
        map.put(2, "2");

        assertThat(map, aMapWithSize(3));
        assertThat(map.keySet().iterator().next(), is(4));
        assertThat(map.get(1), is("1replaced"));

        String removed;

        removed = map.remove(0);

        assertThat(map, aMapWithSize(3));
        assertThat(map.keySet().iterator().next(), is(4));
        assertThat(removed, is(nullValue()));

        removed = map.remove(1);

        assertThat(map, aMapWithSize(2));
        assertThat(map.keySet().iterator().next(), is(4));
        assertThat(removed, is("1replaced"));

        // try to remove it again
        removed = map.remove(1);

        assertThat(map, aMapWithSize(2));
        assertThat(removed, is(nullValue()));

        removed = map.remove(4);

        assertThat(map, aMapWithSize(1));
        assertThat(removed, is("4"));
        assertThat(map.keySet().iterator().next(), is(2));

        removed = map.remove(2);

        assertThat(map, aMapWithSize(0));
        assertThat(removed, is("2"));

        try {
            map.keySet().iterator().next();
            fail();
        } catch (NoSuchElementException e) {
            // expected
        }
    }

    @Test
    public void clear() throws IOException {
        final Map<String,String> map = this.newMap(String.class, String.class);

        map.put("1", "123456789");
        map.put("2", "-10");

        assertThat(map, aMapWithSize(2));

        map.clear();

        assertThat(map, aMapWithSize(0));

        map.put("2", "1");
        map.put("3", "5");

        assertThat(map, aMapWithSize(2));
    }

    @Test
    public void entrySet() {
        final Map<Integer,String> map = this.newMap(Integer.class, String.class);

        map.put(3, "3");
        map.put(2, "-10");
        map.put(1, "123456789");
        map.put(4, "4");

        Set<Map.Entry<Integer,String>> entrySet = map.entrySet();

        assertThat(entrySet.size(), is(4));
        assertThat(entrySet.isEmpty(), is(false));

        Iterator<Map.Entry<Integer,String>> iterator = entrySet.iterator();

        assertThat(iterator.hasNext(), is(true));
        assertThat(toIteratedKeyList(iterator, 4), hasItems(3, 2, 1, 4));
        assertThat(iterator.hasNext(), is(false));

        try {
            iterator.next();
            fail();
        } catch (NoSuchElementException e) {
            // expected
        }
    }

    @Test
    public void values() {
        final Map<Integer,String> map = this.newMap(Integer.class, String.class);

        map.put(2, "2");
        map.put(1, "1");

        Collection<String> values = map.values();

        assertThat(values.size(), is(2));
        assertThat(values.isEmpty(), is(false));

        // test if map backing it changes, this works
        map.put(0, "0");

        assertThat(values.size(), is(3));

        Iterator<String> it = values.iterator();

        assertThat(it.hasNext(), is(true));
        assertThat(it.next(), is("2"));
        assertThat(it.next(), is("1"));
        assertThat(it.next(), is("0"));
        assertThat(it.hasNext(), is(false));

        try {
            it.next();
            fail();
        } catch (NoSuchElementException e) {
            // expected
        }

        // test removal
        map.remove(2);

        assertThat(toValueList(map), hasItems("1", "0"));

        map.remove(0);

        assertThat(toValueList(map), hasItems("1"));

        values.clear();

        assertThat(values.isEmpty(), is(true));
        assertThat(values.size(), is(0));
    }

    @Test
    public void keySet() {
        final Map<Integer,String> map = this.newMap(Integer.class, String.class);

        map.put(3, "3");
        map.put(2, "2");
        map.put(1, "1");
        map.put(4, "4");

        Set<Integer> entrySet = map.keySet();

        assertThat(entrySet.size(), is(4));
        assertThat(entrySet.isEmpty(), is(false));

        Iterator<Integer> it = entrySet.iterator();

        assertThat(it.hasNext(), is(true));
        assertThat(toIteratedList(it, 4), hasItems(3, 2, 1, 4));
        assertThat(it.hasNext(), is(false));

        try {
            it.next();
            fail();
        } catch (NoSuchElementException e) {
            // expected
        }

        // test removal
        map.remove(3);

        assertThat(toKeyList(map), hasItems(2, 1, 4));

        map.remove(4);

        assertThat(toKeyList(map), hasItems(2, 1));
    }

}