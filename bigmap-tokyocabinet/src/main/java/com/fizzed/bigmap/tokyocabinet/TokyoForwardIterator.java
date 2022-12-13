package com.fizzed.bigmap.tokyocabinet;

import com.fizzed.bigmap.KeyValueBytes;
import tokyocabinet.BDB;
import tokyocabinet.BDBCUR;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class TokyoForwardIterator implements Iterator<KeyValueBytes> {

    static public TokyoForwardIterator build(BDB db) {
        BDBCUR cursor = new BDBCUR(db);
        boolean hasNext = cursor.first();
        return new TokyoForwardIterator(cursor, hasNext);
    }

    private final BDBCUR cursor;
    private boolean hasNext;

    public TokyoForwardIterator(BDBCUR cursor, boolean hasNext) {
        this.cursor = cursor;
        this.hasNext = hasNext;
    }

    public boolean hasNext() {
        return this.hasNext;
    }

    public KeyValueBytes next() {
        // NOTE: this throws a NoSuchElementException is no element exists
        if (!hasNext) {
            throw new NoSuchElementException();
        }

        // we are already on the item we want
        final KeyValueBytes kvb = new KeyValueBytes( this.cursor.key(),  this.cursor.val());

        // now we'll iterate to the next
        this.hasNext = this.cursor.next();

        return kvb;
    }

}