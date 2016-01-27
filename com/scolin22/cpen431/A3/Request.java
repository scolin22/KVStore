package com.scolin22.cpen431.A3;

import com.scolin22.cpen431.utils.StringUtils;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by colin on 2016-01-16.
 */
public class Request {
    final static int UNIQUE_ID_LENGTH = 16;
    final static int KEY_LENGTH = 32;
    final static int MAX_VAL_LENGTH = 10000;
    final static int COMMAND_LENGTH = 1;
    final static int VAL_LEN_LENGTH = 2;
    private static Logger log = Logger.getLogger(Request.class.getName());
    RequestType reqType;
    ReplyType repType;
    byte[] UID = new byte[UNIQUE_ID_LENGTH];
    short length = 0;
    byte[] value;
    InetAddress remoteIP;
    int remotePort;
    private byte[] key = new byte[KEY_LENGTH];
    boolean isCached = false;

    public Request(ByteBuffer inBuf, InetAddress remoteIP, int remotePort, int packetLength) {
        log.setLevel(Level.WARNING);

        this.remoteIP = remoteIP;
        this.remotePort = remotePort;

        if (packetLength < UNIQUE_ID_LENGTH + COMMAND_LENGTH) {
            log.warning("Missing a command or UID is too short.");
            repType = ReplyType.INCOMPLETE_REQ;
            return;
        }

        inBuf.order(ByteOrder.LITTLE_ENDIAN);
        setUID(inBuf);
        setReqType(inBuf);

        if ((reqType == RequestType.PUT || reqType == RequestType.GET || reqType == RequestType.REMOVE) && packetLength < UNIQUE_ID_LENGTH + COMMAND_LENGTH + KEY_LENGTH) {
            log.warning("UID: " + StringUtils.byteArrayToHexString(UID) + " Length of the Key is too short.");
            repType = ReplyType.INVALID_KEY;
            return;
        }

        setKey(inBuf);

        if (reqType == RequestType.PUT) {
            setPutParams(inBuf, packetLength);
        }
        log.info("CREATED request, UID: " + StringUtils.byteArrayToHexString(UID) + " command: " + reqType.getByteCode());
    }

    public void copyRequest(Request cachedRequest) {
        this.repType = cachedRequest.repType;
        this.isCached = true;

        if (cachedRequest.reqType == RequestType.GET) {
            log.warning("UID: " + StringUtils.byteArrayToHexString(UID) + " Cached response.");
            this.length = cachedRequest.length;
            this.value = cachedRequest.value;
        }
    }

    public void setReply(ByteBuffer outBuf) {
        outBuf.order(ByteOrder.LITTLE_ENDIAN);
        outBuf.put(UID);
        outBuf.put(repType.getByteCode());

        if (reqType == RequestType.GET && repType == ReplyType.OP_SUCCESS) {
            outBuf.putShort(length);
            try {
                outBuf.put(value);
            } catch (NullPointerException e) {
                log.warning("UID: " + StringUtils.byteArrayToHexString(UID) + " NPE.");
            }
        }
    }

    public ByteBuffer getKey() {
        return ByteBuffer.wrap(key);
    }

    public ByteBuffer getUID() {
        return ByteBuffer.wrap(UID);
    }

    private void setKey(ByteBuffer inBuf) {
        inBuf.get(key);
    }

    private void setUID(ByteBuffer inBuf) {
        inBuf.get(UID);
    }

    private void setReqType(ByteBuffer inBuf) {
        reqType = RequestType.getType(inBuf.get());
    }

    private void setPutParams(ByteBuffer inBuf, int packetLength) {
        if (packetLength < UNIQUE_ID_LENGTH + COMMAND_LENGTH + KEY_LENGTH + VAL_LEN_LENGTH) {
            log.warning("UID: " + StringUtils.byteArrayToHexString(UID) + " Missing a Value Length.");
            repType = ReplyType.MISSING_LEN;
            return;
        }

        this.length = inBuf.getShort();

        if (this.length < 0 || this.length > MAX_VAL_LENGTH) {
            log.warning("UID: " + StringUtils.byteArrayToHexString(UID) + " Value Length is too short.");
            repType = ReplyType.BAD_VAL_LEN;
            return;
        }

        if (packetLength < UNIQUE_ID_LENGTH + COMMAND_LENGTH + KEY_LENGTH + VAL_LEN_LENGTH + this.length) {
            log.warning("UID: " + StringUtils.byteArrayToHexString(UID) + " Actual length of Value is longer than the provided Value Length.");
            repType = ReplyType.INCOMPLETE_VAL;
            return;
        }

        value = new byte[this.length];
        inBuf.get(value);
    }

    enum RequestType {
        PUT((byte) 0x01),
        GET((byte) 0x02),
        REMOVE((byte) 0x03),
        SHUTDOWN((byte) 0x04),
        DELETE_ALL((byte) 0x05),
        INVALID_REQ_CODE((byte) 0x06); // Undefined

        private byte index;

        RequestType(byte index) {
            this.index = index;
        }

        public static RequestType getType(byte index) {
            for (RequestType rc : RequestType.values()) {
                if (rc.index == index) {
                    return rc;
                }
            }
            return INVALID_REQ_CODE;
        }

        public byte getByteCode() {
            return this.index;
        }
    }

    //TODO: clarify what these codes mean
    enum ReplyType {
        OP_SUCCESS((byte) 0x00),
        INVALID_KEY((byte) 0x01),
        NO_SPACE((byte) 0x02),
        OVERLOADED((byte) 0x03),
        STORE_FAIL((byte) 0x04),
        UNKNOWN_COM((byte) 0x05),
        BAD_VAL_LEN((byte) 0x06),
        INVALID_REP_CODE((byte) 0x07), // Undefined
        MISSING_LEN((byte) 0x20),
        INCOMPLETE_REQ((byte) 0x21),
        INCOMPLETE_VAL((byte) 0x22);

        private byte index;

        ReplyType(byte index) {
            this.index = index;
        }

        public static ReplyType getType(byte index) {
            for (ReplyType rc : ReplyType.values()) {
                if (rc.index == index) {
                    return rc;
                }
            }
            return INVALID_REP_CODE;
        }

        public byte getByteCode() {
            return this.index;
        }
    }
}