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

Apache 2 License!
