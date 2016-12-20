package ru.spbau.mit.java.server;


import com.google.protobuf.InvalidProtocolBufferException;
import ru.spbau.mit.java.commons.proto.IntArrayMsg;

import java.util.Collections;
import java.util.List;

public class RequestProcess {
    public static byte[] process(byte[] msg) throws InvalidProtocolBufferException {
        IntArrayMsg intArrayMsg = IntArrayMsg.parseFrom(msg);
        List<Integer> numbers = intArrayMsg.getNumbersList();
        Collections.sort(numbers);
        IntArrayMsg.Builder builder = IntArrayMsg.newBuilder();
        IntArrayMsg answer = builder.addAllNumbers(numbers).build();
        return answer.toByteArray();
    }
}
