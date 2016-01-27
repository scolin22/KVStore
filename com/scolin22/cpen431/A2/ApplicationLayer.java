package com.scolin22.cpen431.A2;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.logging.Logger;

public class ApplicationLayer {
    final static int MAX_PAYLOAD_SIZE = 10000 + 35 + 16;

    private static Logger log = Logger.getLogger(ApplicationLayer.class.getName());
    DatagramSocket socket;
    KVStoreServer server;

    public ApplicationLayer(KVStoreServer server, int port) {
        try {
            this.server = server;
            socket = new DatagramSocket(port);
            log.info("Socket running on: " + socket.getLocalAddress() + ", port: " + socket.getLocalPort());
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void sendReply(Request req) {
        ByteBuffer outBuf = ByteBuffer.allocate(MAX_PAYLOAD_SIZE);
        req.setReply(outBuf);
        DatagramPacket outPacket = new DatagramPacket(outBuf.array(), MAX_PAYLOAD_SIZE, req.remoteIP, req.remotePort);
        try {
            socket.send(outPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //TODO: put for debugging, change back to shutdown
        if (req.reqType == Request.RequestType.SHUTDOWN) {
            server.shutdown();
        }
    }

    public Request getRequest() {
        ByteBuffer inBuf = ByteBuffer.allocate(MAX_PAYLOAD_SIZE);
        DatagramPacket inPacket = new DatagramPacket(inBuf.array(), MAX_PAYLOAD_SIZE);
        try {
            socket.receive(inPacket);
            return new Request(inBuf, inPacket.getAddress(), inPacket.getPort(), inPacket.getLength());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void shutdown() {
        socket.close();
    }
}
