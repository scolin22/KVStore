package com.scolin22.cpen431.A2;

public class Main {

    public static void main(String[] args) {
        //TODO: accept arguments at least for port, timeout
        if (args.length != 1) {
            System.out.println("Invalid number of arguments: e.g. java -jar simpleKVStore.jar <port>");
            return;
        }
        int port = Integer.parseInt(args[0]);
        KVStoreServer server = new KVStoreServer(port);
        server.start();
    }
}
