package com.fizzed.bigmap.rocksdb;

import com.fizzed.bigmap.KeyValueBytes;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksIterator;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class RocksForwardIterator implements Iterator<KeyValueBytes> {

    static public RocksForwardIterator build(RocksDB db) {
        final RocksIterator iter = db.newIterator();
        iter.seekToFirst();
        return new RocksForwardIterator(iter);
    }

    private final org.rocksdb.RocksIterator it;

    public RocksForwardIterator(RocksIterator it) {
        this.it = it;
    }

    public boolean hasNext() {
        return it.isValid();
    }

    public KeyValueBytes next() {
        // NOTE: this throws a NoSuchElementException is no element exists
        if (!it.isValid()) {
            throw new NoSuchElementException();
        }

        // we are already on the item we want
        final KeyValueBytes kvb = new KeyValueBytes(it.key(), it.value());

        // now we'll iterate to the next
        it.next();

        return kvb;
    }

}