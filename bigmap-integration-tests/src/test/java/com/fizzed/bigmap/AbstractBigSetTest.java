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
import java.util.*;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

abstract public class AbstractBigSetTest {

    abstract public <V> Set<V> newSet(Class<V> valueType);

    @Test
    public void addAndContains() {
        final Set<String> set = this.newSet(String.class);
        boolean added;

        added = set.add("a");

        assertThat(added, is(true));
        assertThat(set, hasItem("a"));
        assertThat(set.contains("a"), is(true));
        assertThat(set.size(), is(1));
        assertThat(set.isEmpty(), is(false));

        set.remove("a");
        
        assertThat(set.contains("a"), is(false));
        assertThat(set.size(), is(0));
        assertThat(set.isEmpty(), is(true));
        
        set.remove("b");

        assertThat(set.contains("b"), is(false));
        assertThat(set.size(), is(0));

        added = set.add("b");

        assertThat(set.contains("b"), is(true));
        assertThat(added, is(true));
        assertThat(set.size(), is(1));

        added = set.add("b");       // duplicate item

        assertThat(set.contains("b"), is(true));
        assertThat(added, is(false));
        assertThat(set.size(), is(1));
    }

    @Test
    public void size() {
        final Set<String> set = this.newSet(String.class);

        assertThat(set, hasSize(0));

        set.add("a");

        assertThat(set.size(), is(1));
        assertThat(set, hasSize(1));

        set.remove("a");

        assertThat(set, hasSize(0));

        set.add("a");
        set.add("b");
        set.add("a");       // duplicate item

        assertThat(set, hasSize(2));

        set.clear();

        assertThat(set, hasSize(0));
    }

    @Test
    public void remove() {
        final Set<String> set = this.newSet(String.class);

        boolean removed;

        removed = set.remove("a");      // non-existent

        assertThat(removed, is(false));

        set.add("a");

        removed = set.remove("a");      // non-existent

        assertThat(removed, is(true));
    }

    /*@Test
    public void first() {
        final Set<Long> set = this.newSet(Long.class);
        
        set.add(5L);
        set.add(1L);
        set.add(2L);
        
        assertThat(set.first(), is(1L));
    }*/
    
    /*@Test
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
    }*/
    
    @Test
    public void iterator() {
        final Set<String> set = this.newSet(String.class);

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