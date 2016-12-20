package ru.spbau.mit.java.benchserver;

import lombok.extern.slf4j.Slf4j;
import ru.spbau.mit.java.commons.net.ConnectionAcceptor;
import ru.spbau.mit.java.commons.net.OnConnAccept;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    public ServerRunner(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        clientExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        acceptor = Executors.newSingleThreadExecutor();
    }

    void start() {
        acceptor.submit(new ConnectionAcceptor(
                serverSocket,
                clientSock -> {
                    log.debug("Got connection from client: " + clientSock.getInetAddress());
                    clientExecutor.submit(new OneBenchClientTask(clientSock));
                }
                ));
    }


    void stop() {
        clientExecutor.shutdownNow();
        acceptor.shutdownNow();
    }
}
