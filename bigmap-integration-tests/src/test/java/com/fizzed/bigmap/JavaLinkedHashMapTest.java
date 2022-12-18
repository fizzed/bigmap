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

import com.fizzed.bigmap.rocksdb.RocksBigLinkedMapBuilder;
import org.junit.Ignore;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

public class JavaLinkedHashMapTest extends AbstractBigLinkedMapTest {

    @Override
    public <K,V> Map<K, V> newMap(Class<K> keyType, Class<V> valueType) {
        return new LinkedHashMap<>();
    }

    @Test
    @Ignore("nulls are technically allowed with a java hashmap")
    public void putNullKey() {
        super.putNullKey();
    }

    @Test  @Ignore("nulls are technically allowed with a java hashmap")
    public void putNullValue() {
        super.putNullKey();
    }

    @Test @Ignore
    public void putAndGetWithComplexObject() {}

}