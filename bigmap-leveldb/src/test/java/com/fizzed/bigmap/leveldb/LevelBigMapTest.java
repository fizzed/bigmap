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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
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
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.fail;

public class LevelBigMapTest {
 
    @Test
    public void putGetWithStrings() {
        LevelBigMap<String,String> map = new LevelBigMapBuilder()
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
        
        System.out.println("Map is using key_bytes=" + map.getKeyByteSize() + " and value_bytes=" + map.getValueByteSize());
    }
 
    @Test
    public void putGetWithLongs() {
        LevelBigMap<Long,String> map = new LevelBigMapBuilder()
            .setScratchDirectory(Paths.get("target"))
            .setKeyType(Long.class)
            .setValueType(String.class)
            .build();
        
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
        LevelBigMap<Long,String> map = new LevelBigMapBuilder()
            .setScratchDirectory(Paths.get("target"))
            .setKeyType(Long.class)
            .setValueType(String.class)
            .build();
        
        map.put(5L,"5");
        map.put(1L, "1");
        map.put(2L, "2");
        
        assertThat(map.firstKey(), is(1L));
//        assertThat(map.lastKey(), is(5L));
    }
    
    @Test
    public void ordering() {
        LevelBigMap<Long,String> map = new LevelBigMapBuilder()
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

        List<String> values = map.entrySet().stream()
            .map(entry -> entry.getValue())
            .collect(toList());

        assertThat(values.get(0), is("-10"));
        assertThat(values.get(1), is("1"));
        assertThat(values.get(2), is("2"));
        assertThat(values.get(3), is("3"));
        assertThat(values.get(4), is("5"));
        assertThat(values.get(5), is("123456789"));
    }
    
    @Test
    public void byteSizeTracking() {
        LevelBigMap<String,String> map = new LevelBigMapBuilder()
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
    public void clear() throws IOException {
        LevelBigMap<String,String> map = new LevelBigMapBuilder()
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
    
    @Test
    public void entrySet() {
        LevelBigMap<String,String> map = new LevelBigMapBuilder()
            .setScratchDirectory(Paths.get("target"))
            .setKeyType(String.class)
            .setValueType(String.class)
            .build();

        map.put("1", "123456789");
        map.put("2", "-10");
        
        Set<Map.Entry<String,String>> entrySet = map.entrySet();
        
        assertThat(entrySet.size(), is(2));
        assertThat(entrySet.isEmpty(), is(false));
        
        Iterator<Map.Entry<String, String>> it = entrySet.iterator();
        
        assertThat(it.hasNext(), is(true));
        assertThat(it.next().getKey(), is("1"));
        assertThat(it.next().getKey(), is("2"));
        assertThat(it.hasNext(), is(false));
        
        try {
            it.next();
            fail();
        } catch (NoSuchElementException e) {
            // expected
        }
    }
    
    @Test
    public void keySet() {
        LevelBigMap<String,String> map = new LevelBigMapBuilder()
            .setScratchDirectory(Paths.get("target"))
            .setKeyType(String.class)
            .setValueType(String.class)
            .build();

        map.put("1", "123456789");
        map.put("2", "-10");
        
        Set<String> keys = map.keySet();
        
        assertThat(keys.size(), is(2));
        assertThat(keys.isEmpty(), is(false));
        assertThat(keys.contains("1"), is(true));
        assertThat(keys.contains("a"), is(false));
        
        // test if map backing it changes, this works
        map.put("a", "b");
        
        assertThat(keys.size(), is(3));
        assertThat(keys.contains("a"), is(true));
        
        Iterator<String> it = keys.iterator();
        
        assertThat(it.hasNext(), is(true));
        assertThat(it.next(), is("1"));
        assertThat(it.next(), is("2"));
        assertThat(it.next(), is("a"));
        assertThat(it.hasNext(), is(false));
        
        try {
            it.next();
            fail();
        } catch (NoSuchElementException e) {
            // expected
        }
        
        keys.remove("1");
        
        assertThat(keys, hasSize(2));
        
        keys.clear();
        
        assertThat(keys.isEmpty(), is(true));
        assertThat(keys.size(), is(0));
    }
    
    @Test
    public void values() {
        LevelBigMap<String,String> map = new LevelBigMapBuilder()
            .setScratchDirectory(Paths.get("target"))
            .setKeyType(String.class)
            .setValueType(String.class)
            .build();

        map.put("1", "123456789");
        map.put("2", "-10");
        
        Collection<String> values = map.values();
        
        assertThat(values.size(), is(2));
        assertThat(values.isEmpty(), is(false));
        
        // test if map backing it changes, this works
        map.put("a", "b");
        
        assertThat(values.size(), is(3));
        
        Iterator<String> it = values.iterator();
        
        assertThat(it.hasNext(), is(true));
        assertThat(it.next(), is("123456789"));
        assertThat(it.next(), is("-10"));
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
        
        LevelBigMap<CustomKey,String> map = new LevelBigMapBuilder()
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
    
}