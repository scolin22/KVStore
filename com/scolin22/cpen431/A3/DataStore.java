package com.scolin22.cpen431.A3;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataStore {
    final static int STORE_CAPACITY = 100000;
    final static int INIT_CAPACITY = 5000;
    final static int CACHE_CAPACITY = 100;
    private static Logger log = Logger.getLogger(DataStore.class.getName());
    ConcurrentHashMap<ByteBuffer, byte[]> ds;
    Map cache;

    public DataStore() {
        log.setLevel(Level.WARNING);

        this.ds = new ConcurrentHashMap<ByteBuffer, byte[]>(INIT_CAPACITY);
        this.cache = Collections.synchronizedMap(new LRUCache(CACHE_CAPACITY));
    }

    public void put(Request r) {
        double heapFreeSize = Runtime.getRuntime().freeMemory();
        double heapMaxSize = Runtime.getRuntime().maxMemory();

        Request cachedRequest = (Request) cache.get(r.getUID());
        if (cachedRequest != null) {
            r.copyRequest(cachedRequest);
            return;
        }

        if ((heapFreeSize / heapMaxSize) < 0.15 || ds.size() >= STORE_CAPACITY) {
            log.warning("Exceeded Heap Limit, Free: " + heapFreeSize + " Max: " + heapMaxSize);
            r.repType = Request.ReplyType.NO_SPACE;
        } else {
            ds.put(r.getKey(), r.value);
            r.repType = Request.ReplyType.OP_SUCCESS;
        }

        cache.put(r.getUID(), r);
    }

    public void get(Request r) {
        Request cachedRequest = (Request) cache.get(r.getUID());
        if (cachedRequest != null) {
            r.copyRequest(cachedRequest);
            return;
        }

        if (!ds.containsKey(r.getKey())) {
            r.repType = Request.ReplyType.INVALID_KEY;
        } else {
            byte[] store = ds.get(r.getKey());
            r.length = (short) store.length;
            r.value = store;
            r.repType = Request.ReplyType.OP_SUCCESS;
        }

        cache.put(r.getUID(), r);
    }

    public void remove(Request r) {
        Request cachedRequest = (Request) cache.get(r.getUID());
        if (cachedRequest != null) {
            r.copyRequest(cachedRequest);
            return;
        }

        if (!ds.containsKey(r.getKey())) {
            r.repType = Request.ReplyType.INVALID_KEY;
        } else {
            ds.remove(r.getKey());
            r.repType = Request.ReplyType.OP_SUCCESS;
        }

        cache.put(r.getUID(), r);
    }

    public void delete_all(Request r) {
        Request cachedRequest = (Request) cache.get(r.getUID());
        if (cachedRequest != null) {
            r.copyRequest(cachedRequest);
            return;
        }

        ds.clear();
        r.repType = Request.ReplyType.OP_SUCCESS;
        cache.put(r.getUID(), r);

        double heapFreeSize = Runtime.getRuntime().freeMemory();
        double heapMaxSize = Runtime.getRuntime().maxMemory();
        log.warning("Cleared data store, Heap, Free: " + heapFreeSize + " Max: " + heapMaxSize);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
