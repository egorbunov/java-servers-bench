package ru.spbau.mit.java.server.tcp;

import lombok.extern.slf4j.Slf4j;
import ru.spbau.mit.java.commons.net.ConnectionAcceptor;
import ru.spbau.mit.java.server.BenchOpts;
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

@Slf4j
public class ThreadPoolTcpServer implements BenchServer {
    private final ServerSocket serverSocket;
    private final Thread acceptingThread;
    private final ExecutorService threadPool;
    private final List<Future<List<OneRequestStats>>> futureStats;

    public ThreadPoolTcpServer(int port, BenchOpts opts) throws IOException {
        this.serverSocket = new ServerSocket(port);
        threadPool = Executors.newCachedThreadPool();
        this.futureStats = new ArrayList<>(opts.getClientNum());
        // creating client accepting thread
        this.acceptingThread = new Thread(
                new ConnectionAcceptor(
                        serverSocket,
                        clientSock -> {
                            // handle connection accepted event
                            futureStats.add(threadPool.submit(new ClientServingTask(clientSock)));
                        },
                        opts.getClientNum() // max number of connections to accept
                )
        );
    }


    @Override
    public ServerStats bench() {
        try {
            log.info("Waiting for accepting thread to stop accepting clients...");
            acceptingThread.join();
        } catch (InterruptedException e) {
            log.error("Accepting thread join interrupted");
            return null;
        }
        log.info("Waiting for clients...");
        List<List<OneRequestStats>> stats = new ArrayList<>();
        for (Future<List<OneRequestStats>> f : futureStats) {
            try {
                stats.add(f.get());
            } catch (InterruptedException e) {
                log.error("Interrupt during client wait: " + e.getMessage());
            } catch (ExecutionException e) {
                log.error("Client failed during execution: " + e.getCause());
            }
        }
        log.debug("Shutting down threads...");
        threadPool.shutdown();
        ServerStats res = ServerStats.calc(stats.stream().flatMap(List::stream).parallel());
        log.info("Ok. Returning stats: " + res);
        return res;
    }

    @Override
    public void start() {
        acceptingThread.start();
    }

    @Override
    public void stop() throws InterruptedException, IOException {
        acceptingThread.interrupt();
        acceptingThread.join();
        for (Future<?> f : futureStats) {
            f.cancel(true);
        }
        threadPool.shutdownNow();
        serverSocket.close();
    }

    @Override
    public int getPort() {
        return serverSocket.getLocalPort();
    }

}