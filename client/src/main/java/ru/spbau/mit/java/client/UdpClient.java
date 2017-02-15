package ru.spbau.mit.java.client;

import com.google.protobuf.ByteString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import ru.spbau.mit.java.commons.UDPProtocol;
import ru.spbau.mit.java.commons.proto.IntArrayMsg;
import ru.spbau.mit.java.commons.proto.UDPMessage;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

/**
 * Client, which sends UDP Datagrams to server;
 */
@Slf4j
public class UdpClient implements Client {
    private final String host;
    private final int port;
    private final DatagramSocket socket;

    /**
     *
     * @param host there to send data (server)
     * @param port port at which server is listening for packets
     * @throws SocketException
     */
    public UdpClient(String host, int port) throws SocketException {
        this.host = host;
        this.port = port;
        this.socket = new DatagramSocket();
        UDPProtocol.setupClientUDPSocket(socket);
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

    /**
     * Sends array in as as sequence of datagrams; Every datagram is sent in a
     * blocking manner with waiting for acknowledgement from server; After all
     * datagrams are sent, client waits for the set of datagrams with sorted array
     */
    @Override
    public IntArrayMsg makeBlockingRequest(IntArrayMsg toSort) throws IOException {
        List<UDPMessage> udpMessages = UDPProtocol.splitArrayToUdpMsgs(toSort);

        log.debug("Sending " + udpMessages.size() + " datagrams representing one array...");
        for (val msg : udpMessages) {
            byte[] msgBytes = msg.toByteArray();
            val packet = new DatagramPacket(msgBytes, msgBytes.length, InetAddress.getByName(host), port);
            socket.send(packet);
        }

        log.debug("Waiting for answer...");
        socket.setReceiveBufferSize(UDPProtocol.MAX_UDP_DATAGRAM_SIZE * 10);
        byte[] ansPart = new byte[UDPProtocol.MAX_UDP_DATAGRAM_SIZE];
        TreeSet<UDPMessage> msgs = new TreeSet<>(UDPProtocol.UDP_ARR_PART_MSG_COMPARATOR);
        while (true) {
            val ans = new DatagramPacket(ansPart, ansPart.length);
            socket.receive(ans);
            val msg = UDPMessage.parseFrom(Arrays.copyOf(ans.getData(), ans.getLength()));
            msgs.add(msg);
            log.debug("Got " + msgs.size() + " out of " + msg.getTotalPartNum() + " packets");
            if (msgs.size() == msg.getTotalPartNum()) {
                ByteString byteString = msgs.descendingSet().stream()
                        .map(UDPMessage::getArrayBytes)
                        .reduce(ByteString::concat)
                        .orElse(msg.toByteString());
                val result = IntArrayMsg.parseFrom(byteString);
                return result;
            }
        }
    }


    @Override
    public void disconnect() throws IOException {
        socket.close();
    }
}
