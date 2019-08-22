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
package com.fizzed.bigmap.leveldb;

import com.fizzed.crux.util.StopWatch;
import java.io.Serializable;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

public class PerfDemo {
 
    static public class Item implements Serializable {
        Long a;
        String b;
        String c;
        String d;
        String e;
        String f;
        String g;
        Integer h;
    }
    
    static public void printMemory() {
        System.out.println("Memory Used (MB): " + (double) (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024*1024));
    }
    
    static public void main(String[] args) throws Exception {
        final LevelBigMap<String,Item> map = new LevelBigMapBuilder()
            .setScratchDirectory(Paths.get("target"))
            .setKeyType(String.class)
            .setValueType(Item.class)
            .build();

//        final TreeMap<String,Item> map = new TreeMap<>();
//        final Map<String,Item> map = new HashMap<>();
//        final Map<String,Item> map = new LinkedHashMap<>();
        
        printMemory();
        
        StopWatch writeTimer = StopWatch.timeMillis();
        for (int i = 0; i < 100000; i++) {
            if (i % 5000 == 0) {
                System.out.print("@ iteration " + i + " -> ");
                printMemory();
            }
            Item item = new Item();
            item.a = (long)i;
            item.b = "This is sooooo cool dude! " + i;
            item.c = "Look mom no hands " + i;
            item.d = "Woooo baby! " + i;
            item.e = "Woza! " + i;
            item.g = "Blah blah aljlfjalfrjsd;lfjsdlfjsdlafjsdlfjsdlfjsdlfjsldafjlsdfjsdlfjsdlfjsdalfjsdlfjsdlfjdsjf" + i;
            item.h = i;
            
            map.put(i+"", item);
        }
        
        System.out.println("Took "+writeTimer+" to populate map");
        
        // cleanup garbage to make reading harder
        System.gc();
        
        // intensive read back now
        StopWatch readTimer = StopWatch.timeMillis();
        int readCount = 0;
        for (int i = 0; i < map.size(); i+=100) {
            map.get(i+"");
            readCount++;
        }
        
        System.out.println("Took "+readTimer+" to fetch "+readCount+" random items");
        
        System.gc();
        
        System.out.print("Final memory: ");
        printMemory();
        
        // this forces JVM to NOT gc map yet!
        map.get("1");
        
        
//        System.out.println("Map had "+map.getKeyByteSize()+" key bytes and "+map.getValueByteSize()+" value bytes");
//        
//        map.close();
    }
    
}