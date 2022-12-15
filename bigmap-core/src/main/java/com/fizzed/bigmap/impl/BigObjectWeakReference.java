package com.fizzed.bigmap.impl;

import com.fizzed.bigmap.BigObject;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BigObjectWeakReference extends WeakReference<BigObject> {

    private final UUID id;

    public BigObjectWeakReference(BigObject bigObject, ReferenceQueue<BigObject> referenceQueue) {
        super (bigObject, referenceQueue);
        this.id = bigObject.getId();
    }

    public UUID getId() {
        return id;
    }

}