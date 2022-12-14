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

import com.fizzed.bigmap.kryo.KryoByteCodec;
import com.fizzed.bigmap.leveldb.LevelBigMapBuilder;
import com.fizzed.bigmap.rocksdb.RocksBigLinkedMapBuilder;
import com.fizzed.bigmap.rocksdb.RocksBigMapBuilder;
import com.fizzed.bigmap.tokyocabinet.TokyoBigMapBuilder;
import com.fizzed.crux.util.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.SystemInfo;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;

import java.io.IOException;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class PerfDemo {
    static private final Logger log = LoggerFactory.getLogger(PerfDemo.class);

    static public void main(String[] args) throws Exception {
        //
        // config options
        //

        String type = "TokyoBigMap";
//        String type = "LevelBigMap";
//        String type = "RocksBigLinkedMap";
//        String type = "RocksBigMap";
//        String type = "MVStoreMap";
//        String type = "TokyoCabinetMap";
//        String type = "KyotoCabinetMap";
//        String type = "TkrzwCabinetMap";
        int mapCount = 10;
        int entryCountPerMap = 3000000;

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
                directory = ((BigMap)map).getDirectory();
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

    static Map<String,Object> ENGINE_STATICS = new HashMap<>();

    static public Map<String,Item> buildMap(String type, int identifier) throws Exception {
        final ByteCodec<String> stringByteCodec = ByteCodecs.utf8StringCodec();
//        final ByteCodec<Item> itemByteCodec = ByteCodecs.autoCodec(Item.class);
        final ByteCodec<Item> itemByteCodec = new KryoByteCodec<>(Item.class);
//        final ByteCodec<Item> itemByteCodec = new FSTByteCodec<>();

        switch (type) {
            case "TokyoBigMap":
                return new TokyoBigMapBuilder()
                    .setScratchDirectory(Paths.get("target"))
                    .setKeyType(String.class, stringByteCodec)
                    .setValueType(Item.class, itemByteCodec)
                    .build();
            case "RocksBigMap":
                return new RocksBigMapBuilder()
                    .setScratchDirectory(Paths.get("target"))
                    .setKeyType(String.class, stringByteCodec)
                    .setValueType(Item.class, itemByteCodec)
                    .build();
            case "RocksBigLinkedMap":
                return new RocksBigLinkedMapBuilder()
                    .setScratchDirectory(Paths.get("target"))
                    .setKeyType(String.class, stringByteCodec)
                    .setValueType(Item.class, itemByteCodec)
                    .build();
            case "LevelBigMap":
                return new LevelBigMapBuilder()
                    .setScratchDirectory(Paths.get("target"))
                    .setKeyType(String.class, stringByteCodec)
                    .setValueType(Item.class, itemByteCodec)
                    .build();
            case "MVStoreMap": {
                org.h2.mvstore.MVStore mvstore = (org.h2.mvstore.MVStore) ENGINE_STATICS.computeIfAbsent("mvstore", k -> {
                    try {
                        Path dir = Paths.get("target", "mvstore-" + UUID.randomUUID());
                        Files.createDirectories(dir);
                        String filename = dir.resolve("mvstore").toAbsolutePath().toString();

                        return new org.h2.mvstore.MVStore.Builder()
                            .fileName(filename)
                            .cacheSize(5)
                            .compress()
                            .open();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
                Map<String,Item> map =  mvstore.openMap("data-" + identifier);
                return new OffheapMap<>(Paths.get(mvstore.getFileStore().getFileName()).getParent(), map);
            }
            case "TkrzwCabinetMap": {
                Path dir = Paths.get("target", "tkrzw-" + UUID.randomUUID());
                Files.createDirectories(dir);
                // BTREEMAP
                tkrzw.DBM dbm = new tkrzw.DBM();
                // open the database
                tkrzw.Status status = dbm.open(dir.resolve("casket.tkt").toString(), true, "max_cached_pages=1");
                if (!status.isOK()) {
                    throw new RuntimeException(status.getMessage());
                }
                return new OffheapMap<String,Item>(dir, null) {
                    @Override
                    public Item get(Object key) {
                        byte[] b = dbm.get(stringByteCodec.serialize((String)key));
                        return itemByteCodec.deserialize(b);
                    }
                    @Override
                    public Item put(String key, Item value) {
                        dbm.set(stringByteCodec.serialize(key), itemByteCodec.serialize(value));
                        return null;
                    }
                };
            }
            /*case "KyotoCabinetMap": {
                Path dir = Paths.get("target", "kyotocabinet-" + UUID.randomUUID());
                Files.createDirectories(dir);
                // BTREEMAP
                kyotocabinet.Loader.load();
                kyotocabinet.DB db = new kyotocabinet.DB();
                db.
                // open the database
                if (!db.open(dir.resolve("casket.kct").toString(), kyotocabinet.DB.OWRITER | kyotocabinet.DB.OCREATE)) {
                    throw new RuntimeException("kyotocabinet open error: " + db.error());
                }
                return new OffheapMap<String,Item>(dir, null) {
                    @Override
                    public Item get(Object key) {
                        byte[] b = db.get(stringByteCodec.serialize((String)key));
                        return itemByteCodec.deserialize(b);
                    }
                    @Override
                    public Item put(String key, Item value) {
                        db.set(stringByteCodec.serialize(key), itemByteCodec.serialize(value));
                        return null;
                    }
                };
            }*/
            // tokyo cobinet
            // wget http://fallabs.com/tokyocabinet/tokyocabinet-1.4.48.tar.gz
            // ./configure --prefix=/usr
            // make -j4
            // sudo make install
            // wget http://fallabs.com/tokyocabinet/javapkg/tokyocabinet-java-1.24.tar.gz
            /*case "TokyoCabinetMap": {
                Path dir = Paths.get("target", "tokyocabinet-" + UUID.randomUUID());
                Files.createDirectories(dir);
                // BTREEMAP
                tokyocabinet.Loader.load();
                tokyocabinet.BDB db = new tokyocabinet.BDB();
                // open the database
                if (!db.open(dir.resolve("casket.tcb").toString(), HDB.OWRITER | HDB.OCREAT)){
                    int ecode = db.ecode();
                    throw new RuntimeException("TokyoCabinet open error: " + db.errmsg(ecode));
                }
                return new OffheapMap<String,Item>(dir, null) {
                    @Override
                    public Item get(Object key) {
                        byte[] b = db.get(stringByteCodec.serialize((String)key));
                        return itemByteCodec.deserialize(b);
                    }
                    @Override
                    public Item put(String key, Item value) {
                        db.put(stringByteCodec.serialize(key), itemByteCodec.serialize(value));
                        return null;
                    }
                };
            }*/

        }

//        final TreeMap<String,Item> map = new TreeMap<>();
//        final Map<String,Item> map = new HashMap<>();
//        final Map<String,Item> map = new LinkedHashMap<>();

        // open the store (in-memory if fileName is null)
        // create/get the map named "data"
//        final Map<String,Item> map = MVSTORE.openMap("data-"+UUID.randomUUID());

        //final Map<String,Item> map = new PerfRocksDBMap<>(ByteCodecs.utf8StringCodec(), ByteCodecs.autoCodec(Item.class));

//        final Map<String,Item> map = new TkrzwMap<>(ByteCodecs.utf8StringCodec(), ByteCodecs.autoCodec(Item.class));

        throw new UnsupportedOperationException();
    }

//    static private MVStore MVSTORE = n

    static OperatingSystem SYSTEM = new SystemInfo().getOperatingSystem();
    static int processId = 0;
    static long maxOpenFiles = 0;
    static long maxRssMb = 0;
    static long maxHeapMb = 0;

    static public void logMemory(String identifier) {
        OSProcess process = SYSTEM.getCurrentProcess();
        long openFiles = process.getOpenFiles();
        int threadCount = process.getThreadCount();
        long rssMb = (long)((double)process.getResidentSetSize() / (1024d*1024d));
        long heapMb = (long)((double)(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024*1024));

        if (processId == 0) {
            processId = process.getProcessID();
        }

        if (openFiles > maxOpenFiles) {
            maxOpenFiles = openFiles;
        }

        if (rssMb > maxRssMb) {
            maxRssMb = rssMb;
        }

        if (heapMb > maxHeapMb) {
            maxHeapMb = heapMb;
        }

        log.info("{}: pid={}, openFiles={}, threadCount={}, heap={} (MB), rss={} (MB)", identifier, processId, openFiles, threadCount, heapMb, rssMb);
    }

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

}