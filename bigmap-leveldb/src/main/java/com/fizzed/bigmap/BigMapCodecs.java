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

import java.nio.charset.StandardCharsets;

public class BigMapCodecs {
    
    static public final byte[] ZERO_BYTES = new byte[0];

    static public <T> BigMapCodec<T> of(Class<T> type) {
        if (type.isAssignableFrom(String.class)) {
            return (BigMapCodec<T>)strings();
        }
        else {
            return new FSTBigMapCodec(type);
        }
    }
    
    static public BigMapCodec<String> strings() {
        return new BigMapCodec<String>() {
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
 
//    static public BigMapCodec<Long> longs() {
//        return new BigMapCodec<Long>() {
//            @Override
//            public byte[] serialize(Long value) {
//                if (value == null) {
//                    return ZERO_BYTES;
//                }
//                long v = value;
//                return new byte[] {
//                    (byte) v,
//                    (byte) (v >> 8),
//                    (byte) (v >> 16),
//                    (byte) (v >> 24),
//                    (byte) (v >> 32),
//                    (byte) (v >> 40),
//                    (byte) (v >> 48),
//                    (byte) (v >> 56)};
//            }
//
//            @Override
//            public Long deserialize(byte[] bytes) {
//                if (bytes == null) {
//                    return null;
//                }
//                
//                         }
//        };
//    }
    
}