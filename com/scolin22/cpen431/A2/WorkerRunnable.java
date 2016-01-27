package com.scolin22.cpen431.A2;

import com.scolin22.cpen431.utils.StringUtils;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by colin on 2016-01-17.
 */
public class WorkerRunnable implements Runnable {
    private static Logger log = Logger.getLogger(WorkerRunnable.class.getName());

    ApplicationLayer al;
    DataStore ds;
    Request r;

    public WorkerRunnable(Request r, ApplicationLayer al, DataStore ds) {
        log.setLevel(Level.OFF);

        this.r = r;
        this.al = al;
        this.ds = ds;
    }

    @Override
    public void run() {
        if (r.repType == null) {
            switch (r.reqType) {
                case PUT:
                    ds.put(r);
                    break;
                case GET:
                    ds.get(r);
                    break;
                case REMOVE:
                    ds.remove(r);
                    break;
                case SHUTDOWN:
                    r.repType = Request.ReplyType.OP_SUCCESS;
                    break;
                case DELETE_ALL:
                    ds.delete_all(r);
                    break;
                default:
                    r.repType = Request.ReplyType.UNKNOWN_COM;
                    break;
            }
        }
        log.info("REPLIED request, UID: " + StringUtils.byteArrayToHexString(r.UID) + " command: " + r.reqType.getByteCode());
        al.sendReply(r);
    }
}
