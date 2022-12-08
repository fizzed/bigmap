package com.fizzed.bigmap.rocksdb;

public class RocksKeyValue {

    private final byte[] key;
    private final byte[] value;

    public RocksKeyValue(byte[] key, byte[] value) {
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