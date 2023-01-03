package com.fizzed.bigmap.tokyocabinet;

import com.fizzed.bigmap.BigMap;
import org.junit.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

@DisabledOnOs(OS.WINDOWS)
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