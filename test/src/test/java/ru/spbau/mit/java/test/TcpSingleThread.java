package ru.spbau.mit.java.test;


import org.junit.Test;
import ru.spbau.mit.java.client.TcpConnectionPerRequestClient;
import ru.spbau.mit.java.client.runner.ClientRunner;
import ru.spbau.mit.java.client.runner.RunnerOpts;
import ru.spbau.mit.java.server.BenchOpts;
import ru.spbau.mit.java.server.tcp.SingleThreadTcpServer;
import ru.spbau.mit.java.server.stat.ServerStats;

import java.io.IOException;

public class TcpSingleThread {
    private final RunnerOpts runnerOpts = new RunnerOpts(
            10,
            10,
            10,
            10
    );

    private final int serverPort = 5555;

    private final SingleThreadTcpServer server = new SingleThreadTcpServer(serverPort, new BenchOpts(
            runnerOpts.getClientNumber(),
            runnerOpts.getRequestNumber()
    ));

    private final ClientRunner clientRunner = new ClientRunner(runnerOpts,
            new TcpConnectionPerRequestClient.Creator("localhost", serverPort));

    public TcpSingleThread() throws IOException {
    }

    @Test
    public void test() throws IOException, InterruptedException {
        server.start();
        System.out.println("Av. client time: " + clientRunner.run());
        ServerStats bench = server.bench();
        System.out.println(bench);
        server.stop();
    }
}
