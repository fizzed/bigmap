package com.fizzed.bigmap;

public class KeyValueBytes {

    private final byte[] key;
    private final byte[] value;

    public KeyValueBytes(byte[] key, byte[] value) {
        this.key = key;
        this.value = value;
    }

    public byte[] getKey() {
        return key;
    }

    public byte[] getValue() {
        return value;
    }

}