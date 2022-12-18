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

import com.fizzed.bigmap.BigObjectRegistry;

import java.nio.file.Path;

import static com.fizzed.bigmap.ByteCodecs.resolveCodec;

abstract public class AbstractBigObjectBuilder<T> {

    protected Path scratchDirectory;
    protected String name;
    protected BigObjectRegistry registry;

    public AbstractBigObjectBuilder() {
        this.scratchDirectory = BigMapHelper.resolveTempDirectory().resolve("bigobjects");
    }

    public T autoCloseObjects() {
        this.autoCloseObjects(BigObjectRegistry.getDefault());
        return (T)this;
    }

    public T autoCloseObjects(BigObjectRegistry registry) {
        this.registry = registry;
        return (T)this;
    }

    public T setScratchDirectory(Path scratchDirectory) {
        this.scratchDirectory = scratchDirectory;
        return (T)this;
    }

    public T setName(String name) {
        this.name = name;
        return (T)this;
    }

}