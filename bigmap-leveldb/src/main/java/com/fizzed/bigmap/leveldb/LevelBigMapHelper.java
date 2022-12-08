package com.fizzed.bigmap.leveldb;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class LevelBigMapHelper {

    static Path prepFolderDirectoryPath(Path scratchDirectory, boolean persistent, String nonPersistentPrefixName) {

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