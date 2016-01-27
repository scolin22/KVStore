package com.scolin22.cpen431.A2;

import com.scolin22.cpen431.utils.StringUtils;

import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ListenerThread extends Thread {
    final static int THREAD_QUEUE_LENGTH = 16;
    private static Logger log = Logger.getLogger(ListenerThread.class.getName());
    ApplicationLayer al;
    DataStore ds;
    ExecutorService threadPool;

    private volatile boolean running = true;

    public ListenerThread(ApplicationLayer al, DataStore ds) {
        log.setLevel(Level.OFF);

        this.threadPool = new ThreadPoolExecutor(4, 4, Long.MAX_VALUE, TimeUnit.NANOSECONDS, new ArrayBlockingQueue<Runnable>(THREAD_QUEUE_LENGTH));
        this.al = al;
        this.ds = ds;
    }

    public void terminate() {
        running = false;
    }

    @Override
    public void run() {
        while (running) {
            Request r = al.getRequest();
            if (r == null) continue;
            log.info("PROCESD request, UID: " + StringUtils.byteArrayToHexString(r.UID));

            try {
                threadPool.execute(new WorkerRunnable(r, al, ds));
            } catch (RejectedExecutionException e) {
                continue;
            }
        }
    }
}
