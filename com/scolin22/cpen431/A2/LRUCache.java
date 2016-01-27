package com.scolin22.cpen431.A2;

import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by colin on 2016-01-25.
 */
public class LRUCache extends LinkedHashMap<ByteBuffer, Request> {
    private static Logger log = Logger.getLogger(LRUCache.class.getName());
    private int initialCapacity;

    public LRUCache(int initialCapacity) {
        super(initialCapacity);

        log.setLevel(Level.WARNING);
        this.initialCapacity = initialCapacity;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry eldest) {
        log.warning("Removed eldest entry from cache.");
        return size() > this.initialCapacity;
    }
}
