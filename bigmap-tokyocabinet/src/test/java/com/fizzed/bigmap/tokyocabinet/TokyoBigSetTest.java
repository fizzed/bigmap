package com.fizzed.bigmap.tokyocabinet;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.nio.file.Paths;
import java.util.Set;

@DisabledOnOs({ OS.WINDOWS, OS.FREEBSD, OS.OPENBSD })
public class TokyoBigSetTest {

    @Test
    public void putGetWithStrings() {
        final Set<String> set = new TokyoBigSetBuilder<String>()
            .setScratchDirectory(Paths.get("target"))
            .setValueType(String.class)
            .autoCloseObjects()
            .build();
    }

}