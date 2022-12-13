BigMap (and Set) by Fizzed
--------------------------

Lightweight Map and Set implementation(s) with minimal 3rd party dependencies that alleviates memory
pressure by offloading to disk. 

While there are other alternatives out there, they were almost too complicated.  This is a simple
way to drop-in as a replacement where you use JVM Maps or Sets and don't want to fuss with settings
too much.

An initial implementation based on LevelDB (Java only port) is supplied.


LevelBigMap Usage

```java

LevelBigMap<Long,String> map = new LevelBigMapBuilder()
   .setScratchDirectory(Paths.get("target"))
   .setKeyType(Long.class)
   .setValueType(String.class)
   .build();
```


LevelBigSet Usage

```java

LevelBigSet<Long> set = new LevelBigSetBuilder()
   .setScratchDirectory(Paths.get("target"))
   .setKeyType(Long.class)
   .build();
```

Then standard Map (sorted) and Set (sorted) methods all mostly work.  Some methods make no sense
when you are using this instead (e.g. finding a value vs. lookups by key)

## Performance

All tests performed with Azul JDK 11, Linux x64, with -Xmx128m settings for JVM.

### TokyoCabinet (B-Tree+)

JNI-based library.

Performance test: type=TokyoCabinetMap, maps=10, entriesPerMap=3000, totalEntries=30000
Max openFiles=52, heap=75 (MB), rss=222 (MB)
Map put throughput: 281514 entries per second
Map get throughput: 20820 entries per second
Total disk used: 1 (MB)

Performance test: type=TokyoCabinetMap, maps=10, entriesPerMap=300000, totalEntries=3000000
Max openFiles=53, heap=80 (MB), rss=332 (MB)
Map put throughput: 872365 entries per second
Map get throughput: 125808 entries per second
Total disk used: 635 (MB)

Performance test: type=TokyoCabinetMap, maps=10, entriesPerMap=3000000, totalEntries=30000000
Max openFiles=53, heap=81 (MB), rss=349 (MB)
Map put throughput: 849108 entries per second
Map get throughput: 162776 entries per second
Total disk used: 6539 (MB)

Performance test: type=TokyoCabinetMap, maps=10, entriesPerMap=9000000, totalEntries=90000000
Max openFiles=53, heap=81 (MB), rss=345 (MB)
Map put throughput: 836712 entries per second
Map get throughput: 157239 entries per second
Total disk used: 19749 (MB)

Performance test: type=TokyoCabinetMap, maps=30, entriesPerMap=3000000, totalEntries=90000000
Max openFiles=73, heap=81 (MB), rss=639 (MB)
Map put throughput: 807945 entries per second
Map get throughput: 164839 entries per second
Total disk used: 19618 (MB)

### RocksDB

JNI-based library.

Performance test: type=RocksBigMap, maps=10, entriesPerMap=3000, totalEntries=30000
Max openFiles=103, heap=74 (MB), rss=227 (MB)
Map put throughput: 155633 entries per second
Map get throughput: 23344 entries per second
Total disk used: 6 (MB)

Performance test: type=RocksBigMap, maps=10, entriesPerMap=300000, totalEntries=3000000
Max openFiles=114, heap=80 (MB), rss=362 (MB)
Map put throughput: 254209 entries per second
Map get throughput: 108640 entries per second
Total disk used: 192 (MB)

Performance test: type=RocksBigMap, maps=10, entriesPerMap=3000000, totalEntries=30000000
Max openFiles=214, heap=81 (MB), rss=609 (MB)
Map put throughput: 177122 entries per second
Map get throughput: 104554 entries per second
Total disk used: 1632 (MB)

Performance test: type=RocksBigMap, maps=10, entriesPerMap=9000000, totalEntries=90000000
Max openFiles=444, heap=81 (MB), rss=722 (MB)
Map put throughput: 160107 entries per second
Map get throughput: 103470 entries per second
Total disk used: 4611 (MB)


## License

Apache 2 License!
