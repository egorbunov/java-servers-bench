package ru.spbau.mit.java.benchserver;

import lombok.extern.slf4j.Slf4j;
import ru.spbau.mit.java.commons.net.ConnectionAcceptor;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Waits for client;
 * Reads information about which server architecture to start and which parameters to use...
 * Starts benchmark
 * Responses with statistics
 */
@Slf4j
public class ServerRunner {
    private final ServerSocket serverSocket;
    private final ExecutorService clientExecutor;
    private final ExecutorService acceptor;

    private Future<?> acceptingTaskF;
    private final List<Future<?>> clientTasksFs = new ArrayList<>();


    public ServerRunner(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        clientExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        acceptor = Executors.newSingleThreadExecutor();
    }

    void start() {
        acceptingTaskF = acceptor.submit(new ConnectionAcceptor(
                serverSocket,
                clientSock -> {
                    log.debug("Got connection from client: " + clientSock.getInetAddress());
                    clientTasksFs.add(clientExecutor.submit(new OneBenchClientTask(clientSock)));
                }
                ));
    }


    void stop() throws IOException {
        acceptingTaskF.cancel(true);
        for (Future f : clientTasksFs) {
            f.cancel(true);
        }
        serverSocket.close();
        clientExecutor.shutdownNow();
        acceptor.shutdownNow();
    }
}
