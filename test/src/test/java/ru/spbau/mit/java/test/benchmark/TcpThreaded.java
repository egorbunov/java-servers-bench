package ru.spbau.mit.java.test.benchmark;


import org.junit.Test;
import ru.spbau.mit.java.client.TcpConnectionPreservingClient;
import ru.spbau.mit.java.client.runner.ClientRunner;
import ru.spbau.mit.java.client.runner.RunnerOpts;
import ru.spbau.mit.java.server.tcp.ThreadedTcpServer;
import ru.spbau.mit.java.server.stat.ServerStats;

import java.io.IOException;

public class TcpThreaded {
    private final RunnerOpts runnerOpts = new RunnerOpts(
            10,
            10,
            10,
            5
    );

    private final int serverPort = 5555;

    private final ThreadedTcpServer server = new ThreadedTcpServer(serverPort);

    private final ClientRunner clientRunner = new ClientRunner(runnerOpts,
            new TcpConnectionPreservingClient.Creator("localhost", serverPort));

    public TcpThreaded() throws IOException {
    }

    @Test
    public void test() throws IOException, InterruptedException {
        server.start();
        System.out.println("Av. client time: " + clientRunner.run());
        ServerStats bench = server.stop();
        System.out.println(bench);
    }
}
