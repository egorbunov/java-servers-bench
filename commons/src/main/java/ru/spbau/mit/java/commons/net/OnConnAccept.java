package ru.spbau.mit.java.commons.net;


import java.net.Socket;

@FunctionalInterface
public interface OnConnAccept {
    void accepted(Socket clientSock);
}
