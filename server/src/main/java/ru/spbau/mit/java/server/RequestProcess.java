package ru.spbau.mit.java.server;


import com.google.protobuf.InvalidProtocolBufferException;
import ru.spbau.mit.java.commons.proto.IntArrayMsg;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class RequestProcess {
    /**
     * Reads given bytes as protobuf message with array, sorts it and returns encoded
     * in protobuf format
     *
     * @param msg bytes with proto message
     * @return bytes with proto message with sorted array
     * @throws InvalidProtocolBufferException in case of malformed input protobuf message
     */
    public static byte[] process(byte[] msg) throws InvalidProtocolBufferException {
        IntArrayMsg intArrayMsg = IntArrayMsg.parseFrom(msg);
        List<Integer> numbers = intArrayMsg.getNumbersList();
        List<Integer> sorted = numbers.stream().sorted().collect(Collectors.toList());
        IntArrayMsg.Builder builder = IntArrayMsg.newBuilder();
        IntArrayMsg answer = builder.addAllNumbers(sorted).build();
        return answer.toByteArray();
    }
}
