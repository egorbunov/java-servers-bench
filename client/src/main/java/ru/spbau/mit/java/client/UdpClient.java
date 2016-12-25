package ru.spbau.mit.java.client;

import lombok.extern.slf4j.Slf4j;
import ru.spbau.mit.java.commons.proto.IntArrayMsg;
import ru.spbau.mit.java.commons.proto.Protobuf;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;

/**
 * Client, which sends UDP Datagrams to server
 */
@Slf4j
public class UdpClient implements Client {
    private final String host;
    private final int port;
    private final DatagramSocket socket;

    public UdpClient(String host, int port) throws SocketException {
        this.host = host;
        this.port = port;
        this.socket = new DatagramSocket();
    }

    public static class Creator implements ClientCreator {
        private final String host;
        private final int port;

        public Creator(String host, int port) {
            this.host = host;
            this.port = port;
        }

        @Override
        public Client create() throws IOException {
            return new UdpClient(host, port);
        }
    }

    @Override
    public IntArrayMsg makeBlockingRequest(IntArrayMsg toSort) throws IOException {
        byte[] msg = toSort.toByteArray();
        DatagramPacket packet = new DatagramPacket(
                msg, msg.length, InetAddress.getByName(host), port);
        socket.send(packet);
        byte[] ansBs = new byte[Protobuf.predictArrayMsgSize(toSort.getNumbersCount())];
        DatagramPacket answer = new DatagramPacket(ansBs, ansBs.length);
        socket.receive(answer);
        return IntArrayMsg.parseFrom(Arrays.copyOf(answer.getData(), answer.getLength()));
    }

    @Override
    public void disconnect() throws IOException {
        socket.close();
    }
}
