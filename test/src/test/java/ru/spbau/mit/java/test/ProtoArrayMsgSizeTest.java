package ru.spbau.mit.java.test;


import org.junit.Assert;
import org.junit.Test;
import ru.spbau.mit.java.client.runner.ArraySupplier;
import ru.spbau.mit.java.commons.proto.IntArrayMsg;
import ru.spbau.mit.java.commons.proto.Protobuf;

import java.net.DatagramSocket;
import java.net.SocketException;

public class ProtoArrayMsgSizeTest {
    @Test
    public void test() {
        int size = 1000;
        ArraySupplier supplier = new ArraySupplier(size);
        int predictedMax = Protobuf.predictArrayMsgSize(size);
        System.err.println(predictedMax);
        for (int i = 0; i < 100000; ++i) {
            IntArrayMsg msg = supplier.get();
            byte[] bytes = msg.toByteArray();
            if (bytes.length > predictedMax) {
                System.err.println(bytes.length);
                Assert.fail();
            }
        }
    }
}
