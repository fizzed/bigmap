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

import com.fizzed.bigmap.impl.BigMapEntrySet;
import com.fizzed.bigmap.impl.BigMapKeySet;
import com.fizzed.bigmap.impl.BigMapValueCollection;

import java.io.Closeable;
import java.nio.file.Path;
import java.util.*;

public interface BigObject extends Closeable {

    UUID getId();

    Path getDirectory();

    void setListener(BigObjectListener listener);

    BigObjectListener getListener();

    void open();

    BigObjectCloser getCloser();

    default void checkIfClosed() {
        if (this.isClosed()) {
            throw new IllegalStateException("Underlying database is closed. Unable to perform any operations.");
        }
    }

    boolean isClosed();

    boolean isPersistent();

}