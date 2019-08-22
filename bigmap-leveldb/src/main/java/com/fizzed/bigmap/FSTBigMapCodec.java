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

import org.nustaq.serialization.FSTConfiguration;

public class FSTBigMapCodec<K> implements BigMapCodec<K> {

    // shared instance...
    static public final FSTConfiguration FST = FSTConfiguration.createDefaultConfiguration();
    
    public FSTBigMapCodec(Class<K> type) {
        FST.registerClass(type);
    }
    
    @Override
    public byte[] serialize(K value) {
        return FST.asByteArray(value);
    }

    @Override
    public K deserialize(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        return (K)FST.asObject(bytes);
    }
    
}