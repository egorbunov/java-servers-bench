package ru.spbau.mit.java.server.tcp.sock;

import lombok.extern.slf4j.Slf4j;
import ru.spbau.mit.java.commons.net.ConnectionAcceptor;
import ru.spbau.mit.java.server.BenchServer;
import ru.spbau.mit.java.server.stat.OneRequestStats;
import ru.spbau.mit.java.server.stat.ServerStats;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Server, which creates one thread per client and uses tcp sockets for
 * communication with them
 */
@Slf4j
public class ThreadedTcpServer implements BenchServer {
    private final ServerSocket serverSocket;
    private final Thread acceptingThread;
    private final List<ExecutorService> clientThreads;
    private final List<Future<List<OneRequestStats>>> futureStats;

    public ThreadedTcpServer(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.clientThreads = new ArrayList<>();
        this.futureStats = new ArrayList<>();
        // creating client accepting thread
        this.acceptingThread = new Thread(
                new ConnectionAcceptor(
                        serverSocket,
                        clientSock -> {
                            // handle connection accepted event
                            ExecutorService clientExecutor = Executors.newSingleThreadExecutor();
                            clientThreads.add(clientExecutor);
                            futureStats.add(clientExecutor.submit(new ClientServingTask(clientSock)));
                        }
                )
        );
    }

    @Override
    public void start() {
        acceptingThread.start();
    }

    @Override
    public ServerStats stop() throws IOException, InterruptedException {
        for (ExecutorService ct : clientThreads) {
            ct.shutdownNow();
        }
        acceptingThread.interrupt();
        serverSocket.close();
        acceptingThread.join();

        for (Future<?> f : futureStats) {
            if (!f.isDone()) {
                f.cancel(true);
            }
        }

        log.info("Getting stats from all the clients...");
        List<List<OneRequestStats>> stats = new ArrayList<>();
        for (Future<List<OneRequestStats>> f : futureStats) {
            try {
                if (!f.isCancelled()) {
                    stats.add(f.get());
                }
            } catch (InterruptedException e) {
                log.error("Interrupt during client wait: " + e.getMessage());
            } catch (ExecutionException e) {
                log.error("Client failed during execution: " + e.getCause());
            }
        }
        ServerStats res = ServerStats.calc(stats.stream().flatMap(List::stream).parallel());
        log.info("Got statistics from " + stats.size() + " clients");
        log.info("Ok. Returning stats: " + res);
        return res;
    }

    @Override
    public int getPort() {
        return serverSocket.getLocalPort();
    }
}
