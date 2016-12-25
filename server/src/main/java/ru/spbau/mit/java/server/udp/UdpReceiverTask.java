package ru.spbau.mit.java.server.udp;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.function.Consumer;

@Slf4j
public class UdpReceiverTask implements Runnable {
    private final DatagramSocket socket;
    private final int maxDatagramSize;
    private final Consumer<DatagramPacket> onReceiveCallback;

    public UdpReceiverTask(DatagramSocket socket,
                           int maxDatagramSize,
                           Consumer<DatagramPacket> onReceiveCallback) {
        this.socket = socket;
        this.maxDatagramSize = maxDatagramSize;
        this.onReceiveCallback = onReceiveCallback;
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            byte[] msg = new byte[maxDatagramSize];
            DatagramPacket packet = new DatagramPacket(msg, msg.length);
            try {
                socket.receive(packet);
            } catch (IOException e) {
                log.error("Error receiving datagram: " + e);
                continue;
            }

            log.debug("Received datagram! Ok.");
            onReceiveCallback.accept(packet);
        }
    }
}
