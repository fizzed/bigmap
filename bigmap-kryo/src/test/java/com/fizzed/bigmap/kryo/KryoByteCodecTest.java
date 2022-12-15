package com.fizzed.bigmap.kryo;

import com.fizzed.bigmap.ByteCodecs;
import com.fizzed.crux.util.Base16;
import org.junit.Test;

import java.time.Instant;
import java.util.Objects;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.*;

public class KryoByteCodecTest {

    @Test
    public void nullsAndEmptyStrings() {
        final KryoByteCodec<String> byteCodec = new KryoByteCodec<>(String.class);

        assertThat(byteCodec.serialize(null), is(ByteCodecs.ZERO_BYTES));
        assertThat(byteCodec.serialize(""), is(new byte[] {(byte)-127}));

        assertThat(byteCodec.deserialize(null), is(nullValue()));
        assertThat(byteCodec.deserialize(ByteCodecs.ZERO_BYTES), is(nullValue()));
        assertThat(byteCodec.deserialize(new byte[] {(byte)-127}), is(""));
    }

    @Test
    public void serializableObjects() {
        final KryoByteCodec<Instant> byteCodec = new KryoByteCodec<>(Instant.class);

        final Instant i1 = Instant.parse("2022-11-05T01:02:03.456Z");

        //System.out.println(Base16.encode(byteCodec.serialize(i1)));

        assertThat(byteCodec.serialize(i1), is(Base16.decode("018bec969b068084b8d901")));
        assertThat(byteCodec.deserialize(Base16.decode("018bec969b068084b8d901")), is(i1));
    }

    @Test
    public void nonSerializableObjects() {
        final KryoByteCodec<Widget> byteCodec = new KryoByteCodec<>(Widget.class);

        final Widget w1 = new Widget()
            .setS("a")
            .setI(5)
            .setType(WidgetType.A)
            .setCreatedAt(Instant.parse("2022-11-12T01:02:03.456Z"));

        //System.out.println(Base16.encode(byteCodec.serialize(w1)));

        assertThat(byteCodec.serialize(w1), is(Base16.decode("01018be1bb9b068084b8d901010a826101")));
        assertThat(byteCodec.deserialize(Base16.decode("01018be1bb9b068084b8d901010a826101")), is(w1));
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