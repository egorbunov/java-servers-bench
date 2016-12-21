package ru.spbau.mit.java.server.tcp;

import lombok.extern.slf4j.Slf4j;
import ru.spbau.mit.java.commons.net.ConnectionAcceptor;
import ru.spbau.mit.java.server.BenchOpts;
import ru.spbau.mit.java.server.BenchServer;
import ru.spbau.mit.java.server.stat.OneClientStats;
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
    private final BenchOpts opts;
    private final Thread acceptingThread;
    private final List<ExecutorService> clientThreads;
    private final List<Future<OneClientStats>> futureStats;

    public ThreadedTcpServer(int port, BenchOpts opts) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.opts = opts;
        this.clientThreads = new ArrayList<>(opts.getClientNum());
        this.futureStats = new ArrayList<>(opts.getClientNum());
        // creating client accepting thread
        this.acceptingThread = new Thread(
                new ConnectionAcceptor(
                        serverSocket,
                        clientSock -> {
                            // handle connection accepted event
                            ExecutorService clientExecutor = Executors.newSingleThreadExecutor();
                            clientThreads.add(clientExecutor);
                            futureStats.add(clientExecutor.submit(new ClientServingTask(clientSock)));
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
        List<OneClientStats> stats = new ArrayList<>();
        for (Future<OneClientStats> f : futureStats) {
            try {
                stats.add(f.get());
            } catch (InterruptedException e) {
                log.error("Interrupt during client wait: " + e.getMessage());
            } catch (ExecutionException e) {
                log.error("Client failed during execution: " + e.getCause());
            }
        }
        log.debug("Shutting down threads...");
        for (ExecutorService ct : clientThreads) {
            ct.shutdown();
        }
        ServerStats res = ServerStats.calc(stats);
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
        for (Future<OneClientStats> f : futureStats) {
            f.cancel(true);
        }
        for (ExecutorService ct : clientThreads) {
            ct.shutdownNow();
        }
        serverSocket.close();
    }

}
