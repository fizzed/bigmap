package com.fizzed.bigmap.kryo;

import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;
import com.fizzed.bigmap.ByteCodec;

public class KryoByteCodec<K> implements ByteCodec<K> {

    private final Kryo kryo;
    private final Class<K> type;

    public KryoByteCodec(Class<K> type) {
        this.kryo = new Kryo();
        this.type = type;
        this.kryo.register(type);
    }

    @Override
    public byte[] serialize(K value) {
        final Output output = new Output(1024, -1);
        this.kryo.writeObjectOrNull(output, value, this.type);
        return output.toBytes();
    }

    @Override
    public K deserialize(byte[] bytes) {
        final Input input = new Input(bytes);
        return this.kryo.readObjectOrNull(input, this.type);
    }

}