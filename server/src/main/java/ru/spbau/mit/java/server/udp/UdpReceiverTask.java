package ru.spbau.mit.java.server.udp;

import com.google.protobuf.ByteString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import ru.spbau.mit.java.commons.UDPProtocol;
import ru.spbau.mit.java.commons.proto.IntArrayMsg;
import ru.spbau.mit.java.commons.proto.UDPMessage;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.function.Consumer;

/**
 * Receives datagrams and constructs arrays from them for sorting;
 * Constructed arrays are returned via callback to request processor;
 */
@Slf4j
public class UdpReceiverTask implements Runnable {
    private final DatagramSocket socket;
    private final Consumer<Tuple2<IntArrayMsg, SocketAddress>> onReceiveCallback;
    private final HashMap<SocketAddress, TreeSet<UDPMessage>> datagrams = new HashMap<>();

    public UdpReceiverTask(DatagramSocket socket,
                           Consumer<Tuple2<IntArrayMsg, SocketAddress>> onReceiveCallback) {
        this.socket = socket;
        this.onReceiveCallback = onReceiveCallback;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            byte[] msg = new byte[UDPProtocol.MAX_UDP_DATAGRAM_SIZE];
            DatagramPacket packet = new DatagramPacket(msg, msg.length);
            try {
                socket.receive(packet);
                val addr = packet.getSocketAddress();
                if (!datagrams.containsKey(addr)) {
                    datagrams.put(addr, new TreeSet<>(UDPProtocol.UDP_ARR_PART_MSG_COMPARATOR));
                }
                val udpMsg = UDPMessage.parseFrom(Arrays.copyOf(packet.getData(), packet.getLength()));
                TreeSet<UDPMessage> dset = datagrams.get(addr);
                if (!dset.add(udpMsg)) {
                    log.debug("Packet resent, already got one; IDX = " + udpMsg.getPartIndex());
                }
                log.debug("Got " + dset.size() + " out of " + udpMsg.getTotalPartNum() + " packets");

                // in case all parts of array received
                if (dset.size() == udpMsg.getTotalPartNum()) {
                    ByteString byteString = dset.descendingSet().stream()
                            .map(UDPMessage::getArrayBytes)
                            .reduce(ByteString::concat)
                            .orElse(udpMsg.toByteString());
                    val result = IntArrayMsg.parseFrom(byteString);
                    datagrams.remove(addr);
                    onReceiveCallback.accept(Tuple.tuple(result, addr));
                }
            } catch (IOException e) {
                log.error("Error receiving datagram: " + e);
            }
        }
    }
}
