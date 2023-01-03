package com.fizzed.bigmap.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.fizzed.bigmap.ByteCodecs;
import com.fizzed.crux.jackson.CruxUtilModule;
import com.fizzed.crux.jackson.EnumStrategyModule;
import com.fizzed.crux.jackson.JavaTimePlusModule;
import com.fizzed.crux.util.Base16;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Objects;
import java.util.TimeZone;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.*;

public class JacksonByteCodecTest {

    static private final ObjectMapper OM1 = new ObjectMapper();
    static private final ObjectMapper OM2 = new ObjectMapper();
    static {
        OM2.enable(SerializationFeature.INDENT_OUTPUT);
        OM2.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        OM2.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        OM2.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        // some objects may not have properties, do not fail
        OM2.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        OM2.setDateFormat(df);

        // secure code, timeuuid, timeduration support
        OM2.registerModule(new CruxUtilModule());
        // lower-case serialized, case insensitive incoming
        OM2.registerModule(new EnumStrategyModule());
        // support for joda datetimes
        OM2.registerModule(new JodaModule());
        // java 8 types
        OM2.registerModule(new Jdk8Module());
        // java 8 parameter name retention in source code (make sure to compile with -parameters annotation!)
        OM2.registerModule(new ParameterNamesModule());
        // this module forces Instants & DateTimes to **always** include zeros for millis
        OM2.registerModule(JavaTimePlusModule.build(true));
    }

    @Test
    public void nullsAndEmptyStrings() {
        final JacksonByteCodec<String> byteCodec = new JacksonByteCodec<>(String.class, OM1);

        assertThat(byteCodec.serialize(null), is(ByteCodecs.ZERO_BYTES));
        assertThat(byteCodec.serialize(""), is(new byte[] {(byte)34, (byte)34}));

        assertThat(byteCodec.deserialize(null), is(nullValue()));
        assertThat(byteCodec.deserialize(ByteCodecs.ZERO_BYTES), is(nullValue()));
        assertThat(byteCodec.deserialize(new byte[] {(byte)34, (byte)34}), is(""));
    }

    @Test
    public void serializableObjects() {
        final JacksonByteCodec<Instant> byteCodec = new JacksonByteCodec<>(Instant.class, OM2);

        final Instant i1 = Instant.parse("2022-11-05T01:02:03.456Z");

        System.out.println(Base16.encode(byteCodec.serialize(i1)));

        assertThat(byteCodec.serialize(i1), is(Base16.decode("22323032322d31312d30355430313a30323a30332e3435365a22")));
        assertThat(byteCodec.deserialize(Base16.decode("22323032322d31312d30355430313a30323a30332e3435365a22")), is(i1));
    }

    @Test
    public void nonSerializableObjects() {
        final JacksonByteCodec<Widget> byteCodec = new JacksonByteCodec<>(Widget.class, OM2);

        final Widget w1 = new Widget()
            .setS("a")
            .setI(5)
            .setType(WidgetType.A)
            .setCreatedAt(Instant.parse("2022-11-12T01:02:03.456Z"));

        byte[] bytes = byteCodec.serialize(w1);
        final Widget w2 = byteCodec.deserialize(bytes);

        assertThat(w2, is(w1));
    }

    static public enum WidgetType {
        A,
        B,
        C;
    }

    static public class Widget {

        private String s;
        private Integer i;
        private WidgetType type;
        private Instant createdAt;

        public String getS() {
            return s;
        }

        public Widget setS(String s) {
            this.s = s;
            return this;
        }

        public Integer getI() {
            return i;
        }

        public Widget setI(Integer i) {
            this.i = i;
            return this;
        }

        public WidgetType getType() {
            return type;
        }

        public Widget setType(WidgetType type) {
            this.type = type;
            return this;
        }

        public Instant getCreatedAt() {
            return createdAt;
        }

        public Widget setCreatedAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Widget widget = (Widget) o;
            return Objects.equals(s, widget.s) && Objects.equals(i, widget.i) && type == widget.type && Objects.equals(createdAt, widget.createdAt);
        }

        @Override
        public int hashCode() {
            return Objects.hash(s, i, type, createdAt);
        }
    }

}