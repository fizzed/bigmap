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

import com.fizzed.bigmap.rocksdb.RocksBigSetBuilder;
import com.fizzed.jne.HardwareArchitecture;
import com.fizzed.jne.NativeTarget;
import com.fizzed.jne.OperatingSystem;
import org.junit.jupiter.api.condition.DisabledIf;

import java.nio.file.Paths;
import java.util.Set;

@DisabledIf("isUnsupportedOs")
public class RocksBigSetTest extends AbstractBigSetTest {

    static public boolean isUnsupportedOs() {
        final NativeTarget current = NativeTarget.detect();
        return current.getOperatingSystem() == OperatingSystem.FREEBSD
            || current.getOperatingSystem() == OperatingSystem.OPENBSD
            || (current.getOperatingSystem() == OperatingSystem.WINDOWS && current.getHardwareArchitecture() == HardwareArchitecture.ARM64)
            || (current.getOperatingSystem() == OperatingSystem.LINUX && current.getHardwareArchitecture() == HardwareArchitecture.RISCV64);
    }

    @Override
    public <V> Set<V> newSet(Class<V> valueType) {
        return new RocksBigSetBuilder<V>()
            .setScratchDirectory(Paths.get("target"))
            .setValueType(valueType)
            .autoCloseObjects()
            .build();
    }

}