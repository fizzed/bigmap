package com.fizzed.bigmap.tkrzw;

import com.fizzed.bigmap.impl.KeyValueBytes;
import tkrzw.DBM;
import tkrzw.Status;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class TkrzwForwardIterator implements Iterator<KeyValueBytes> {

    static public TkrzwForwardIterator build(DBM db) {
        final tkrzw.Iterator iterator = db.makeIterator();
        iterator.first();

        // probe if it has a "first" record
        final boolean hasNext = iterator.getKey() != null;

        // CRITICAL: if there isn't a next record, we need to destruct the iterator
        // so the native resources are cleaned up
        if (!hasNext) {
            iterator.destruct();
        }

        return new TkrzwForwardIterator(iterator, hasNext);
    }

    private final tkrzw.Iterator iter;
    private boolean hasNext;
    // since we need to forward look at the next key/value, we'll keep it
    // locally present so we don't need to get it fetched/allocated from the db again
    private byte[] nextKey;
    private byte[] nextValue;

    public TkrzwForwardIterator(tkrzw.Iterator iter, boolean hasNext) {
        this.iter = iter;
        this.hasNext = hasNext;
        if (hasNext) {
            this.nextKey = iter.getKey();
            this.nextValue = iter.getValue();
        }
    }

    @Override
    public boolean hasNext() {
        return this.hasNext;
    }

    @Override
    public KeyValueBytes next() {
        // NOTE: this throws a NoSuchElementException is no element exists
        if (!hasNext) {
            throw new NoSuchElementException();
        }

        // we are already on the item we want
        final KeyValueBytes kvb = new KeyValueBytes(this.nextKey,  this.nextValue);

        // now we'll iterate to the next ahead of time
        // NOTE: if the current record is missing this operation will fail.
        // if the current record exists, but the next record is missing, this doesn't fail
        this.iter.next();
        this.nextKey = this.iter.getKey();
        this.nextValue = this.iter.getValue();
        this.hasNext = this.nextKey != null;

        // CRITICAL: if there's no "next" value, this iterator is done, and we can clean up resources
        // TODO: what if the iterator isn't fully consumed?
        if (!this.hasNext) {
            this.iter.destruct();
        }

        return kvb;
    }

}