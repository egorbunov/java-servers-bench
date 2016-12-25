package ru.spbau.mit.java.test;

import org.jooq.lambda.tuple.Tuple2;
import org.junit.Assert;
import org.junit.Test;
import ru.spbau.mit.java.client.Client;
import ru.spbau.mit.java.client.ClientCreator;
import ru.spbau.mit.java.client.TcpConnectionPerRequestClient;
import ru.spbau.mit.java.client.TcpConnectionPreservingClient;
import ru.spbau.mit.java.client.runner.ArraySupplier;
import ru.spbau.mit.java.commons.proto.IntArrayMsg;
import ru.spbau.mit.java.server.BenchServer;
import ru.spbau.mit.java.server.BenchingError;
import ru.spbau.mit.java.server.tcp.sock.SingleThreadTcpServer;
import ru.spbau.mit.java.server.tcp.sock.ThreadPoolTcpServer;
import ru.spbau.mit.java.server.tcp.sock.ThreadedTcpServer;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ServersTest {
    private static final int serverPort = 5555;
    private static final String host = "localhost";
    private static final int arraySize = 10000;
    private final static List<Tuple2<Class<? extends BenchServer>, ClientCreator>> serverClasses = Arrays.asList(
            new Tuple2<>(ThreadedTcpServer.class, new TcpConnectionPreservingClient.Creator(host, serverPort)),
            new Tuple2<>(ThreadPoolTcpServer.class, new TcpConnectionPreservingClient.Creator(host, serverPort)),
            new Tuple2<>(SingleThreadTcpServer.class, new TcpConnectionPerRequestClient.Creator(host, serverPort))
    );

    @Test
    public void testSorted() throws InterruptedException, ReflectiveOperationException, BenchingError, IOException {
        for (Tuple2<Class<? extends BenchServer>, ClientCreator> serverClass : serverClasses) {
            testOneServer(serverClass.v1(), serverClass.v2());
        }
    }

    private void testOneServer(Class<? extends BenchServer> cls, ClientCreator clientCreator)
            throws ReflectiveOperationException, InterruptedException, IOException, BenchingError {
        Constructor<? extends BenchServer> ctr = cls.getConstructor(Integer.TYPE);
        try (BenchServer server = ctr.newInstance(serverPort)) {
            server.start();
            try (Client client = clientCreator.create()) {
                ArraySupplier supplier = new ArraySupplier(arraySize);
                IntArrayMsg arr = supplier.get();
                IntArrayMsg sorted = client.makeBlockingRequest(arr);
                List<Integer> actual = sorted.getNumbersList();
                List<Integer> exp = arr.getNumbersList().stream().sorted().collect(Collectors.toList());
                Assert.assertEquals(exp, actual);
            }
        }
    }
}
