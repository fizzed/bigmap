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

import com.fizzed.bigmap.impl.BigMapHelper;
import com.fizzed.bigmap.tokyocabinet.TokyoBigMap;
import com.fizzed.bigmap.tokyocabinet.TokyoBigMapBuilder;
import tokyocabinet.BDB;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.nio.file.Path;

public class PerfBigObjectRegistry extends AbstractPerf {

    static public void main(String[] args) throws Exception {
        new PerfBigObjectRegistry().run();
    }

    public void run() throws Exception {
        TokyoBigMap<String,String> map1 = new TokyoBigMapBuilder<>()
            .setKeyType(String.class)
            .setValueType(String.class)
            .registerForGarbageMonitoring()
            .build();

        //BigObjectRegistry.getDefault().register(map1);




        /*

        TokyoWeakReference wref1 = new TokyoWeakReference(map1, map1.getDirectory(), map1.getDb(), referenceQueue);

        TokyoBigMap<String,String> map2 = new TokyoBigMapBuilder<>()
            .setKeyType(String.class)
            .setValueType(String.class)
            .build();

        TokyoWeakReference wref2 = new TokyoWeakReference(map2, map2.getDirectory(), map2.getDb(), referenceQueue);

        TokyoBigMap<String,String> map3 = new TokyoBigMapBuilder<>()
            .setKeyType(String.class)
            .setValueType(String.class)
            .build();

        TokyoWeakReference wref3 = new TokyoWeakReference(map3, map3.getDirectory(), map3.getDb(), referenceQueue);


        map2 = null;
        map3 = null;*/

        map1 = null;

        log.info("Waiting now...");
        //System.gc();
        Thread.sleep(10000000000L);



//        {
//            for (int i = 0; i < 10000000; i++) {
//                System.gc();
//
//                log.info("Creating new mappy #");
//                //Mappy mappy = new Mappy();
//                Dbm dbm = new Dbm();
//                WeakReference ref = new WeakReference<>(dbm , referenceQueue);
//
//                Thread.sleep(100L);

//                dbm = null;

//                ref.clear();
//                //mappy = null;
//            }
//        }




        /*//
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
        int mapCount = 20000;
        int entryCountPerMap = 3000;

        logMemory("At startup");

        for (int j = 0; j < mapCount; j++) {
            final Map<String,Item> map = buildMap(type, j);

            if (j % 500 == 0) {
                logMemory("Map #" + j);
            } else if (j % 25 == 0) {
                log.info("Processed map #{}", j);
            }

            // add some entries
            for (int i = 0; i < entryCountPerMap; i++) {
                Item item = new Item();
                item.a = (long) i;
                item.b = "This is sooooo cool dude! " + i;
                item.c = "Look mom no hands " + i;
                item.d = "Woooo baby! " + i;
                item.e = "Woza! " + i;
                item.g = "Blah blah aljlfjalfrjsd;lfjsdlfjsdlafjsdlfjsdlfjsdlfjsldafjlsdfjsdlfjsdlfjsdalfjsdlfjsdlfjdsjf" + i;
                item.h = i;
                map.put(i+"", item);
            }

            // get some entries
            for (int i = 0; i < entryCountPerMap; i += 200) {
                map.get(i+"");
            }

            // try a bunch of iterator's
            for (int i = 0; i < 1000; i++) {
                Collection<Item> values = map.values();
            }

            // close map (does it clean up its resources)
            ((BigMap)map).close();
        }

        logMemory("After " + mapCount + " maps created & closed");*/
    }

}