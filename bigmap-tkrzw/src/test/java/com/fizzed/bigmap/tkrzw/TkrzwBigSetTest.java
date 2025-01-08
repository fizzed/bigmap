package com.fizzed.bigmap.tkrzw;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.nio.file.Paths;
import java.util.Set;

@DisabledOnOs({ OS.FREEBSD, OS.OPENBSD })
public class TkrzwBigSetTest {

    @Test
    public void putGetWithStrings() {
        final Set<String> set = new TkrzwBigSetBuilder<String>()
            .setScratchDirectory(Paths.get("target"))
            .setValueType(String.class)
            .autoCloseObjects()
            .build();
    }

}