package ru.spbau.mit.java.commons.net;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Task, which accepts connections
 */
@Slf4j
public class ConnectionAcceptor implements Runnable {
    private final ServerSocket serverSocket;
    private final OnConnAccept acceptAction;

    public ConnectionAcceptor(ServerSocket serverSocket,
                              OnConnAccept acceptAction) {
        this.serverSocket = serverSocket;
        this.acceptAction = acceptAction;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            Socket clientSock;
            try {
                clientSock = serverSocket.accept();
                acceptAction.accepted(clientSock);
            } catch (IOException e) {
                log.error("Error accepting connection: " + e.getMessage());
            }
        }
    }
}
