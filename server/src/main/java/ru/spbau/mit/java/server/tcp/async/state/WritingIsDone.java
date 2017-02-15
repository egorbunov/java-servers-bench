package ru.spbau.mit.java.server.tcp.async.state;


import java.nio.ByteBuffer;

public class WritingIsDone implements WritingState {
    private ByteBuffer buf = ByteBuffer.allocate(0);

    @Override
    public WritingState proceed() {
        return this;
    }

    @Override
    public ByteBuffer getBuffer() {
        return buf;
    }
}
