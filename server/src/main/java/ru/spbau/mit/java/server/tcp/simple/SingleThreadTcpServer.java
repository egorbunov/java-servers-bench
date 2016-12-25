package ru.spbau.mit.java.server.tcp.simple;


import lombok.extern.slf4j.Slf4j;
import ru.spbau.mit.java.server.BenchServer;
import ru.spbau.mit.java.server.stat.OneRequestStats;
import ru.spbau.mit.java.server.stat.ServerStats;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Slf4j
public class SingleThreadTcpServer implements BenchServer {
    private final ExecutorService serverExecutor = Executors.newSingleThreadExecutor();
    private Future<?> serverTaskFuture;
    private final ServerSocket serverSocket;
    private final List<OneRequestStats> resultStats;

    public SingleThreadTcpServer(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
        resultStats = new ArrayList<>();
    }

    @Override
    public void start() {
        resultStats.clear();
        serverTaskFuture = serverExecutor.submit(new ServerTask());
    }

    @Override
    public ServerStats stop() throws IOException {
        serverExecutor.shutdownNow();
        serverSocket.close();
        serverTaskFuture.cancel(true);
        log.info("Got statistics for " + resultStats.size() + " requests");
        return ServerStats.calc(resultStats.parallelStream());
    }

    private class ServerTask implements Callable<Void> {
        @Override
        public Void call() throws IOException {

            int cnt = 0;
            while (!Thread.currentThread().isInterrupted()) {
                try (Socket sock = serverSocket.accept()) {
                    cnt += 1;
                    DataInputStream in = new DataInputStream(sock.getInputStream());
                    DataOutputStream out = new DataOutputStream(sock.getOutputStream());
                    OneRequestTask t = new OneRequestTask(in, out);
                    OneRequestStats data = t.call();
                    if (data == null) {
                        break;
                    }
                    resultStats.add(data);
                }
            }
            return null;
        }
    }

    @Override
    public int getPort() {
        return serverSocket.getLocalPort();
    }

}
