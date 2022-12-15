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
package com.fizzed.bigmap.impl;

import com.fizzed.bigmap.BigMap;

import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

public class BigLinkedMapCloser extends AbstractBigObjectCloser {

    private final BigMap dataMap;
    private final BigMap insertOrderToKeyMap;
    private final BigMap keyToInsertOrderMap;

    public BigLinkedMapCloser(
            UUID id,
            boolean persistent,
            Path directory,
            BigMap dataMap,
            BigMap insertOrderToKeyMap,
            BigMap keyToInsertOrderMap) {
        
        super(id, persistent, directory);
        this.dataMap = dataMap;
        this.insertOrderToKeyMap = insertOrderToKeyMap;
        this.keyToInsertOrderMap = keyToInsertOrderMap;
    }

    @Override
    public void doClose() throws IOException {
        this.dataMap.close();
        this.insertOrderToKeyMap.close();
        this.keyToInsertOrderMap.close();
    }

}