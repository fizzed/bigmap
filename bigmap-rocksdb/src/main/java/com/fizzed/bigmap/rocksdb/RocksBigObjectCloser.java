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
package com.fizzed.bigmap.rocksdb;

import com.fizzed.bigmap.impl.AbstractBigObjectCloser;
import org.rocksdb.RocksDB;

import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

public class RocksBigObjectCloser extends AbstractBigObjectCloser {

    private final RocksDB db;

    public RocksBigObjectCloser(
            UUID id,
            boolean persistent,
            Path directory,
            RocksDB db) {
        
        super(id, persistent, directory);
        this.db = db;
    }

    @Override
    public void doClose() throws IOException {
        this.db.close();
    }

}