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

import com.fizzed.bigmap.ByteCodec;
import com.fizzed.bigmap.ByteCodecs;
import com.fizzed.crux.util.StopWatch;
import org.h2.mvstore.MVStore;
import oshi.SystemInfo;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Paths;
import java.util.*;

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

    static OperatingSystem os = new SystemInfo().getOperatingSystem();

    static public void printMemory() {
        System.out.println("Memory Used (MB): " + (double) (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024*1024));

        OSProcess process = os.getCurrentProcess();
        long rss = process.getResidentSetSize();

        System.out.println("PID " + process.getProcessID() + ", RSS (MB): " + (double)(rss/(1024*1024)));
    }

    static public Map<String,Item> buildMap() {
//        final Map<String,Item> map = new LevelBigMapBuilder()
//            .setScratchDirectory(Paths.get("target"))
//            .setCacheSize(2 * 1024 * 1024)
//            .setKeyType(String.class)
//            .setValueType(Item.class)
//            .build();

//        final Map<String,Item> map = new LevelBigLinkedMapBuilder()
//            .setScratchDirectory(Paths.get("target"))
//            .setCacheSize(2 * 1048576L)
//            .setKeyType(String.class)
//            .setValueType(Item.class)
//            .build();

//        final TreeMap<String,Item> map = new TreeMap<>();
//        final Map<String,Item> map = new HashMap<>();
//        final Map<String,Item> map = new LinkedHashMap<>();

        // open the store (in-memory if fileName is null)
        // create/get the map named "data"
//        final Map<String,Item> map = MVSTORE.openMap("data-"+UUID.randomUUID());

        final Map<String,Item> map = new PerfRocksDBMap<>(ByteCodecs.utf8StringCodec(), ByteCodecs.autoCodec(Item.class));
//            .setScratchDirectory(Paths.get("target"))
//            .setCacheSize(2 * 1024 * 1024)
//            .setKeyType(String.class)
//            .setValueType(Item.class)
//            .build();

        return map;
    }

//    static private MVStore MVSTORE = new MVStore.Builder()
//        .fileName(Paths.get("target/mvstore-"+UUID.randomUUID()).toAbsolutePath().toString())
//        .cacheSize(5)
//        .open();
    
    static public void main(String[] args) throws Exception {
        int mapCount = 10;
        int iterations = 3000000;

        final List<Map<String,Item>> maps = new ArrayList<>(mapCount);
        for (int i = 0; i < mapCount; i++) {
            maps.add(buildMap());
        }

        printMemory();
        
        StopWatch writeTimer = StopWatch.timeMillis();

        for (int j = 0; j < mapCount; j++) {
            Map<String,Item> map = maps.get(j);
            for (int i = 0; i < iterations; i++) {
                if (i % 5000 == 0) {
                    System.out.print("map #" + j + " @ iteration " + i + " -> ");
                    printMemory();
                }
                Item item = new Item();
                item.a = (long) i;
                item.b = "This is sooooo cool dude! " + i;
                item.c = "Look mom no hands " + i;
                item.d = "Woooo baby! " + i;
                item.e = "Woza! " + i;
                item.g = "Blah blah aljlfjalfrjsd;lfjsdlfjsdlafjsdlfjsdlfjsdlfjsldafjlsdfjsdlfjsdlfjsdalfjsdlfjsdlfjdsjf" + i;
                item.h = i;

                map.put(i + "", item);
            }
        }
        
        System.out.println("Took "+writeTimer+" to populate map");
        
        // cleanup garbage to make reading harder
        System.gc();
        
        // intensive read back now
        StopWatch readTimer = StopWatch.timeMillis();
        int readCount = 0;

        for (int j = 0; j < mapCount; j++) {
            Map<String, Item> map = maps.get(j);
            for (int i = 0; i < iterations; i += 100) {
                map.get(i + "");
                readCount++;
            }
        }
        
        System.out.println("Took "+readTimer+" to fetch "+readCount+" random items");
        
        //System.gc();
        
        System.out.print("Final memory: ");
        printMemory();
        
        // this forces JVM to NOT gc map yet!
        maps.get(0).get("1");

        Thread.sleep(10000000L);
        
//        System.out.println("Map had "+map.getKeyByteSize()+" key bytes and "+map.getValueByteSize()+" value bytes");
//        
//        map.close();
    }
    
}