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

import com.fizzed.bigmap.impl.None;

import java.nio.charset.StandardCharsets;

public class ByteCodecs {
    
    static public final byte[] ZERO_BYTES = new byte[0];

    static public <T> ByteCodec<T> autoCodec(Class<T> type) {

        if (String.class.isAssignableFrom(type)) {
            return (ByteCodec<T>)utf8StringCodec();
        }
        else if (Integer.class.isAssignableFrom(type)) {
            return (ByteCodec<T>)integerCodec();
        }
        else if (Long.class.isAssignableFrom(type)) {
            return (ByteCodec<T>)longCodec();
        }
        else if (Short.class.isAssignableFrom(type)) {
            return (ByteCodec<T>)shortCodec();
        }
        else if (Byte.class.isAssignableFrom(type)) {
            return (ByteCodec<T>)byteCodec();
        }
        else if (byte[].class.isAssignableFrom(type)) {
            return (ByteCodec<T>)byteArrayCodec();
        }
        else if (None.class.isAssignableFrom(type)) {
            return (ByteCodec<T>)noneCodec();
        }
        else {
            return new SerializableByteCodec<>();
            //return new FSTByteCodec(type);
        }
    }
    
    /*static public <T> ByteCodec<T> fstObjectCodec() {
        return new FSTByteCodec<>();
    }*/
    
    static public ByteCodec<String> utf8StringCodec() {
        return new ByteCodec<String>() {
            @Override
            public byte[] serialize(String value) {
                if (value == null) {
                    return ZERO_BYTES;
                }
                return value.getBytes(StandardCharsets.UTF_8);
            }

            @Override
            public String deserialize(byte[] bytes) {
                if (bytes == null) {
                    return null;
                }
                return new String(bytes, StandardCharsets.UTF_8);
            }
        };
    }
    
    static public ByteCodec<String> utf8CiStringCodec() {
        return new ByteCodec<String>() {
            @Override
            public byte[] serialize(String value) {
                if (value == null) {
                    return ZERO_BYTES;
                }
                return value.toLowerCase().getBytes(StandardCharsets.UTF_8);
            }

            @Override
            public String deserialize(byte[] bytes) {
                if (bytes == null) {
                    return null;
                }
                return new String(bytes, StandardCharsets.UTF_8);
            }
        };
    }
    
    static public ByteCodec<Short> shortCodec() {
        return new ByteCodec<Short>() {
            @Override
            public byte[] serialize(Short value) {
                if (value == null) {
                    return ZERO_BYTES;
                }
                int v = value;
                return new byte[] {
                    (byte)(v >> 8),
                    (byte)(v)};
            }

            @Override
            public Short deserialize(byte[] bytes) {
                if (bytes == null) {
                    return null;
                }
                return (short)(((short)bytes[1] & 0xff)
                     | ((short)bytes[0] & 0xff) << 8);
            }
        };
    }
    
    static public ByteCodec<Integer> integerCodec() {
        return new ByteCodec<Integer>() {
            @Override
            public byte[] serialize(Integer value) {
                if (value == null) {
                    return ZERO_BYTES;
                }
                int v = value;
                return new byte[] {
                    (byte)(v >> 24),
                    (byte)(v >> 16),
                    (byte)(v >> 8),
                    (byte)(v)};
            }

            @Override
            public Integer deserialize(byte[] bytes) {
                if (bytes == null) {
                    return null;
                }
                return ((int)bytes[3] & 0xff)
                     | ((int)bytes[2] & 0xff) << 8
                     | ((int)bytes[1] & 0xff) << 16
                     | ((int)bytes[0] & 0xff) << 24;
            }
        };
    }
    
    static public ByteCodec<Long> longCodec() {
        return new ByteCodec<Long>() {
            @Override
            public byte[] serialize(Long value) {
                if (value == null) {
                    return ZERO_BYTES;
                }
                long v = value;
                return new byte[] {
                    (byte)(v >> 56),
                    (byte)(v >> 48),
                    (byte)(v >> 40),
                    (byte)(v >> 32),
                    (byte)(v >> 24),
                    (byte)(v >> 16),
                    (byte)(v >> 8),
                    (byte)(v)};
            }

            @Override
            public Long deserialize(byte[] bytes) {
                if (bytes == null) {
                    return null;
                }
                if (bytes.length != 8) {
                    throw new IllegalArgumentException("Byte array did not contain 8 bytes (NOT a long)");
                }
                return ((long)bytes[7] & 0xff)
                     | ((long)bytes[6] & 0xff) << 8
                     | ((long)bytes[5] & 0xff) << 16
                     | ((long)bytes[4] & 0xff) << 24
                     | ((long)bytes[3] & 0xff) << 32
                     | ((long)bytes[2] & 0xff) << 40
                     | ((long)bytes[1] & 0xff) << 48
                     | ((long)bytes[0] & 0xff) << 56;
            }
        };
    }

    static public ByteCodec<Byte> byteCodec() {
        return new ByteCodec<Byte>() {
            @Override
            public byte[] serialize(Byte value) {
                if (value == null) {
                    return ZERO_BYTES;
                }
                long v = value;
                return new byte[] {(byte)(v)};
            }

            @Override
            public Byte deserialize(byte[] bytes) {
                if (bytes == null) {
                    return null;
                }
                if (bytes.length != 1) {
                    throw new IllegalArgumentException("Byte array did not contain 1 byte");
                }
                return Byte.valueOf((byte)(bytes[0] & 0xff));
            }
        };
    }
    
    static public ByteCodec<byte[]> byteArrayCodec() {
        return new ByteCodec<byte[]>() {
            @Override
            public byte[] serialize(byte[] value) {
                return value;
            }

            @Override
            public byte[] deserialize(byte[] bytes) {
                return bytes;
            }
        };
    }

    static public ByteCodec<None> noneCodec() {
        return new ByteCodec<None>() {
            @Override
            public byte[] serialize(None value) {
                return ZERO_BYTES;
            }

            @Override
            public None deserialize(byte[] bytes) {
                return None.NONE;
            }
        };
    }

}