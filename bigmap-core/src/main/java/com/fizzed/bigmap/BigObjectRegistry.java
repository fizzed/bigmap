package com.fizzed.bigmap;

import com.fizzed.bigmap.impl.BigObjectWeakReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BigObjectRegistry implements BigObjectListener {
    static private final Logger log = LoggerFactory.getLogger(BigObjectRegistry.class);

    static private BigObjectRegistry defaultRegistry;

    static public BigObjectRegistry getDefault() {
        if (defaultRegistry != null) {
            return defaultRegistry;
        }
        synchronized (BigObjectRegistry.class) {
            if (defaultRegistry == null) {
                defaultRegistry = new BigObjectRegistry();
            }
        }
        return defaultRegistry;
    }

    private final ConcurrentHashMap<UUID, BigObjectWeakReference> weakReferenceMap;      // weak references to BigObjects
    private final ConcurrentHashMap<UUID,BigObjectCloser> closers;                      // strong references to closers
    private final ReferenceQueue<BigObject> referenceQueue;
    private final GarbageMonitorThread garbageMonitorThread;
    private final ShutdownHookThread shutdownHookThread;

    private BigObjectRegistry() {
        this.weakReferenceMap = new ConcurrentHashMap<>();
        this.closers = new ConcurrentHashMap<>();
        this.referenceQueue = new ReferenceQueue<>();
        this.garbageMonitorThread = new GarbageMonitorThread();
        this.shutdownHookThread = new ShutdownHookThread();
        Runtime.getRuntime().addShutdownHook(this.shutdownHookThread);

        this.garbageMonitorThread.start();
    }

    public void register(BigObject bigObject) {
        Objects.requireNonNull(bigObject, "object was null");

        this.closers.put(bigObject.getId(), bigObject.getCloser());
        this.weakReferenceMap.put(bigObject.getId(), new BigObjectWeakReference(bigObject, this.referenceQueue));
    }

    public void unregister(BigObject bigObject) {
        if (bigObject != null) {
            this.weakReferenceMap.remove(bigObject.getId());
            this.closers.remove(bigObject.getId());
        }
    }

    public boolean isRegistered(UUID id) {
        return this.closers.containsKey(id);
    }

    @Override
    public void onOpened(BigObject bigObject) {
        this.register(bigObject);
    }

    @Override
    public void onClosed(BigObject bigObject) {
        this.unregister(bigObject);
    }

    public class GarbageMonitorThread extends Thread {

        public GarbageMonitorThread() {
            this.setName("BigObjectGarbageMonitor");
        }

        public void run() {
            log.debug("BigObject garbage monitor running...");
            while (true) {
                try {
                    final Reference<? extends BigObject> _ref = BigObjectRegistry.this.referenceQueue.remove();
                    if (_ref instanceof BigObjectWeakReference) {
                        final BigObjectWeakReference ref = (BigObjectWeakReference)_ref;

                        // try to find closer, and make sure the resources are closed
                        final UUID id = ref.getId();
                        final BigObjectCloser closer = BigObjectRegistry.this.closers.remove(ref.getId());
                        BigObjectRegistry.this.weakReferenceMap.remove(ref.getId());
                        if (closer != null) {
                            log.info("Will try to close big object {} via automatic garbage collection (persistent={}, directory={})", id, closer.isPersistent(), closer.getDirectory());
                            try {
                                closer.close();
                                log.info("Successfully closed big object {}", id);
                            } catch (Throwable t) {
                                log.error("Unable to cleanly close big object " + id, t);
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    log.warn("BigObjectGarbageMonitor interrupted!", e);
                }
            }
        }

    }

    public class ShutdownHookThread extends Thread {

        public ShutdownHookThread() {
            this.setName("BigObjectShutdownHook");
        }

        public void run() {
            log.info("BigObject shutdown hook will try to close {} big objects", BigObjectRegistry.this.closers.size());
            // try to close any big objects still around
            for (Map.Entry<UUID,BigObjectCloser> entry : BigObjectRegistry.this.closers.entrySet()) {
                final UUID id = entry.getKey();
                final BigObjectCloser closer = entry.getValue();

                log.info("Will try to close big object {} (persistent={}, directory={})", id, closer.isPersistent(), closer.getDirectory());
                try {
                    closer.close();
                    log.info("Successfully closed big object {}", id);
                } catch (Throwable t) {
                    log.error("Unable to cleanly close big object " + id, t);
                }
            }
        }

    }

}