/*
 * Copyright 2019 Fizzed, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.fizzed.bigmap;

import com.fizzed.crux.util.Base16;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

public class ByteCodecsTest {
 
    @Test
    public void resolveCodec() {
        assertThat(ByteCodecs.resolveCodec(String.class), is(not(nullValue())));
    }
    
    @Test
    public void utf8StringCodec() {
        ByteCodec<String> c = ByteCodecs.resolveCodec(String.class);
        
        assertThat(c.serialize("a"), is("a".getBytes(StandardCharsets.UTF_8)));
        assertThat(c.deserialize("a".getBytes(StandardCharsets.UTF_8)), is("a"));
        assertThat(c.serialize("€"), is("€".getBytes(StandardCharsets.UTF_8)));
        assertThat(c.deserialize("€".getBytes(StandardCharsets.UTF_8)), is("€"));
    }
    
    @Test
    public void integerCodec() {
        ByteCodec<Integer> c = ByteCodecs.resolveCodec(Integer.class);
        
        assertThat(c.serialize(0), is(Ints.toByteArray(0)));
        assertThat(c.deserialize(Ints.toByteArray(0)), is(0));
        assertThat(c.serialize(1), is(Ints.toByteArray(1)));
        assertThat(c.deserialize(Ints.toByteArray(1)), is(1));
        assertThat(c.serialize(-1), is(Ints.toByteArray(-1)));
        assertThat(c.deserialize(Ints.toByteArray(-1)), is(-1));
        assertThat(c.serialize(Integer.MIN_VALUE), is(Ints.toByteArray(Integer.MIN_VALUE)));
        assertThat(c.deserialize(Ints.toByteArray(Integer.MIN_VALUE)), is(Integer.MIN_VALUE));
        assertThat(c.serialize(Integer.MAX_VALUE), is(Ints.toByteArray(Integer.MAX_VALUE)));
        assertThat(c.deserialize(Ints.toByteArray(Integer.MAX_VALUE)), is(Integer.MAX_VALUE));
    }
    
    @Test
    public void longCodec() {
        ByteCodec<Long> c = ByteCodecs.resolveCodec(Long.class);
        
        assertThat(c.serialize(0L), is(Longs.toByteArray(0L)));
        assertThat(c.deserialize(Longs.toByteArray(0L)), is(0L));
        assertThat(c.serialize(1L), is(Longs.toByteArray(1L)));
        assertThat(c.deserialize(Longs.toByteArray(1L)), is(1L));
        assertThat(c.serialize(-1L), is(Longs.toByteArray(-1L)));
        assertThat(c.deserialize(Longs.toByteArray(-1L)), is(-1L));
        assertThat(c.serialize(Long.MIN_VALUE), is(Longs.toByteArray(Long.MIN_VALUE)));
        assertThat(c.deserialize(Longs.toByteArray(Long.MIN_VALUE)), is(Long.MIN_VALUE));
        assertThat(c.serialize(Long.MAX_VALUE), is(Longs.toByteArray(Long.MAX_VALUE)));
        assertThat(c.deserialize(Longs.toByteArray(Long.MAX_VALUE)), is(Long.MAX_VALUE));
    }
    
    @Test
    public void shortCodec() {
        ByteCodec<Short> c = ByteCodecs.resolveCodec(Short.class);
        
        assertThat(c.serialize((short)0), is(Base16.decode("0000")));
        assertThat(c.deserialize(Base16.decode("0000")), is((short)0));
        assertThat(c.serialize((short)1), is(Base16.decode("0001")));
        assertThat(c.deserialize(Base16.decode("0001")), is((short)1));
        assertThat(c.serialize((short)-1), is(Base16.decode("FFFF")));
        assertThat(c.deserialize(Base16.decode("FFFF")), is((short)-1));
        assertThat(c.serialize(Short.MAX_VALUE), is(Base16.decode("7FFF")));
        assertThat(c.deserialize(Base16.decode("7FFF")), is((short)Short.MAX_VALUE));
        assertThat(c.serialize(Short.MIN_VALUE), is(Base16.decode("8000")));
        assertThat(c.deserialize(Base16.decode("8000")), is((short)Short.MIN_VALUE));
    }

    @Test
    public void byteCodec() {
        ByteCodec<Byte> c = ByteCodecs.resolveCodec(Byte.class);

        assertThat(c.serialize((byte)0), is(Base16.decode("00")));
        assertThat(c.deserialize(Base16.decode("00")), is((byte)0));
        assertThat(c.serialize((byte)1), is(Base16.decode("01")));
        assertThat(c.deserialize(Base16.decode("01")), is((byte)1));
        assertThat(c.serialize((byte)-1), is(Base16.decode("FF")));
    }

    @Test
    public void byteArrayCodec() {
        ByteCodec<byte[]> c = ByteCodecs.resolveCodec(byte[].class);

        final byte[] bytes1 = Base16.decode("0001020304050607");

        assertThat(c.serialize(bytes1), is(Base16.decode("0001020304050607")));
        assertThat(c.deserialize(Base16.decode("0001020304050607")), is(bytes1));
    }

    @Test
    public void serializableCodec() {
        // this should be the fallback serializable codec
        ByteCodec<Instant> c = ByteCodecs.resolveCodec(Instant.class);

        assertThat(c, instanceOf(SerializableByteCodec.class));

        final Instant i1 = Instant.parse("2022-11-01T01:02:03.456Z");

        assertThat(c.serialize(i1), is(Base16.decode("aced00057372000d6a6176612e74696d652e536572955d84ba1b2248b20c00007870770d02000000006360700b1b2e020078")));
        assertThat(c.deserialize(Base16.decode("aced00057372000d6a6176612e74696d652e536572955d84ba1b2248b20c00007870770d02000000006360700b1b2e020078")), is(i1));
    }
    
}