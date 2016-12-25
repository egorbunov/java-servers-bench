package ru.spbau.mit.java.client;

import ru.spbau.mit.java.commons.proto.IntArrayMsg;

import java.io.IOException;

/**
 * Client, which sends UDP Datagrams to server
 */
public class UdpClient implements Client {
    @Override
    public IntArrayMsg makeBlockingRequest(IntArrayMsg toSort) {
        return null;
    }

    @Override
    public void disconnect() throws IOException {

    }
}
