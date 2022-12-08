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

import java.nio.file.Paths;
import java.util.Iterator;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import java.util.List;
import java.util.NoSuchElementException;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.fail;

public class LevelBigSetTest {
 
    @Test
    public void putGetWithStrings() {
        LevelBigSet<String> set = new LevelBigSetBuilder()
            .setScratchDirectory(Paths.get("target"))
            .setKeyType(String.class)
            .build();
        
        set.add("a");
        
        assertThat(set, hasItem("a"));
        assertThat(set.contains("a"), is(true));
        assertThat(set.size(), is(1));
        assertThat(set.isEmpty(), is(false));
        
        boolean added = set.add("a");
        
        assertThat(added, is(false));
        
        set.remove("a");
        
        assertThat(set.contains("a"), is(false));
        assertThat(set.size(), is(0));
        assertThat(set.isEmpty(), is(true));
        
        boolean removed = set.remove("b");
        
        assertThat(removed, is(false));
        assertThat(set.size(), is(0));

        boolean added1 = set.add("b");
        
        assertThat(added1, is(true));
        assertThat(set.size(), is(1));
        
        boolean removed1 = set.remove("b");
        
        assertThat(removed1, is(true));
        assertThat(set.size(), is(0));
    }
 
    @Test
    public void first() {
        LevelBigSet<Long> set = new LevelBigSetBuilder()
            .setScratchDirectory(Paths.get("target"))
            .setKeyType(Long.class)
            .build();
        
        set.add(5L);
        set.add(1L);
        set.add(2L);
        
        assertThat(set.first(), is(1L));
    }
    
    @Test
    public void ordering() {
        LevelBigSet<Long> set = new LevelBigSetBuilder()
            .setScratchDirectory(Paths.get("target"))
            .setKeyType(Long.class)
            .build();

        set.add(123456789L);
        set.add(-10L);
        set.add(5L);
        set.add(1L);
        set.add(3L);
        set.add(2L);

        List<Long> values = set.stream()
            .collect(toList());

        assertThat(values.get(0), is(-10L));
        assertThat(values.get(1), is(1L));
        assertThat(values.get(2), is(2L));
        assertThat(values.get(3), is(3L));
        assertThat(values.get(4), is(5L));
        assertThat(values.get(5), is(123456789L));
    }
    
    @Test
    public void iterator() {
        LevelBigSet<String> set = new LevelBigSetBuilder()
            .setScratchDirectory(Paths.get("target"))
            .setKeyType(String.class)
            .build();

        set.add("4");
        set.add("1");
        set.add("3");
        set.add("2");

        Iterator<String> it = set.iterator();

        assertThat(it.hasNext(), is(true));
        assertThat(it.next(), is("1"));
        assertThat(it.next(), is("2"));
        assertThat(it.next(), is("3"));
        assertThat(it.next(), is("4"));
        assertThat(it.hasNext(), is(false));
        
        try {
            it.next();
            fail();
        } catch (NoSuchElementException e) {
            // expected
        }

        // test removal
        set.remove("1");

        it = set.iterator();

        assertThat(it.hasNext(), is(true));
        assertThat(it.next(), is("2"));
        assertThat(it.next(), is("3"));
        assertThat(it.next(), is("4"));
        assertThat(it.hasNext(), is(false));

        set.remove("3");

        it = set.iterator();

        assertThat(it.hasNext(), is(true));
        assertThat(it.next(), is("2"));
        assertThat(it.next(), is("4"));
        assertThat(it.hasNext(), is(false));

        set.remove("4");

        it = set.iterator();

        assertThat(it.hasNext(), is(true));
        assertThat(it.next(), is("2"));
        assertThat(it.hasNext(), is(false));

        set.remove("2");

        it = set.iterator();

        assertThat(it.hasNext(), is(false));
    }
    
}