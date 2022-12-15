package com.fizzed.bigmap.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fizzed.bigmap.ByteCodec;

import java.io.IOException;
import java.io.UncheckedIOException;

import static com.fizzed.bigmap.ByteCodecs.ZERO_BYTES;

public class JacksonByteCodec<K> implements ByteCodec<K> {

    private final ObjectMapper objectMapper;
    private final Class<K> type;

    public JacksonByteCodec(ObjectMapper objectMapper, Class<K> type) {
        this.objectMapper = objectMapper;
        this.type = type;
    }

    @Override
    public byte[] serialize(K value) {
        if (value == null) {
            return ZERO_BYTES;
        }
        try {
            return this.objectMapper.writeValueAsBytes(value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public K deserialize(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            return this.objectMapper.readValue(bytes, type);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}