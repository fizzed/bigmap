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
package com.fizzed.bigmap.impl;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map.Entry;

import static java.util.stream.Collectors.toList;

public class BigMapHelper {

    static public <K> List<K> toKeyList(Map<K,?> map) {
        return map.keySet().stream().collect(toList());
    }

    static public <V> List<V> toValueList(Map<?,V> map) {
        return map.values().stream().collect(toList());
    }

    static public <V> List<V> toIteratedList(Iterator<V> it, int count) {
        List<V> items = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            items.add(it.next());
        }
        return items;
    }

    static public <K,V> List<K> toIteratedKeyList(Iterator<Entry<K,V>> it, int count) {
        List<K> items = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            items.add(it.next().getKey());
        }
        return items;
    }

    static public long sizeOf(byte[] b) {
        return b != null ? b.length : 0;
    }

    static public Path resolveScratchDirectory(Path scratchDirectory, boolean persistent, String nonPersistentPrefixName) {
        final Path resolvedScratchDir = scratchDirectory != null ? scratchDirectory : Paths.get(".");

        Path directory = resolvedScratchDir;
        if (!persistent) {
            String nonPersistentName = UUID.randomUUID().toString();

            if (nonPersistentPrefixName != null) {
                nonPersistentName = nonPersistentPrefixName + "-" + nonPersistentName;
            }

            directory = resolvedScratchDir.resolve(nonPersistentName);
        }

        return directory;
    }
    
}