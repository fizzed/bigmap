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

import com.fizzed.bigmap.impl.AbstractBigLinkedMap;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static com.fizzed.bigmap.impl.BigMapHelper.*;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeThat;

abstract public class AbstractBigLinkedMapTest extends AbstractBigMapTest {

    @Test
    public void putOrdered() {
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
    public void removeOrdered() throws IOException {
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
    public void entrySetOrdered() {
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
    public void valuesOrdered() {
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
        assertThat(toIteratedList(it, 3), hasItems("2", "1", "0"));
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
    public void keySetOrdered() {
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