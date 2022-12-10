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

import org.junit.Assume;
import org.junit.Test;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map.Entry;

import static com.fizzed.bigmap.BigMapHelper.*;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeThat;

abstract public class AbstractBigMapTest {

    abstract public <K,V> Map<K,V> newMap(Class<K> keyType, Class<V> valueType);

    @Test
    public void putNullKey() {
        final Map<String,String> map = this.newMap(String.class, String.class);

        try {
            map.put(null, "1");
            fail();
        }
        catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void putNullValue() {
        final Map<String,String> map = this.newMap(String.class, String.class);

        try {
            map.put("1", null);
            fail();
        }
        catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void putAndGetWithStrings() {
        final Map<String,String> map = this.newMap(String.class, String.class);

        map.put("a", "1");

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
    public void putAndGetWithLongs() {
        final Map<Long,String> map = this.newMap(Long.class, String.class);

        map.put(1L, "1");

        assertThat(map.get(1L), is("1"));
        assertThat(map.size(), is(1));
        assertThat(map.isEmpty(), is(false));

        map.remove(1L);

        assertThat(map.get(1L), is(nullValue()));
        assertThat(map.size(), is(0));
        assertThat(map.isEmpty(), is(true));

        String removed = map.put(2L, "2");

        assertThat(removed, is(nullValue()));

        String removed1 = map.put(2L, "3");

        assertThat(removed1, is("2"));
    }

    @Test
    public void containsKey() throws IOException {
        final Map<String,String> map = this.newMap(String.class, String.class);

        map.put("1", "1");
        map.put("2", "2");

        assertThat(map, aMapWithSize(2));

        assertThat(map.containsKey("1"), is(true));
        assertThat(map.containsKey("2"), is(true));
        assertThat(map.containsKey("0"), is(false));
        assertThat(map.containsKey(""), is(false));

        map.remove("1");

        assertThat(map.containsKey("1"), is(false));
        assertThat(map.containsKey("2"), is(true));

        map.remove("2");

        assertThat(map.containsKey("1"), is(false));
        assertThat(map.containsKey("2"), is(false));
    }

    @Test
    public void containsKeyWithNull() {
        final Map<String,String> map = this.newMap(String.class, String.class);

        assertThat(map.containsKey(null), is(false));
    }

    @Test
    public void clear() throws IOException {
        final Map<String,String> map = this.newMap(String.class, String.class);

        map.put("1", "1");
        map.put("2", "2");
        map.put("0", "0");

        assertThat(map, aMapWithSize(3));

        map.clear();

        assertThat(map, aMapWithSize(0));

        map.put("2", "1");
        map.put("3", "5");

        assertThat(map, aMapWithSize(2));
    }

    @Test
    public void remove() throws IOException {
        final Map<String,String> map = this.newMap(String.class, String.class);

        map.put("1", "1");
        map.put("2", "2");

        assertThat(map, aMapWithSize(2));

        String removed;

        removed = map.remove("0");

        assertThat(map, aMapWithSize(2));
        assertThat(removed, is(nullValue()));
        assertThat(toValueList(map), hasItems("1", "2"));

        removed = map.remove("1");

        assertThat(map, aMapWithSize(1));
        assertThat(removed, is("1"));
        assertThat(toValueList(map), hasItems("2"));

        // try to remove it again
        removed = map.remove("1");

        assertThat(map, aMapWithSize(1));
        assertThat(removed, is(nullValue()));
        assertThat(toValueList(map), hasItems("2"));

        removed = map.remove("2");

        assertThat(map, aMapWithSize(0));
        assertThat(removed, is("2"));
        assertThat(toValueList(map), hasSize(0));
    }

    @Test
    public void values() {
        final Map<String,String> map = this.newMap(String.class, String.class);

        map.put("1", "1");
        map.put("2", "2");

        Collection<String> values = map.values();

        assertThat(values.size(), is(2));
        assertThat(values.isEmpty(), is(false));

        // test if map backing it changes, this works
        map.put("a", "b");

        assertThat(values.size(), is(3));

        Iterator<String> it = values.iterator();

        assertThat(it.hasNext(), is(true));
        assertThat(toIteratedList(it, 3), containsInAnyOrder("1", "2", "b"));
        assertThat(it.hasNext(), is(false));

        try {
            it.next();
            fail();
        } catch (NoSuchElementException e) {
            // expected
        }

        values.clear();

        assertThat(values.isEmpty(), is(true));
        assertThat(values.size(), is(0));

        // test removal of items too
        map.put("2", "2");
        map.put("3", "3");
        map.put("1", "1");
        map.put("4", "4");

        assertThat(toValueList(map), containsInAnyOrder("1", "2", "3", "4"));

        map.remove("1");

        assertThat(toValueList(map), containsInAnyOrder("2", "3", "4"));

        map.remove("4");

        assertThat(toValueList(map), containsInAnyOrder("2", "3"));

        map.remove("2");

        assertThat(toValueList(map), containsInAnyOrder("3"));

        map.remove("3");

        assertThat(toValueList(map), hasSize(0));
    }

    @Test
    public void keySet() {
        final Map<String,String> map = this.newMap(String.class, String.class);

        map.put("1", "1");
        map.put("2", "2");

        Set<String> keys = map.keySet();

        assertThat(keys.size(), is(2));
        assertThat(keys.isEmpty(), is(false));
        assertThat(keys.contains("1"), is(true));
        assertThat(keys.contains("2"), is(true));
        assertThat(keys.contains("a"), is(false));

        // test if map backing it changes, this works
        map.put("a", "b");

        assertThat(keys, hasSize(3));
        assertThat(keys.contains("a"), is(true));

        Iterator<String> it = keys.iterator();

        assertThat(it.hasNext(), is(true));
        assertThat(toIteratedList(it, 3), containsInAnyOrder("1", "2", "a"));
        assertThat(it.hasNext(), is(false));

        try {
            it.next();
            fail();
        } catch (NoSuchElementException e) {
            // expected
        }

        keys.remove("1");

        assertThat(keys, hasSize(2));

        it = keys.iterator();

        assertThat(it.hasNext(), is(true));
        assertThat(toIteratedList(it, 2), containsInAnyOrder("2", "a"));
        assertThat(it.hasNext(), is(false));

        keys.clear();

        assertThat(keys.isEmpty(), is(true));
        assertThat(keys.size(), is(0));
    }

    @Test
    public void entrySet() {
        final Map<String,String> map = this.newMap(String.class, String.class);

        map.put("1", "1");
        map.put("2", "2");
        map.put("3", "3");
        map.put("4", "4");

        Set<Entry<String,String>> entrySet = map.entrySet();

        assertThat(entrySet.size(), is(4));
        assertThat(entrySet.isEmpty(), is(false));

        Iterator<Entry<String,String>> it = entrySet.iterator();

        assertThat(it.hasNext(), is(true));
        assertThat(toIteratedKeyList(it, 4), containsInAnyOrder("1", "2", "3", "4"));
        assertThat(it.hasNext(), is(false));

        try {
            it.next();
            fail();
        } catch (NoSuchElementException e) {
            // expected
        }

        // test removal
        map.remove("1");

        entrySet = map.entrySet();

        assertThat(entrySet, hasSize(3));
        assertThat(toIteratedKeyList(entrySet.iterator(), 3), containsInAnyOrder("2", "3", "4"));

        map.remove("3");

        entrySet = map.entrySet();

        assertThat(entrySet, hasSize(2));
        assertThat(toIteratedKeyList(entrySet.iterator(), 2), containsInAnyOrder("2", "4"));

        map.remove("4");

        entrySet = map.entrySet();

        assertThat(entrySet, hasSize(1));
        assertThat(toIteratedKeyList(entrySet.iterator(), 1), containsInAnyOrder("2"));

        map.remove("2");

        entrySet = map.entrySet();

        assertThat(entrySet, hasSize(0));
    }

    //
    // SortedMap tests
    //

    @Test
    public void firstKey() {
        final Map<Long,String> _map = this.newMap(Long.class, String.class);

        assumeThat(_map, instanceOf(SortedMap.class));

        final SortedMap<Long,String> map = (SortedMap<Long,String>)_map;

        try {
            map.firstKey(); // java map throws a NoSuchElementException
            fail();
        }
        catch (NoSuchElementException e) {
            // expected
        }

        map.put(5L,"5");
        map.put(2L, "2");
        map.put(1L, "1");

        assertThat(map.firstKey(), is(1L));
        assertThat(map, aMapWithSize(3));
        assertThat(map.isEmpty(), is(false));

        map.remove(1L);

        assertThat(map.firstKey(), is(2L));
        assertThat(map, aMapWithSize(2));
        assertThat(map.isEmpty(), is(false));

        map.remove(2L);

        assertThat(map.firstKey(), is(5L));
        assertThat(map, aMapWithSize(1));
        assertThat(map.isEmpty(), is(false));

        map.remove(5L);

        assertThat(map, aMapWithSize(0));
        assertThat(map.isEmpty(), is(true));

        try {
            map.firstKey(); // java map throws a NoSuchElementException
            fail();
        }
        catch (NoSuchElementException e) {
            // expected
        }
    }

    @Test
    public void sortedOrdering() {
        final Map<Long,String> _map = this.newMap(Long.class, String.class);

        assumeThat(_map, instanceOf(SortedMap.class));

        final SortedMap<Long,String> map = (SortedMap<Long,String>)_map;

        map.put(123456789L, "123456789");
        map.put(5L, "5");
        map.put(1L, "1");
        map.put(3L, "3");
        map.put(2L, "2");
        map.put(0L, "0");

        assertThat(toValueList(map), hasItems("0", "1", "2", "3", "5", "123456789"));
    }

    /*
    @Test
    public void byteSizeTracking() {
        final LevelBigMap<String,String> map = new LevelBigMapBuilder()
            .setScratchDirectory(Paths.get("target"))
            .setKeyType(String.class)
            .setValueType(String.class)
            .build();

        map.put("1", "123456789");
        map.put("2", "-10");

        assertThat(map.getKeyByteSize(), is(2L));
        assertThat(map.getValueByteSize(), is(12L));

        // replace value updates bytes
        map.put("2", "1");

        assertThat(map.getKeyByteSize(), is(2L));
        assertThat(map.getValueByteSize(), is(10L));

        // remove value updates
        map.remove("2");

        assertThat(map.getKeyByteSize(), is(1L));
        assertThat(map.getValueByteSize(), is(9L));
    }

    @Test
    public void clearWithDisk() throws IOException {
        final LevelBigMap<String,String> map = new LevelBigMapBuilder()
            .setScratchDirectory(Paths.get("target"))
            .setKeyType(String.class)
            .setValueType(String.class)
            .build();

        map.put("1", "123456789");
        map.put("2", "-10");

        assertThat(map, aMapWithSize(2));
        assertThat(map.getKeyByteSize(), is(2L));
        assertThat(map.getValueByteSize(), is(12L));

        Path directory = map.getDirectory();

        assertThat(Files.list(directory).count(), is(4L));
        Path firstFile = Files.list(directory).findFirst().orElse(null);

        map.clear();

        assertThat(map, aMapWithSize(0));
        assertThat(map.getKeyByteSize(), is(0L));
        assertThat(map.getValueByteSize(), is(0L));

        map.put("2", "1");
        map.put("3", "5");

        assertThat(map, aMapWithSize(2));
        assertThat(map.getKeyByteSize(), is(2L));
        assertThat(map.getValueByteSize(), is(2L));
    }

    static public class CustomKey implements Serializable {

        Long a;
        Integer b;

        public CustomKey(Long a, Integer b) {
            this.a = a;
            this.b = b;
        }

    }

    @Test
    public void customComparator() {
        Comparator<CustomKey> keyComparator = (CustomKey o1, CustomKey o2) -> {
            // right first then left for A
            int c = o2.a.compareTo(o1.a);
            if (c == 0) {
                // left first then right for B
                c = o1.b.compareTo(o2.b);
            }
            return c;
        };

        final Map<CustomKey,String> map = new LevelBigMapBuilder()
            .setScratchDirectory(Paths.get("target"))
            .setKeyType(CustomKey.class, keyComparator)
            .setValueType(String.class)
            .build();

        map.put(new CustomKey(0L,1), "0-1");
        map.put(new CustomKey(1L,5), "1-5");
        map.put(new CustomKey(1L,1), "1-1");
        map.put(new CustomKey(1L,2), "1-2");
        map.put(new CustomKey(3L,1), "3-1");
        map.put(new CustomKey(10L,6), "10-6");
        map.put(new CustomKey(15L,1), "15-1");

        Iterator<String> it = map.values().iterator();

        assertThat(it.next(), is("15-1"));
        assertThat(it.next(), is("10-6"));
        assertThat(it.next(), is("3-1"));
        assertThat(it.next(), is("1-1"));
        assertThat(it.next(), is("1-2"));
        assertThat(it.next(), is("1-5"));
        assertThat(it.next(), is("0-1"));
    }

    @Test
    public void persistent() throws IOException {
        final Path dbDir = Paths.get("target/persistent-"+UUID.randomUUID());

        LevelBigMap<Integer,String> map = new LevelBigMapBuilder()
            .setPersistent(true)
            .setScratchDirectory(dbDir)
            .setKeyType(Integer.class)
            .setValueType(String.class)
            .build();

        map.put(1, "1");
        map.put(1025, "1025");
        map.put(651, "651");

        map.close();
        map = null;


        map = new LevelBigMapBuilder()
            .setPersistent(true)
            .setScratchDirectory(dbDir)
            .setKeyType(Integer.class)
            .setValueType(String.class)
            .build();

        assertThat(map, hasKey(1));
        assertThat(map, hasKey(1025));
        assertThat(map, hasKey(651));
        assertThat(map, not(hasKey(2)));

        assertThat(map.size(), is(3));
        assertThat(map.getKeyByteSize(), is(12L));
        assertThat(map.getValueByteSize(), is(8L));

        map.clear();

        assertThat(map.size(), is(0));

        map.close();
        map = null;


        map = new LevelBigMapBuilder()
            .setPersistent(true)
            .setScratchDirectory(dbDir)
            .setKeyType(Integer.class)
            .setValueType(String.class)
            .build();

        map.put(1, "1");

        assertThat(map.size(), is(1));
    }
    */

}