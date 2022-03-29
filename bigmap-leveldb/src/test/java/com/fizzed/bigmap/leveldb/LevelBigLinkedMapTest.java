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
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.junit.Assert.fail;

public class LevelBigLinkedMapTest {

    @Test
    public void putGetWithStrings() {
        LevelBigLinkedMap<String, String> map = new LevelBigMapBuilder()
                .setScratchDirectory(Paths.get("target"))
                .setKeyType(String.class)
                .setValueType(String.class)
                .buildLinked();

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
    public void putGetWithLongs() {
        LevelBigLinkedMap<Long, String> map = new LevelBigMapBuilder()
                .setScratchDirectory(Paths.get("target"))
                .setKeyType(Long.class)
                .setValueType(String.class)
                .buildLinked();

        map.put(1L, "1");

        assertThat(map, hasKey(1L));
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
    public void firstKey() {
        LevelBigLinkedMap<Long, String> map = new LevelBigMapBuilder()
                .setScratchDirectory(Paths.get("target"))
                .setKeyType(Long.class)
                .setValueType(String.class)
                .buildLinked();

        map.put(5L, "5");
        map.put(1L, "1");
        map.put(2L, "2");

        assertThat(map.firstKey(), is(5L));
    }

    @Test
    public void firstKeyString() {
        LevelBigLinkedMap<String, String> map = new LevelBigMapBuilder()
                .setScratchDirectory(Paths.get("target"))
                .setKeyType(String.class)
                .setValueType(String.class)
                .buildLinked();

        map.put("Z", "5");
        map.put("A", "1");
        map.put("C", "2");

        assertThat(map.firstKey(), is("Z"));
    }

    @Test
    public void ordering() {
        LevelBigLinkedMap<Long, String> map = new LevelBigMapBuilder()
                .setScratchDirectory(Paths.get("target"))
                .setKeyType(Long.class)
                .setValueType(String.class)
                .buildLinked();

        map.put(123456789L, "123456789");
        map.put(-10L, "-10");
        map.put(5L, "5");
        map.put(1L, "1");
        map.put(3L, "3");
        map.put(2L, "2");

        System.out.println(map.toString());

        List<String> values = map.entrySet().stream()
                .map(entry -> entry.getValue())
                .collect(toList());

        assertThat(values.get(0), is("123456789"));
        assertThat(values.get(1), is("-10"));
        assertThat(values.get(2), is("5"));
        assertThat(values.get(3), is("1"));
        assertThat(values.get(4), is("3"));
        assertThat(values.get(5), is("2"));
    }

    @Test
    public void clear() throws IOException {
        LevelBigLinkedMap<String, String> map = new LevelBigMapBuilder()
                .setScratchDirectory(Paths.get("target"))
                .setKeyType(String.class)
                .setValueType(String.class)
                .buildLinked();

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
        LevelBigLinkedMap<String, String> map = new LevelBigMapBuilder()
                .setScratchDirectory(Paths.get("target"))
                .setKeyType(String.class)
                .setValueType(String.class)
                .buildLinked();

        map.put("2", "-10");
        map.put("1", "123456789");

        Set<Map.Entry<String, String>> entrySet = map.entrySet();

        assertThat(entrySet.size(), is(2));
        assertThat(entrySet.isEmpty(), is(false));

        Iterator<Map.Entry<String, String>> it = entrySet.iterator();

        assertThat(it.hasNext(), is(true));
        assertThat(it.next().getKey(), is("2"));
        assertThat(it.next().getKey(), is("1"));
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
        LevelBigLinkedMap<String, String> map = new LevelBigMapBuilder()
                .setScratchDirectory(Paths.get("target"))
                .setKeyType(String.class)
                .setValueType(String.class)
                .buildLinked();

        map.put("2", "-10");
        map.put("1", "123456789");

        Collection<String> values = map.values();

        assertThat(values.size(), is(2));
        assertThat(values.isEmpty(), is(false));

        // test if map backing it changes, this works
        map.put("a", "b");

        assertThat(values.size(), is(3));

        Iterator<String> it = values.iterator();

        assertThat(it.hasNext(), is(true));
        assertThat(it.next(), is("-10"));
        assertThat(it.next(), is("123456789"));
        assertThat(it.next(), is("b"));
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
    }

    @Test
    public void keySet() {
        LevelBigLinkedMap<String, String> map = new LevelBigMapBuilder()
                .setScratchDirectory(Paths.get("target"))
                .setKeyType(String.class)
                .setValueType(String.class)
                .buildLinked();

        map.put("22", "123456789");
        map.put("1", "111");
        map.put("2", "-10");

        try {
            Set<String> keys = map.keySet();

            assertThat(keys.size(), is(3));

        } catch (UnsupportedOperationException ex) {
            // expected
        }

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

        LevelBigMap<CustomKey, String> map = new LevelBigMapBuilder()
                .setScratchDirectory(Paths.get("target"))
                .setKeyType(CustomKey.class, keyComparator)
                .setValueType(String.class)
                .build();

        map.put(new CustomKey(0L, 1), "0-1");
        map.put(new CustomKey(1L, 5), "1-5");
        map.put(new CustomKey(1L, 1), "1-1");
        map.put(new CustomKey(1L, 2), "1-2");
        map.put(new CustomKey(3L, 1), "3-1");
        map.put(new CustomKey(10L, 6), "10-6");
        map.put(new CustomKey(15L, 1), "15-1");

        Iterator<String> it = map.values().iterator();

        assertThat(it.next(), is("15-1"));
        assertThat(it.next(), is("10-6"));
        assertThat(it.next(), is("3-1"));
        assertThat(it.next(), is("1-1"));
        assertThat(it.next(), is("1-2"));
        assertThat(it.next(), is("1-5"));
        assertThat(it.next(), is("0-1"));
    }

}
