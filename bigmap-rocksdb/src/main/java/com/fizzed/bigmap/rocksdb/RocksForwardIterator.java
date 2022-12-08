package com.fizzed.bigmap.rocksdb;

import org.rocksdb.RocksIterator;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class RocksForwardIterator {

    private final org.rocksdb.RocksIterator it;

    public RocksForwardIterator(RocksIterator it) {
        this.it = it;
    }

    public boolean hasNext() {
        return it.isValid();
    }

    public RocksKeyValue next() {
        // NOTE: this throws a NoSuchElementException is no element exists
        if (!it.isValid()) {
            throw new NoSuchElementException();
        }

        // we are already on the item we want
        final RocksKeyValue kv = new RocksKeyValue(it.key(), it.value());

        // now we'll iterate to the next
        it.next();

        return kv;
    }

}