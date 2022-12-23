package com.fizzed.bigmap.impl;

import com.fizzed.bigmap.BigObjectCloser;

import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

abstract public class AbstractBigObjectCloser implements BigObjectCloser {

    private final UUID id;
    private final boolean persistent;
    private final Path directory;
    private boolean closed;

    public AbstractBigObjectCloser(UUID id, boolean persistent, Path directory) {
        this.id = id;
        this.persistent = persistent;
        this.directory = directory;
    }

    @Override
    public UUID getId() {
        return this.id;
    }

    @Override
    public boolean isPersistent() {
        return persistent;
    }

    @Override
    public Path getPath() {
        return directory;
    }

    @Override
    public boolean isClosed() {
        return this.closed;
    }

    @Override
    synchronized public void close() throws IOException {
        // do nothing on multiple closes
        if (this.closed) {
            return;
        }

        this.doClose();

        if (!this.persistent) {
            // remove the scratch directory
            BigMapHelper.recursivelyDelete(this.directory);
        }

        this.closed = true;
    }

    abstract public void doClose() throws IOException;

}