package ru.spbau.mit.java.test;


import org.junit.Test;
import ru.spbau.mit.java.client.TcpClient;
import ru.spbau.mit.java.client.runner.ClientRunner;
import ru.spbau.mit.java.client.runner.RunnerOpts;
import ru.spbau.mit.java.server.BenchOpts;
import ru.spbau.mit.java.server.ThreadedTcpServer;
import ru.spbau.mit.java.server.stat.ServerStats;

import java.io.IOException;

public class TcpThreaded {
    private RunnerOpts runnerOpts = new RunnerOpts(
            10,
            10,
            10,
            10
    );

    private int serverPort = 5555;

    private ThreadedTcpServer server = new ThreadedTcpServer(serverPort, new BenchOpts(
            runnerOpts.getClientNumber(),
            runnerOpts.getRequestNumber()
    ));

    private ClientRunner clientRunner = new ClientRunner(runnerOpts,
            new TcpClient.Creator("localhost", serverPort));

    public TcpThreaded() throws IOException {
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
