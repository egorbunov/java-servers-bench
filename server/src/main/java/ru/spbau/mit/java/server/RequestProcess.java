package ru.spbau.mit.java.server;


import com.google.protobuf.InvalidProtocolBufferException;
import ru.spbau.mit.java.commons.proto.IntArrayMsg;

import java.util.ArrayList;
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
        return process(intArrayMsg);
    }

    public static byte[] process(IntArrayMsg arr) {
        IntArrayMsg answer = processArray(arr);
        return answer.toByteArray();
    }

    public static IntArrayMsg processArray(IntArrayMsg arr) {
        List<Integer> numbers = new ArrayList<>(arr.getNumbersList());
        insertionSort(numbers);
        IntArrayMsg.Builder builder = IntArrayMsg.newBuilder();
        return builder.addAllNumbers(numbers).build();
    }

    private static void insertionSort(List<Integer> numbers) {
        for (int i = 0; i < numbers.size(); ++i) {
            int maxInd = 0;
            for (int j = 0; j < numbers.size() - i; ++j) {
                if (numbers.get(j) > numbers.get(maxInd)) {
                    maxInd = j;
                }
            }
            int tmp = numbers.get(numbers.size() - i - 1);
            numbers.set(numbers.size() - i - 1, numbers.get(maxInd));
            numbers.set(maxInd, tmp);
        }
    }
}
