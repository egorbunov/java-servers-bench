package ru.spbau.mit.java.client;

import ru.spbau.mit.java.commons.proto.IntArray;

/**
 * Client, which sends UDP Datagrams to server
 */
public class UdpClient implements BenchClient {
    @Override
    public IntArray makeBlockingRequest(IntArray toSort) {
        return null;
    }

    @Override
    public void close() throws Exception {

    }
}
