package ru.spbau.mit.java.server.tcp;


import lombok.extern.slf4j.Slf4j;
import ru.spbau.mit.java.server.BenchOpts;
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
    private final BenchOpts opts;
    private final ExecutorService serverExecutor = Executors.newSingleThreadExecutor();
    private Future<List<OneRequestStats>> futureRes;
    private final ServerSocket serverSocket;

    public SingleThreadTcpServer(int port, BenchOpts opts) throws IOException {
        this.opts = opts;
        this.serverSocket = new ServerSocket(port);
    }

    @Override
    public ServerStats bench() {
        ServerStats serverStats = null;
        try {
            List<OneRequestStats> stats = futureRes.get();
            serverStats = ServerStats.calc(stats.parallelStream());
        } catch (InterruptedException e) {
            log.error("Interrupt during wait");
        } catch (ExecutionException e) {
            log.error("Bench execution error: " + e.getCause() + " " + e.getMessage());
        }
        serverExecutor.shutdown();
        return serverStats;
    }

    @Override
    public void start() {
        futureRes = serverExecutor.submit(new ServerTask());
    }

    @Override
    public void stop() throws InterruptedException, IOException {
        futureRes.cancel(true);
        serverExecutor.shutdownNow();
    }

    private class ServerTask implements Callable<List<OneRequestStats>> {
        @Override
        public List<OneRequestStats> call() throws IOException {
            int totalRequests = opts.getClientNum() * opts.getRequestNum();
            List<OneRequestStats> results = new ArrayList<>(totalRequests);

            for (int i = 0; i < totalRequests; ++i) {
                try (Socket sock = serverSocket.accept()) {
                    DataInputStream in = new DataInputStream(sock.getInputStream());
                    DataOutputStream out = new DataOutputStream(sock.getOutputStream());
                    OneRequestTask t = new OneRequestTask(in, out);
                    results.add(t.call());
//                    // client must sent disconnect after one request
//                    if (in.readInt() != BenchReqCode.DISCONNECT) {
//                        throw new IOException("Clien't do not want to disconnect");
//                    }
                }
            }

            return results;
        }
    }

    @Override
    public int getPort() {
        return serverSocket.getLocalPort();
    }

}
