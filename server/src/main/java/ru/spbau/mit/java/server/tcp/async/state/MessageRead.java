package ru.spbau.mit.java.server.tcp.async.state;

import java.nio.ByteBuffer;

/**
 * That is terminal state for reading process -- read message holder
 */
public class MessageRead<T> implements ReadingState<T> {
    private final T message;

    public MessageRead(T val) {
        message = val;
    }

    private ByteBuffer dummyBuffer = ByteBuffer.allocate(0);

    @Override
    public ReadingState<T> proceed() {
        return this;
    }

    @Override
    public ByteBuffer getBuffer() {
        return dummyBuffer;
    }

    @Override
    public T getMessage() {
        return message;
    }
}
