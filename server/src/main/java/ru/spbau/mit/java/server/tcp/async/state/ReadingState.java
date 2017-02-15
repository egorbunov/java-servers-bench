package ru.spbau.mit.java.server.tcp.async.state;


import java.nio.ByteBuffer;

/**
 * This is logically the same as WritingState, but with
 * reading operation. So instance of ReadingState represents
 * some state of reading operation in progress.
 *
 * Data is read to ByteBuffer
 */
public interface ReadingState<T> {
    ReadingState<T> proceed();
    ByteBuffer getBuffer();
    T getMessage();
}
