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
import java.util.logging.Level;
import java.util.logging.Logger;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.fail;
import org.junit.Ignore;

public class LevelBigLinkedMapTest {
 
    @Test
    public void putGetWithStrings() throws InterruptedException {
        LevelBigLinkedMap<String,String> map = new LevelBigLinkedMapBuilder()
            .setScratchDirectory(Paths.get("target"))
            .setKeyType(String.class)
            .setValueType(String.class)
            .build();

        map.put("b", "1");
        map.put("a", "22");
        map.put("d", "11");
        map.put("f", "66");
        
        assertThat(map, hasKey("a"));
        assertThat(map.get("a"), is("22"));
        assertThat(map.size(), is(4));
        assertThat(map.isEmpty(), is(false));
        
        map.remove("a");
        
        assertThat(map.get("a"), is(nullValue()));
        assertThat(map.size(), is(3));
        assertThat(map.isEmpty(), is(false));
        
        String removed = map.put("c", "44");
        
        assertThat(removed, is(nullValue()));
        assertThat(map.size(), is(4));

        String removed1 = map.put("c", "3");
        
        assertThat(removed1, is("44"));
        assertThat(map.size(), is(4));
        
        System.out.println("Map is using key_bytes=" + map.getKeyByteSize() + " and value_bytes=" + map.getValueByteSize());

    }
 
    @Test
    public void firstKey() throws InterruptedException {
        LevelBigLinkedMap<String,String> map = new LevelBigLinkedMapBuilder()
            .setScratchDirectory(Paths.get("target"))
            .setKeyType(String.class)
            .setValueType(String.class)
            .build();
        
        map.put("5", "First");
        map.put("1", "Second");
        map.put("2", "Third");
        
        assertThat(map.firstKey(), is("5"));

    }

    @Test
    public void LinkedOrder() throws InterruptedException {
        LevelBigLinkedMap<String,String> map = new LevelBigLinkedMapBuilder()
            .setScratchDirectory(Paths.get("target"))
            .setKeyType(String.class)
            .setValueType(String.class)
            .build();

        map.put("D", "123456789");
        map.put("E", "-10");
        map.put("F", "5");
        map.put("B", "1");
        map.put("A", "3");
        map.put("C", "2");

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
    public void values() throws InterruptedException {
        LevelBigLinkedMap<String,String> map = new LevelBigLinkedMapBuilder()
            .setScratchDirectory(Paths.get("target"))
            .setKeyType(String.class)
            .setValueType(String.class)
            .build();

        map.put("2", "123456789");
        map.put("1", "-10");
        
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

    @Test
    public void clear() throws IOException {
        LevelBigLinkedMap<String,String> map = new LevelBigLinkedMapBuilder()
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
 
// we only need to iterate over the map, 
// which means some methods of entrySet may not need implemented
    @Ignore 
    @Test
    public void entrySet() {
        LevelBigLinkedMap<String,String> map = new LevelBigLinkedMapBuilder()
            .setScratchDirectory(Paths.get("target"))
            .setKeyType(String.class)
            .setValueType(String.class)
            .build();
        
        map.put("9", "abcdefgh");
        map.put("2", "123456789");
        map.put("1", "LastOne");

        Set<Map.Entry<String,String>> entrySet = map.entrySet();
        
        assertThat(entrySet.size(), is(3));
        assertThat(entrySet.isEmpty(), is(false));
        
        Iterator<Map.Entry<String, String>> it = entrySet.iterator();
        
        assertThat(it.hasNext(), is(true));
        Map.Entry<String,String> lknext = it.next();
        
        assertThat(lknext.getKey(), is("9"));
        assertThat(lknext.getValue(), is("abcdefgh"));
        
        assertThat(it.hasNext(), is(true));
        lknext = it.next();
        assertThat(lknext.getKey(), is("2"));
        assertThat(lknext.getValue(), is("123456789"));
        
        assertThat(it.hasNext(), is(true));
        lknext = it.next();
        assertThat(it.next().getKey(), is("1"));
        assertThat(lknext.getValue(), is("-10"));
        
        assertThat(it.hasNext(), is(false));
        
        try {
            it.next();
            fail();
        } catch (NoSuchElementException e) {
            // expected
        }
    }
    
    @Ignore
    @Test
    public void keySet() {
        LevelBigLinkedMap<String,String> map = new LevelBigLinkedMapBuilder()
            .setScratchDirectory(Paths.get("target"))
            .setKeyType(String.class)
            .setValueType(String.class)
            .build();

        map.put("2", "123456789");
        map.put("1", "-10");
        
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
    
}