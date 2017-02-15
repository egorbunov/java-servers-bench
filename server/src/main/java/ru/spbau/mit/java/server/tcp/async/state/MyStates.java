package ru.spbau.mit.java.server.tcp.async.state;

import com.google.protobuf.InvalidProtocolBufferException;
import ru.spbau.mit.java.commons.proto.IntArrayMsg;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Simple initial state builders for our SIZE-PAYLOAD protocol
 */
public class MyStates {
    public static ReadingState<Integer> intReadingState() {
        ByteBuffer intReadBuf = ByteBuffer.allocate(Integer.BYTES);
        return new ChainedReading<>(intReadBuf, bb -> new MessageRead<>(bb.getInt()));
    }

    public static ReadingState<IntArrayMsg> arrMessageReadingState(int size) {
        ByteBuffer arrBuf = ByteBuffer.allocate(size);
        return new ChainedReading<>(arrBuf, bb -> {
            try {
                byte[] bs = new byte[bb.limit() - bb.position()];
                bb.get(bs);
                return new MessageRead<>(IntArrayMsg.parseFrom(bs));
            } catch (InvalidProtocolBufferException e) {
                return null;
            }
        });
    }

    /**
     * State for writing int array message with int size "header"
     */
    public static WritingState createWritingState(IntArrayMsg intArrayMsg) {
        byte[] bs = intArrayMsg.toByteArray();
        return new GatheringWriting(Arrays.asList(
                ByteBuffer.allocate(Integer.BYTES).putInt(0, bs.length),
                ByteBuffer.wrap(bs))
        );
    }
}
