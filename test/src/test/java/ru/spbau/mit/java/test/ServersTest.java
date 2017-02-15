package ru.spbau.mit.java.test;

import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.junit.Assert;
import org.junit.Test;
import ru.spbau.mit.java.client.*;
import ru.spbau.mit.java.client.runner.ArraySupplier;
import ru.spbau.mit.java.commons.proto.IntArrayMsg;
import ru.spbau.mit.java.commons.proto.Protobuf;
import ru.spbau.mit.java.server.BenchServer;
import ru.spbau.mit.java.server.BenchingError;
import ru.spbau.mit.java.server.tcp.async.AsyncServer;
import ru.spbau.mit.java.server.tcp.nonblocking.NioTcpServer;
import ru.spbau.mit.java.server.tcp.simple.SingleThreadTcpServer;
import ru.spbau.mit.java.server.tcp.simple.ThreadPoolTcpServer;
import ru.spbau.mit.java.server.tcp.simple.ThreadedTcpServer;
import ru.spbau.mit.java.server.udp.FixedPoolUdpServer;
import ru.spbau.mit.java.server.udp.ThreadedUdpServer;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ServersTest {
    private static final int serverPort = 5555;
    private static final String host = "localhost";
    private static final int arraySize = 1000;

    private interface ServerCreator {
        BenchServer create() throws IOException;
    }

    private final static List<Tuple2<ServerCreator, ClientCreator>> serverClasses = Arrays.asList(
            new Tuple2<>(
                    () -> new ThreadedTcpServer(serverPort),
                    new TcpConnectionPreservingClient.Creator(host, serverPort)),
            new Tuple2<>(
                    () -> new ThreadPoolTcpServer(serverPort),
                    new TcpConnectionPreservingClient.Creator(host, serverPort)),
            new Tuple2<>(
                    () -> new SingleThreadTcpServer(serverPort),
                    new TcpConnectionPerRequestClient.Creator(host, serverPort)),
            new Tuple2<>(
                    () -> new FixedPoolUdpServer(serverPort, Runtime.getRuntime().availableProcessors() - 1),
                    new UdpClient.Creator(host, serverPort)),
            new Tuple2<>(
                    () -> new ThreadedUdpServer(serverPort),
                    new UdpClient.Creator(host, serverPort)),
            new Tuple2<>(
                    () -> new NioTcpServer(serverPort, Runtime.getRuntime().availableProcessors() - 1),
                    new TcpConnectionPreservingClient.Creator(host, serverPort)),
            Tuple.tuple(
                    () -> new AsyncServer(serverPort),
                    new TcpConnectionPerRequestClient.Creator(host, serverPort)
            )
    );

    @Test
    public void testSorted() throws InterruptedException, ReflectiveOperationException, BenchingError, IOException {
        for (Tuple2<ServerCreator, ClientCreator> serverClass : serverClasses) {
            testOneServer(serverClass.v1(), serverClass.v2());
        }
    }

    private void testOneServer(ServerCreator serverCreator, ClientCreator clientCreator)
            throws ReflectiveOperationException, InterruptedException, IOException, BenchingError {
        try (BenchServer server = serverCreator.create()) {
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
