package ru.spbau.mit.java.commons.proto;


import java.util.stream.IntStream;

public class Protobuf {
    /**
     * Returns size of protobug message with int32 array as if it is filled with
     * max possible integer value (value, which spans max number of bytes).
     */
    public static int predictArrayMsgSize(int arrayLen) {
        IntArrayMsg.Builder builder = IntArrayMsg.newBuilder();
        IntStream.generate(() -> Integer.MIN_VALUE).limit(arrayLen).forEach(builder::addNumbers);
        IntArrayMsg msg = builder.build();
        return msg.toByteArray().length;
    }
}
