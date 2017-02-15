package ru.spbau.mit.java.server.tcp.async.state;

import java.nio.ByteBuffer;
import java.util.function.Function;

/**
 * State represents reading in progress with some continuation
 */
public class ChainedReading<T> implements ReadingState<T> {
    private final ByteBuffer bufToRead;
    private final Function<ByteBuffer, ReadingState<T>> transition;

    /**
     * @param bufToRead  buffer, in which data is going to be read
     * @param transition continuation for reading, that is simple function,
     *                   which takes result of reading operation from previous chained reading
     *                   step and returns, basing on it, new reading state; ByteBuffer is flipped (!)
     *                   just before invoking transition continuation so it is prepared for reading
     *                   data from it during transition evaluation
     */
    public ChainedReading(ByteBuffer bufToRead, Function<ByteBuffer, ReadingState<T>> transition) {

        this.bufToRead = bufToRead;
        this.transition = transition;
    }


    @Override
    public ReadingState<T> proceed() {
        if (bufToRead.hasRemaining()) {
            return this;
        }
        bufToRead.flip();
        return transition.apply(bufToRead);
    }

    @Override
    public ByteBuffer getBuffer() {
        return bufToRead;
    }

    @Override
    public T getMessage() {
        throw new IllegalStateException("getMessage() at non-terminal state");
    }
}
