package ru.spbau.mit.java.server.tcp.async.state;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * This writing state represents progress of writing many byte buffers somewhere.
 */
public class GatheringWriting implements WritingState {
    private int currentIdx = 0;
    private List<ByteBuffer> buffers;

    public GatheringWriting(List<ByteBuffer> buffers) {
        this.buffers = buffers;
    }

    @Override
    public WritingState proceed() {
        ByteBuffer curBuf = buffers.get(currentIdx);
        if (!curBuf.hasRemaining()) {
            if (currentIdx == buffers.size() - 1) {
                return new WritingIsDone();
            } else {
                currentIdx += 1;
            }
        }
        return this;
    }

    @Override
    public ByteBuffer getBuffer() {
        return buffers.get(currentIdx);
    }
}
