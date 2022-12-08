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
package com.fizzed.bigmap.leveldb;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Paths;
import java.util.*;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import org.junit.Test;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.junit.Assert.fail;

public class LevelBigLinkedMapTest {

    @Test
    public void putAndGet() {
        final Map<String, String> map = new LevelBigLinkedMapBuilder()
            .setScratchDirectory(Paths.get("target"))
            .setKeyType(String.class)
            .setValueType(String.class)
            .build();

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
    public void firstKey() {
        final SortedMap<Long,String> map = new LevelBigLinkedMapBuilder()
            .setScratchDirectory(Paths.get("target"))
            .setKeyType(Long.class)
            .setValueType(String.class)
            .build();

        try {
            map.firstKey(); // java map throws a NoSuchElementException
            fail();
        }
        catch (NoSuchElementException e) {
            // expected
        }

        map.put(5L, "5");
        map.put(2L, "2");
        map.put(1L, "1");   // this would normally be first in a sorted map, but the first element inserted is 5L

        assertThat(map.firstKey(), is(5L));

        map.remove(5L);

        assertThat(map.firstKey(), is(2L));
    }

    @Test
    public void ordering() {
        final Map<Long,String> map = new LevelBigLinkedMapBuilder()
            .setScratchDirectory(Paths.get("target"))
            .setKeyType(Long.class)
            .setValueType(String.class)
            .build();

        map.put(123456789L, "123456789");
        map.put(-10L, "-10");
        map.put(5L, "5");
        map.put(1L, "1");
        map.put(3L, "3");
        map.put(2L, "2");

        final List<String> values = map.values()
            .stream()
            .collect(toList());

        assertThat(values.get(0), is("123456789"));
        assertThat(values.get(1), is("-10"));
        assertThat(values.get(2), is("5"));
        assertThat(values.get(3), is("1"));
        assertThat(values.get(4), is("3"));
        assertThat(values.get(5), is("2"));
    }

    @Test
    public void remove() throws IOException {
        final SortedMap<Integer,String> map = new LevelBigLinkedMapBuilder()
            .setScratchDirectory(Paths.get("target"))
            .setKeyType(Integer.class)
            .setValueType(String.class)
            .build();

        map.put(4, "4");
        map.put(1, "1");
        map.put(1, "1replaced");
        map.put(2, "2");

        assertThat(map, aMapWithSize(3));
        assertThat(map.firstKey(), is(4));
        assertThat(map.get(1), is("1replaced"));

        String removed;

        removed = map.remove(0);

        assertThat(map, aMapWithSize(3));
        assertThat(map.firstKey(), is(4));
        assertThat(removed, is(nullValue()));

        removed = map.remove(1);

        assertThat(map, aMapWithSize(2));
        assertThat(map.firstKey(), is(4));
        assertThat(removed, is("1replaced"));

        // try to remove it again
        removed = map.remove(1);

        assertThat(map, aMapWithSize(2));
        assertThat(removed, is(nullValue()));

        removed = map.remove(4);

        assertThat(map, aMapWithSize(1));
        assertThat(removed, is("4"));
        assertThat(map.firstKey(), is(2));

        removed = map.remove(2);

        assertThat(map, aMapWithSize(0));
        assertThat(removed, is("2"));

        try {
            map.firstKey();
            fail();
        } catch (NoSuchElementException e) {
            // expected
        }
    }

    @Test
    public void clear() throws IOException {
        LevelBigLinkedMap<String, String> map = new LevelBigLinkedMapBuilder()
            .setScratchDirectory(Paths.get("target"))
            .setKeyType(String.class)
            .setValueType(String.class)
            .build();

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
        final Map<Integer,String> map = new LevelBigLinkedMapBuilder()
            .setScratchDirectory(Paths.get("target"))
            .setKeyType(Integer.class)
            .setValueType(String.class)
            .build();

        map.put(3, "3");
        map.put(2, "-10");
        map.put(1, "123456789");
        map.put(4, "4");

        Set<Map.Entry<Integer,String>> entrySet = map.entrySet();

        assertThat(entrySet.size(), is(4));
        assertThat(entrySet.isEmpty(), is(false));

        Iterator<Map.Entry<Integer,String>> it = entrySet.iterator();

        assertThat(it.hasNext(), is(true));
        assertThat(it.next().getKey(), is(3));
        assertThat(it.next().getKey(), is(2));
        assertThat(it.next().getKey(), is(1));
        assertThat(it.next().getKey(), is(4));
        assertThat(it.hasNext(), is(false));

        try {
            it.next();
            fail();
        } catch (NoSuchElementException e) {
            // expected
        }
    }

    @Test
    public void values() {
        final Map<Integer,String> map = new LevelBigLinkedMapBuilder()
            .setScratchDirectory(Paths.get("target"))
            .setKeyType(Integer.class)
            .setValueType(String.class)
            .build();

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

        List<String> ar = map.values().stream().collect(toList());

        assertThat(ar, hasItems("1", "0"));

        map.remove(0);

        ar = map.values().stream().collect(toList());

        assertThat(ar, hasItems("1"));

        values.clear();

        assertThat(values.isEmpty(), is(true));
        assertThat(values.size(), is(0));
    }

    @Test
    public void keySet() {
        final Map<Integer,String> map = new LevelBigLinkedMapBuilder()
            .setScratchDirectory(Paths.get("target"))
            .setKeyType(Integer.class)
            .setValueType(String.class)
            .build();

        map.put(3, "3");
        map.put(2, "2");
        map.put(1, "1");
        map.put(4, "4");

        Set<Integer> entrySet = map.keySet();

        assertThat(entrySet.size(), is(4));
        assertThat(entrySet.isEmpty(), is(false));

        Iterator<Integer> it = entrySet.iterator();

        assertThat(it.hasNext(), is(true));
        assertThat(it.next(), is(3));
        assertThat(it.next(), is(2));
        assertThat(it.next(), is(1));
        assertThat(it.next(), is(4));
        assertThat(it.hasNext(), is(false));

        try {
            it.next();
            fail();
        } catch (NoSuchElementException e) {
            // expected
        }

        // test removal
        map.remove(3);

        List<Integer> ar = map.keySet().stream().collect(toList());

        assertThat(ar, hasItems(2, 1, 4));

        map.remove(4);

        ar = map.keySet().stream().collect(toList());

        assertThat(ar, hasItems(2, 1));
    }

}