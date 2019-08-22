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
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class LevelBigMapTest {
 
    @Test
    public void putGetWithStrings() {
        LevelBigMap<String,String> map = new LevelBigMapBuilder()
            .setDirectory(Paths.get("target"))
            .build(String.class, String.class);
        
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
        
        String removed1 = map.put("b", "3");
        
        assertThat(removed1, is("2"));
    }
 
    @Test
    public void putGetWithLongs() {
        LevelBigMap<Long,String> map = new LevelBigMapBuilder()
            .setDirectory(Paths.get("target"))
            .build(Long.class, String.class);
        
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
    
}
