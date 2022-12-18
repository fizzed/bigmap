# BigMap by Fizzed

[![Java 8](https://github.com/fizzed/bigmap/actions/workflows/java8.yaml/badge.svg?style=flat-square)](https://github.com/fizzed/bigmap/actions/workflows/java8.yaml)
[![Java 11](https://github.com/fizzed/bigmap/actions/workflows/java11.yaml/badge.svg?style=flat-square)](https://github.com/fizzed/bigmap/actions/workflows/java11.yaml)
[![Java 17](https://github.com/fizzed/bigmap/actions/workflows/java17.yaml/badge.svg?style=flat-square)](https://github.com/fizzed/bigmap/actions/workflows/java17.yaml)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.fizzed/bigmap/badge.svg?style=flat-square)](https://maven-badges.herokuapp.com/maven-central/com.fizzed/bigmap)

Lightweight Map, SortedMap, LinkedMap, Set, and SortedSet implementation(s) with minimal 3rd party dependencies that alleviates memory
pressure by offloading to disk.  Tested on Java 8, 11, and 17.

While there are other alternatives out there, they were almost too complicated.  This is a simple
way to drop-in as a replacement where you use JVM Maps or Sets and don't want to fuss with settings
too much.

There are several implementations available, depending on your needs and runtime environment. We evaluated
RocksDB, LevelDB (pure Java and JNI versions), MVStore, LMDB, KyotoCabinet, TokyoCabinet, and Tkrzw.

Based on our primary need for minimal memory usage (including a desire to not rely on memory-mapped files)
and as small as possible dependencies, we narrowed in on the older, but still impressive TokyoCabinet vs.
some of the more recent entrants like RocksDB.

## Example LevelBigMap Usage

In your maven pom:

```xml
<dependency>
    <groupId>com.fizzed</groupId>
    <artifactId>bigmap-leveldb</artifactId>
    <version>VERSION-HERE</version>
</dependency>
```

```java
import com.fizzed.bigmap.leveldb.LevelBigMapBuilder;
...
Map<Long,String> map = new LevelBigMapBuilder()
   .setScratchDirectory(Paths.get("target"))
   .setKeyType(Long.class)
   .setValueType(String.class)
   .build();
```

```java
import com.fizzed.bigmap.leveldb.LevelBigLinkedMapBuilder;
...
Map<Long,String> map = new LevelBigLinkedMapBuilder()
   .setScratchDirectory(Paths.get("target"))
   .setKeyType(Long.class)
   .setValueType(String.class)
   .build();

```

```java
import com.fizzed.bigmap.leveldb.LevelBigSetBuilder;
...
Set<Long> set = new LevelBigSetBuilder()
   .setScratchDirectory(Paths.get("target"))
   .setValueType(Long.class)
   .build();
```

Then standard Map (sorted) and Set (sorted) methods all mostly work.  Some methods make no sense
when you are using this instead (e.g. finding a value vs. lookups by key)

## Serialization

All keys and values must be serialized to/from byte arrays in order to offload your entries to disk.
Standard primitives like Strings, Longs, Integers, etc. have built-in codecs in the BigMap library
for quick and efficient serialization. For more complex objects that implement the Serializable interface,
there is support for that in BigMap as well. However, if you desire high performance for your complex
objects, you can either provide your own ByteCodec to your maps, or take a look at the `bigmap-kryo` 
dependency, which offers excellent performance and works great on Java 8+ (including Java 17).

## Performance

All tests performed with Azul JDK 11, Linux x64, with -Xmx128m settings for JVM.

### TokyoCabinet (B-Tree+)

JNI-based library. Small dependency, most efficient use of memory, and highest performance.

```
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
```

### RocksDB

JNI-based library.

```
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
```

### LevelDB (Java version)

Pure Java library.

```
Performance test: type=LevelBigMap, maps=10, entriesPerMap=3000, totalEntries=30000
Max openFiles=68, heap=76 (MB), rss=229 (MB)
Map put throughput: 222263 entries per second
Map get throughput: 9968 entries per second
Total disk used: 20 (MB)

Performance test: type=LevelBigMap, maps=10, entriesPerMap=300000, totalEntries=3000000
Max openFiles=71, heap=103 (MB), rss=906 (MB)
Map put throughput: 214845 entries per second
Map get throughput: 197670 entries per second
Total disk used: 637 (MB)

YIKES - ran out of heap almost towards end of test, adjusted to -Xms256m
Performance test: type=LevelBigMap, maps=10, entriesPerMap=3000000, totalEntries=3000000
Max openFiles=71, heap=223 (MB), rss=6766 (MB)
Map put throughput: 189189 entries per second
Map get throughput: 250558 entries per second
Total disk used: 6307 (MB)
```

### MVStore (H2 DB engine)

Pure Java library.

```
Performance test: type=MVStoreMap, maps=10, entriesPerMap=3000, totalEntries=30000
Max openFiles=39, heap=64 (MB), rss=299 (MB)
Map put throughput: 77023 entries per second
Map get throughput: 15670 entries per second
Total disk used: 163 (MB)

Performance test: type=MVStoreMap, maps=10, entriesPerMap=300000, totalEntries=3000000
Max openFiles=40, heap=101 (MB), rss=339 (MB)
Map put throughput: 225884 entries per second
Map get throughput: 11504 entries per second
Total disk used: 2580 (MB)

Performance test: type=MVStoreMap, maps=10, entriesPerMap=3000000, totalEntries=30000000
Max openFiles=40, heap=104 (MB), rss=354 (MB)
Map put throughput: 216259 entries per second
Map get throughput: 12201 entries per second
Total disk used: 24135 (MB)
```

## License

Apache 2 License!
