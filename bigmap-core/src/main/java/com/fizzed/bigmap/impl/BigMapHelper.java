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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Stream;

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

    static public void recursivelyDelete(Path directoryOrFile) {
        if (directoryOrFile == null) {
            return;
        }
        try {
            if (Files.exists(directoryOrFile)) {
                // if just a normal file, delete it
                if (Files.isRegularFile(directoryOrFile)) {
                    Files.delete(directoryOrFile);
                    return;
                }

                // otherwise, its a directory and we need to recursively walk it
                try (Stream<Path> files = Files.list(directoryOrFile)) {
                    files.forEach(file -> {
                        try {
                            Files.delete(file);
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
                } catch (Exception e) {
                    throw new RuntimeException("Unable to delete existing database file!", e);
                }
                // now cleanup the directory too
                try {
                    Files.delete(directoryOrFile);
                } catch (IOException e) {
                    throw new UncheckedIOException("Unable to delete existing database file!", e);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to list existing database directory!", e);
        }
    }

    static public Path resolveTempDirectory() {
        return Paths.get(System.getProperty("java.io.tmpdir"));
    }

    static public Path resolveScratchDirectory(Path scratchDirectory, boolean persistent, UUID id, String nonPersistentPrefixName) {
        Objects.requireNonNull(id, "id was null");

        final Path resolvedScratchDir = scratchDirectory != null ? scratchDirectory : Paths.get(".");

        Path directory = resolvedScratchDir;
        if (!persistent) {
            String nonPersistentName = id.toString();

            if (nonPersistentPrefixName != null) {
                nonPersistentName = nonPersistentPrefixName + "-" + nonPersistentName;
            }

            directory = resolvedScratchDir.resolve(nonPersistentName);
        }

        return directory;
    }
    
}