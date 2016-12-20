package ru.spbau.mit.java.client;

import ru.spbau.mit.java.commons.proto.IntArrayMsg;

import java.io.IOException;

public interface BenchClient extends AutoCloseable {
    IntArrayMsg makeBlockingRequest(IntArrayMsg toSort) throws IOException;
    void disconnect() throws IOException;

    @Override
    default void close() throws IOException {
        disconnect();
    }
}
