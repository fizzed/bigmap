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
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

import static com.fizzed.bigmap.impl.BigMapHelper.*;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeThat;

abstract public class AbstractBigLinkedSetTest extends AbstractBigSetTest {

    @Test @Ignore
    public void byteSizeTracking() {
        // we want to just ignore this
    }

    @Test
    public void addOrdered() {
        final Set<Long> set = this.newSet(Long.class);

        set.add(5L);
        set.add(1L);
        set.add(3L);
        set.add(2L);

        assertThat(toValueList(set), hasItems(5L, 1L, 3L, 2L));

        // adding a value does not change its insert ordering
        set.add(5L);

        assertThat(toValueList(set), hasItems(5L, 1L, 3L, 2L));
    }

    @Test
    public void removeOrdered() throws IOException {
        final Set<String> set = this.newSet(String.class);

        set.add("4");
        set.add("1");
        set.add("3");
        set.add("2");

        assertThat(set, hasSize(4));
        assertThat(toValueList(set), hasItems("4", "1", "3", "2"));

        boolean removed;

        removed = set.remove("1");

        assertThat(removed, is(true));
        assertThat(toValueList(set), hasItems("4", "3", "2"));

        // try to remove it again
        removed = set.remove("1");

        assertThat(removed, is(false));
        assertThat(toValueList(set), hasItems("4", "3", "2"));

        removed = set.remove("4");

        assertThat(removed, is(true));
        assertThat(toValueList(set), hasItems("3", "2"));
    }

}