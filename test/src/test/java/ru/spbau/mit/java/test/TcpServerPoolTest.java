package ru.spbau.mit.java.test;

import org.junit.Assert;
import org.junit.Test;
import ru.spbau.mit.java.benchserver.OneBenchClientTask;
import ru.spbau.mit.java.client.TcpConnectionPreservingClient;
import ru.spbau.mit.java.client.runner.ArraySupplier;
import ru.spbau.mit.java.commons.proto.IntArrayMsg;
import ru.spbau.mit.java.server.tcp.ThreadedTcpServer;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by: Egor Gorbunov
 * Date: 12/25/16
 * Email: egor-mailbox@ya.com
 */
public class TcpServerPoolTest {
    private final int serverPort = 5555;
    private final ThreadedTcpServer server = new ThreadedTcpServer(serverPort);

    public TcpServerPoolTest() throws IOException {
    }

    @Test
    public void test() throws IOException {
        server.start();

        TcpConnectionPreservingClient client = new TcpConnectionPreservingClient("localhost", serverPort);

        ArraySupplier supplier = new ArraySupplier(25000);
        IntArrayMsg arr = supplier.get();

        IntArrayMsg sorted = client.makeBlockingRequest(arr);

        List<Integer> actual = sorted.getNumbersList();
        List<Integer> exp = arr.getNumbersList().stream().sorted().collect(Collectors.toList());

        Assert.assertEquals(exp, actual);
    }
}
