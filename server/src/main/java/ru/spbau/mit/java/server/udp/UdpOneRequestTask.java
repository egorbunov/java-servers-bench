package ru.spbau.mit.java.server.udp;


import ru.spbau.mit.java.server.RequestProcess;
import ru.spbau.mit.java.server.stat.OneRequestStats;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class UdpOneRequestTask implements Callable<OneRequestStats> {
    private final DatagramPacket requestPacket;
    private final DatagramSocket udpSocket;
    private final long requestRecieveTimeNs;

    /**
     * Creates single udp request task
     *
     * @param requestPacket packet, which contains request data
     * @param udpSocket socket with which response will be written
     * @param requestRecieveTimeNs time stamp of request recieve
     */
    public UdpOneRequestTask(DatagramPacket requestPacket,
                             DatagramSocket udpSocket,
                             long requestRecieveTimeNs) {
        this.requestPacket = requestPacket;
        this.udpSocket = udpSocket;
        this.requestRecieveTimeNs = requestRecieveTimeNs;
    }

    @Override
    public OneRequestStats call() throws IOException {
        long processStart = System.nanoTime();
        byte[] answerBytes = RequestProcess.process(
                Arrays.copyOf(requestPacket.getData(), requestPacket.getLength()));
        long processEnd = System.nanoTime();

        DatagramPacket answerPacket = new DatagramPacket(
                answerBytes,
                answerBytes.length,
                requestPacket.getAddress(),
                requestPacket.getPort());
        udpSocket.send(answerPacket);
        long requestSendTimeNs = System.nanoTime();

        return new OneRequestStats(
                requestSendTimeNs - requestRecieveTimeNs,
                processEnd - processStart
        );
    }
}
