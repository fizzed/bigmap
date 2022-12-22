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

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

import static com.fizzed.bigmap.impl.BigMapHelper.toIteratedList;
import static com.fizzed.bigmap.impl.BigMapHelper.toValueList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeThat;

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
    public void addAndContainsWithComplexObject() {
        final Set<TestIdentifier> set = this.newSet(TestIdentifier.class);

        set.add(new TestIdentifier("a", "b"));
        set.add(new TestIdentifier("c", "d"));

        assertThat(set, hasSize(2));
        assertThat(set.contains(new TestIdentifier("a", "b")), is(true));

        set.remove(new TestIdentifier("a", "b"));

        assertThat(set.contains(new TestIdentifier("a", "b")), is(false));
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

    @Test
    public void clear() throws IOException {
        final Set<String> set = this.newSet(String.class);

        set.add("1");
        set.add("2");
        set.add("0");

        assertThat(set, hasSize(3));

        set.clear();

        assertThat(set, hasSize(0));

        set.add("1");

        assertThat(set, hasSize(1));
    }

    @Test
    public void iterator() {
        final Set<String> set = this.newSet(String.class);

        set.add("4");
        set.add("1");
        set.add("3");
        set.add("2");

        Iterator<String> it = set.iterator();
        assertThat(toIteratedList(it, 4), containsInAnyOrder("1", "2", "3", "4"));
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
        assertThat(toIteratedList(it, 3), containsInAnyOrder("2", "3", "4"));
        assertThat(it.hasNext(), is(false));

        set.remove("3");

        it = set.iterator();

        assertThat(it.hasNext(), is(true));
        assertThat(toIteratedList(it, 2), containsInAnyOrder("4", "2"));
        assertThat(it.hasNext(), is(false));

        set.remove("4");

        it = set.iterator();

        assertThat(it.hasNext(), is(true));
        assertThat(toIteratedList(it, 1), containsInAnyOrder("2"));
        assertThat(it.hasNext(), is(false));

        set.remove("2");

        it = set.iterator();

        assertThat(it.hasNext(), is(false));
    }

    @Test
    public void iteratingLarger() {
        final Set<String> set = this.newSet(String.class);

        for (int i = 0; i < 50000; i++) {
            set.add("hello #" + i);
        }

        for (int i = 0; i < 5; i++) {
            for (String s : set) {
                Assert.assertThat(s.length(), greaterThan(1));
                assertThat(s, is(not(nullValue())));
            }
        }
    }

    @Test
    public void iteratingMany() throws IOException {
        for (int j = 0; j < 10; j++) {
            final Set<String> set = this.newSet(String.class);
            for (int i = 0; i < 5000; i++) {
                UUID uuid = UUID.randomUUID();
                set.add(uuid.toString());
            }
            for (String blah : set) {
                if (blah == null) {
                    fail("Value was null");
                } else {
                    //System.out.println("blah: " + blah);
                    //log.debug("asdf: {}", blah);
                }
            }
        }
    }

    @Test
    public void sortedSetOrdered() {
        final Set<Long> _set = this.newSet(Long.class);

        assumeThat(_set, instanceOf(SortedSet.class));

        final SortedSet<Long> set = (SortedSet<Long>)_set;

        set.add(123456789L);
        set.add(-10L);
        set.add(5L);
        set.add(1L);
        set.add(3L);
        set.add(2L);

        assertThat(toValueList(set), hasItems(-10L, 1L, 2L, 3L, 5L, 123456789L));
    }

    @Test
    public void byteSizeTracking() {
        final Set<String> _set = this.newSet(String.class);

        assumeThat(_set, instanceOf(BigSet.class));

        final BigSet<String> set = (BigSet<String>)_set;

        set.add("1");
        set.add("2");

        assertThat(set.getValueByteSize(), is(2L));

        // replace value updates bytes
        set.add("2");

        assertThat(set.getValueByteSize(), is(2L));

        // remove value updates
        set.remove("2");

        assertThat(set.getValueByteSize(), is(1L));
    }

    @Test
    public void close() throws IOException {
        final Set<String> _set = this.newSet(String.class);

        assumeThat(_set, instanceOf(BigSet.class));

        final BigSet<String> set = (BigSet<String>)_set;

        set.add("1");
        set.add("2");

        assertThat(set, hasSize(2));

        assertThat(Files.exists(set.getPath()), is(true));

        set.close();

        // the directory and everything should be cleaned up now
        assertThat(Files.exists(set.getPath()), is(false));
        assertThat(set.isClosed(), is(true));

        // map.close() should be able to succeed again and not throw an exception
        set.close();

        try {
            set.checkIfClosed();
            fail();
        } catch (Exception e) {
            // expected
        }
    }

    @Test
    public void closeRemovesFromRegistry() throws IOException {
        final Set<String> _set = this.newSet(String.class);

        assumeThat(_set, instanceOf(BigSet.class));

        final BigSet<String> set = (BigSet<String>)_set;

        UUID id = set.getId();

        assertThat(BigObjectRegistry.getDefault().isRegistered(id), is(true));

        set.close();

        assertThat(BigObjectRegistry.getDefault().isRegistered(id), is(false));

        // we should be able to re-open it again
        set.open();

        assertThat(BigObjectRegistry.getDefault().isRegistered(id), is(true));

        set.close();

        assertThat(BigObjectRegistry.getDefault().isRegistered(id), is(false));
    }

    @Test
    public void dereferenceAutomaticallyGarbageCollectsFromRegistry() throws Exception {
        Set<String> _set = this.newSet(String.class);

        assumeThat(_set, instanceOf(BigSet.class));

        BigSet<String> set = (BigSet<String>)_set;

        UUID id = set.getId();

        assertThat(BigObjectRegistry.getDefault().isRegistered(id), is(true));

        _set = null;
        set = null;
        System.gc();

        // wait for garbage collector to run
        final long now = System.currentTimeMillis();
        while (BigObjectRegistry.getDefault().isRegistered(id)) {
            if (System.currentTimeMillis() - now > 10000L) {
                fail("Garbage not collected within 10secs");
            }
            Thread.sleep(100L);
        }

        assertThat(BigObjectRegistry.getDefault().isRegistered(id), is(false));
    }

}