package ru.spbau.mit.java.test;


import org.junit.Test;
import ru.spbau.mit.java.client.TcpConnectionPreservingClient;
import ru.spbau.mit.java.client.runner.ClientRunner;
import ru.spbau.mit.java.client.runner.RunnerOpts;
import ru.spbau.mit.java.server.BenchOpts;
import ru.spbau.mit.java.server.tcp.ThreadPoolTcpServer;
import ru.spbau.mit.java.server.stat.ServerStats;

import java.io.IOException;

public class TcpPool {
    private RunnerOpts runnerOpts = new RunnerOpts(
            10,
            10,
            10,
            10
    );

    private int serverPort = 5555;

    private ThreadPoolTcpServer server = new ThreadPoolTcpServer(serverPort, new BenchOpts(
            runnerOpts.getClientNumber(),
            runnerOpts.getRequestNumber()
    ));

    private ClientRunner clientRunner = new ClientRunner(runnerOpts,
            new TcpConnectionPreservingClient.Creator("localhost", serverPort));

    public TcpPool() throws IOException {
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
