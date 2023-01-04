package com.fizzed.bigmap.tokyocabinet;

import com.fizzed.bigmap.BigMap;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@DisabledOnOs(OS.WINDOWS)
public class TokyoBigMapTest {

    @Test
    public void putGetWithStrings() {
        final Map<String,String> map = new TokyoBigMapBuilder<String,String>()
            .setScratchDirectory(Paths.get("target"))
            .setKeyType(String.class)
            .setValueType(String.class)
            .autoCloseObjects()
            .build();

        map.put("a", "1");

        assertThat(map.get("a"), is("1"));
        assertThat(map.size(), is(1));
        assertThat(map.isEmpty(), is(false));

        map.remove("a");

        assertThat(map.get("a"), is(nullValue()));
        assertThat(map.size(), is(0));
        assertThat(map.isEmpty(), is(true));

        String removed = map.put("b", "2");

        assertThat(removed, is(nullValue()));
        assertThat(map.size(), is(1));

        String removed1 = map.put("b", "3");

        assertThat(removed1, is("2"));
        assertThat(map.size(), is(1));
    }

    @Test
    public void close() throws IOException {
        final BigMap<String,String> map = new TokyoBigMapBuilder<String,String>()
            .setScratchDirectory(Paths.get("target"))
            .setKeyType(String.class)
            .setValueType(String.class)
            .build();

        map.put("a", "1");

        map.close();

        assertThat(map.isClosed(), is(true));
        assertThat(Files.exists(map.getPath()), is(false));
    }

}