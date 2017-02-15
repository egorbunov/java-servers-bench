package ru.spbau.mit.java.server.udp;


import lombok.extern.slf4j.Slf4j;
import lombok.val;
import ru.spbau.mit.java.commons.UDPProtocol;
import ru.spbau.mit.java.commons.proto.IntArrayMsg;
import ru.spbau.mit.java.server.RequestProcess;
import ru.spbau.mit.java.server.stat.OneRequestStats;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.concurrent.Callable;

@Slf4j
public class UdpOneRequestTask implements Callable<OneRequestStats> {
    private final IntArrayMsg arrayToSort;
    private SocketAddress recipientAddr;
    private final DatagramSocket udpSocket;
    private final long requestRecieveTimeNs;

    /**
     * Creates task for processing single message
     */
    public UdpOneRequestTask(IntArrayMsg arrayToSort,
                             SocketAddress recipientAddr,
                             DatagramSocket udpSocket,
                             long requestRecieveTimeNs) {
        this.arrayToSort = arrayToSort;
        this.recipientAddr = recipientAddr;
        this.udpSocket = udpSocket;
        this.requestRecieveTimeNs = requestRecieveTimeNs;
    }

    @Override
    public OneRequestStats call() throws IOException {
        long processStart = System.nanoTime();
        val sortedArray = RequestProcess.processArray(arrayToSort);
        long processEnd = System.nanoTime();

        val udpMessages = UDPProtocol.splitArrayToUdpMsgs(sortedArray);

        log.debug("Sending " + udpMessages.size() + " datagrams representing one SORTED array...");
        for (val msg : udpMessages) {
            byte[] msgBytes = msg.toByteArray();
            val packet = new DatagramPacket(msgBytes, msgBytes.length, recipientAddr);
            udpSocket.send(packet);
        }

        long requestSendTimeNs = System.nanoTime();

        return new OneRequestStats(
                requestSendTimeNs - requestRecieveTimeNs,
                processEnd - processStart
        );
    }
}
