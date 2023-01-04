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

import com.fizzed.crux.util.Base16;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class BytesTest {
 
    @Test
    public void startsWith() {
        byte[] a = Base16.decode("01");
        byte[] b = Base16.decode("0102030405060708");
        
        assertThat(Bytes.startsWith(null, null), is(true));
        assertThat(Bytes.startsWith(a, null), is(false));
        assertThat(Bytes.startsWith(a, Base16.decode("00")), is(false));
        assertThat(Bytes.startsWith(a, Base16.decode("01")), is(true));
        assertThat(Bytes.startsWith(a, Base16.decode("0102")), is(false));
        assertThat(Bytes.startsWith(b, Base16.decode("0102")), is(true));
        assertThat(Bytes.startsWith(Base16.decode("0FF0FF"), Base16.decode("0FF0FF")), is(true));
    }
    
    @Test
    public void startsWithWildcard() {
        byte[] a = Base16.decode("01");
        byte[] b = Base16.decode("0102030405060708");
        
        assertThat(Bytes.startsWith(a, Base16.decode("00"), (byte)1), is(false));
        assertThat(Bytes.startsWith(a, Base16.decode("01"), (byte)1), is(true));
        assertThat(Bytes.startsWith(a, Base16.decode("01"), null), is(true));
        assertThat(Bytes.startsWith(a, Base16.decode("0102"), (byte)0), is(false));
        assertThat(Bytes.startsWith(b, Base16.decode("010003"), (byte)1), is(false));
        assertThat(Bytes.startsWith(b, Base16.decode("010003"), (byte)0), is(true));
        assertThat(Bytes.startsWith(b, Base16.decode("010203"), (byte)0), is(true));
    }
 
    @Test
    public void endsWith() {
        byte[] a = Base16.decode("01");
        byte[] b = Base16.decode("0102030405060708");
        
        assertThat(Bytes.endsWith(null, null), is(true));
        assertThat(Bytes.endsWith(a, null), is(false));
        assertThat(Bytes.endsWith(a, Base16.decode("00")), is(false));
        assertThat(Bytes.endsWith(a, Base16.decode("01")), is(true));
        assertThat(Bytes.endsWith(a, Base16.decode("0001")), is(false));
        assertThat(Bytes.endsWith(b, Base16.decode("0708")), is(true));
        assertThat(Bytes.endsWith(Base16.decode("0FF0FF"), Base16.decode("0FF0FF")), is(true));
    }
 
    @Test
    public void endsWithWildcard() {
        byte[] a = Base16.decode("01");
        byte[] b = Base16.decode("0102030405060708");
        
        assertThat(Bytes.endsWith(null, null), is(true));
        assertThat(Bytes.endsWith(a, null), is(false));
        assertThat(Bytes.endsWith(a, Base16.decode("00")), is(false));
        assertThat(Bytes.endsWith(a, Base16.decode("01")), is(true));
        assertThat(Bytes.endsWith(a, Base16.decode("0001")), is(false));
        assertThat(Bytes.endsWith(b, Base16.decode("0707"), (byte)0), is(false));
        assertThat(Bytes.endsWith(b, Base16.decode("0707"), (byte)7), is(true));
        assertThat(Bytes.endsWith(b, Base16.decode("0708"), (byte)2), is(true));
        assertThat(Bytes.endsWith(Base16.decode("0FF0FF"), Base16.decode("0FFFFF"), (byte)0xFF), is(true));
    }
    
}