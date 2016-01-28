package com.scolin22.cpen431.A3;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataStore {
    final static int STORE_CAPACITY = 100000;
    final static int INIT_CAPACITY = 5000;
    final static long CACHE_CAPACITY = 500;
    private static Logger log = Logger.getLogger(DataStore.class.getName());
    ConcurrentHashMap<ByteBuffer, byte[]> ds;
    Cache<ByteBuffer, Request> cache;

    public DataStore() {
        log.setLevel(Level.WARNING);

        this.ds = new ConcurrentHashMap<ByteBuffer, byte[]>(INIT_CAPACITY);
        this.cache = CacheBuilder.newBuilder()
            .maximumSize(CACHE_CAPACITY)
            .expireAfterWrite(5, TimeUnit.SECONDS)
            .build();
    }

    public void put(Request r) {
        try {
            r.copyRequest(fetchCache(r));
        } catch (Exception e) {
            double heapFreeSize = Runtime.getRuntime().freeMemory();
            double heapMaxSize = Runtime.getRuntime().maxMemory();

            if ((heapFreeSize / heapMaxSize) < 0.15 || ds.size() >= STORE_CAPACITY) {
                log.warning("Exceeded Heap Limit, Free: " + heapFreeSize + " Max: " + heapMaxSize);
                r.repType = Request.ReplyType.NO_SPACE;
            } else {
                ds.put(r.getKey(), r.value);
                r.repType = Request.ReplyType.OP_SUCCESS;
            }
            cache.put(r.getUID(), r);
        }
    }

    public void get(Request r) {
        try {
            r.copyRequest(fetchCache(r));
        } catch (Exception e) {
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
    }

    public void remove(Request r) {
        try {
            r.copyRequest(fetchCache(r));
        } catch (Exception e) {
            if (!ds.containsKey(r.getKey())) {
                r.repType = Request.ReplyType.INVALID_KEY;
            } else {
                ds.remove(r.getKey());
                r.repType = Request.ReplyType.OP_SUCCESS;
            }
            cache.put(r.getUID(), r);
        }
    }

    public void delete_all(Request r) {
        try {
            r.copyRequest(fetchCache(r));
        } catch (Exception e) {
            ds.clear();
            System.gc();

            double heapFreeSize = Runtime.getRuntime().freeMemory();
            double heapMaxSize = Runtime.getRuntime().maxMemory();
            if ((heapFreeSize / heapMaxSize) < 0.15 || ds.size() >= STORE_CAPACITY) {
                log.warning("Garbage Collection didn't run, Heap, Free: " + heapFreeSize + " Max: " + heapMaxSize);
                r.repType = Request.ReplyType.DELETE_FAIL;
            } else {
                log.warning("Cleared data store, Heap, Free: " + heapFreeSize + " Max: " + heapMaxSize);
                r.repType = Request.ReplyType.OP_SUCCESS;
            }
            cache.put(r.getUID(), r);
        }
    }

    private Request fetchCache(Request r) throws Exception {
        return cache.get(r.getUID(), new Callable<Request>() {
            @Override
            public Request call() throws Exception {
                return null;
            }
        });
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
