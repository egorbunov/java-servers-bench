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
    private final int maxAcceptNum;
    private final ServerSocket serverSocket;
    private final OnConnAccept acceptAction;
    private int curAcceptNum = 0;

    public ConnectionAcceptor(ServerSocket serverSocket,
                              OnConnAccept acceptAction) {
        this.serverSocket = serverSocket;
        this.acceptAction = acceptAction;
        maxAcceptNum = -1;
    }

    public ConnectionAcceptor(ServerSocket serverSocket,
                              OnConnAccept acceptAction,
                              int maxAcceptNum) {
        this.serverSocket = serverSocket;
        this.acceptAction = acceptAction;
        this.maxAcceptNum = maxAcceptNum;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            if (maxAcceptNum > 0 && curAcceptNum >= maxAcceptNum) {
                break;
            }

            Socket clientSock;
            try {
                clientSock = serverSocket.accept();
                curAcceptNum += 1;
                acceptAction.accepted(clientSock);
            } catch (IOException e) {
                log.error("Error accepting connection: " + e.getMessage());
            }
        }
    }
}