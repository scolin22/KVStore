package com.scolin22.cpen431.A2;

import java.util.logging.Level;
import java.util.logging.Logger;

public class KVStoreServer {
    private static Logger log = Logger.getLogger(KVStoreServer.class.getName());

    ApplicationLayer al;
    ListenerThread lt;

    DataStore ds;

    public KVStoreServer(int port) {
        log.setLevel(Level.OFF);

        ds = new DataStore();

        al = new ApplicationLayer(this, port);
        lt = new ListenerThread(al, ds);
    }

    public void start() {
        lt.start();
    }

    public void shutdown() {
        //TODO: gracefully wait for threads to clean up and join
        lt.terminate();
        al.shutdown();
    }
}
