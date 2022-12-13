package com.fizzed.bigmap.leveldb;

import com.fizzed.bigmap.KeyValueBytes;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBIterator;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

public class LevelForwardIterator implements Iterator<KeyValueBytes> {

    static public LevelForwardIterator build(DB db) {
        final DBIterator iter = db.iterator();
        iter.seekToFirst();
        return new LevelForwardIterator(iter);
    }

    private final DBIterator it;

    public LevelForwardIterator(DBIterator it) {
        this.it = it;
    }

    public boolean hasNext() {
        return it.hasNext();
    }

    public KeyValueBytes next() {
        // NOTE: this throws a NoSuchElementException is no element exists
        if (!it.hasNext()) {
            throw new NoSuchElementException();
        }

        // we are already on the item we want
        final Map.Entry<byte[],byte[]> entry = it.next();

        final KeyValueBytes kvb = new KeyValueBytes(entry.getKey(), entry.getValue());

        return kvb;
    }

}