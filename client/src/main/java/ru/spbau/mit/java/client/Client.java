package ru.spbau.mit.java.client;

import ru.spbau.mit.java.commons.proto.IntArrayMsg;

import java.io.IOException;

/**
 * Client for server, which is being benchmarked
 */
public interface Client extends AutoCloseable {
    IntArrayMsg makeBlockingRequest(IntArrayMsg toSort) throws IOException;
    void disconnect() throws IOException;

    @Override
    default void close() throws IOException {
        disconnect();
    }
}
