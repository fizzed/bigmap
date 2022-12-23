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

import com.fizzed.crux.util.StopWatch;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class PerfThroughput extends AbstractPerf {

    static public void main(String[] args) throws Exception {
        new PerfThroughput().run();
    }

    public void run() throws Exception {
        //
        // config options
        //

//        String type = "TokyoBigMap";
//        String type = "LevelBigMap";
//        String type = "RocksBigLinkedMap";
//        String type = "RocksBigMap";
//        String type = "MVStoreMap";
//        String type = "TokyoCabinetMap";
//        String type = "KyotoCabinetMap";
//        String type = "TkrzwCabinetMap";
        String type = "MapDBMap";
        int mapCount = 10;
        int entryCountPerMap = 300000;

        //
        //
        //
        logMemory("At startup");

        final List<Map<String,Item>> maps = new ArrayList<>(mapCount);
        for (int i = 0; i < mapCount; i++) {
            maps.add(buildMap(type, i));
        }

        logMemory("After " + mapCount + " maps created");

        log.info("======================================================================");
        log.info("Writing maps...");
        final StopWatch entryPutTimer = StopWatch.timeMillis();
        long entryPutCount = 0;

        for (int j = 0; j < mapCount; j++) {
            Map<String,Item> map = maps.get(j);
            for (int i = 0; i < entryCountPerMap; i++) {
                if (i % 25000 == 0) {
                    logMemory("Putting map " + (j+1) + "/" + mapCount + ", entry " + (i+1) + "/" + entryCountPerMap);
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
                entryPutCount++;
            }
        }
        entryPutTimer.stop();

        log.info("======================================================================");
        logMemory("After maps created");
        log.info("Put {} entries across {} maps (in {})", entryPutCount, mapCount, entryPutTimer);
        
        // cleanup garbage to make reading harder
        log.info("======================================================================");
        log.info("Collecting garbage...");
        System.gc();
        logMemory("After GC called");

        // intensive read back now
        final StopWatch entryGetTimer = StopWatch.timeMillis();
        int entryGetCount = 0;

        log.info("======================================================================");
        log.info("Reading pseudo-randomized entries...");
        for (int j = 0; j < mapCount; j++) {
            Map<String, Item> map = maps.get(j);
            for (int i = 0; i < entryCountPerMap; i += 100) {
                map.get(i + "");
                entryGetCount++;
            }
        }
        entryGetTimer.stop();

        logMemory("After maps pseudo-randomized read");
        log.info("Get {} entries across {} maps (in {})", entryGetCount, mapCount, entryGetTimer);


        log.info("======================================================================");
        log.info("Performance test: type={}, maps={}, entriesPerMap={}, totalEntries={}", type, mapCount, entryCountPerMap, entryPutCount);
        log.info("Max openFiles={}, heap={} (MB), rss={} (MB)", maxOpenFiles, maxHeapMb, maxRssMb);
        log.info("Map put throughput: {} entries per second", (long)((double)entryPutCount / entryPutTimer.elapsedSeconds()));
        log.info("Map get throughput: {} entries per second", (long)((double)entryGetCount / entryGetTimer.elapsedSeconds()));

        // calculate total disk size used
        long totalDiskUsed = 0L;
        for (int j = 0; j < mapCount; j++) {
            Map<String, Item> map = maps.get(j);
            // can we get a directory we'll use to calculate disk space?
            Path directory = null;
            if (map instanceof BigMap) {
                directory = ((BigMap)map).getPath();
            } else if (map instanceof OffheapMap) {
                directory = ((OffheapMap)map).getDirectory();
            }
            if (directory != null) {
                //log.info("Calculating disk space used in {}", directory);
                totalDiskUsed += Files.walk(directory)
                    .filter(p -> Files.isRegularFile(p))
                    .mapToLong(p -> { try { return Files.size(p); } catch (IOException e) { throw new UncheckedIOException(e); }})
                    .sum();
            }
        }

        if (totalDiskUsed > 0L) {
            log.info("Total disk used: {} (MB)", (long)((double)totalDiskUsed / (1024*1024)));
        }


        log.info("======================================================================");
        log.info("Will sleep now...");
        Thread.sleep(10000000L);
    }

}