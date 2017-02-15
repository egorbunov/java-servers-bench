package ru.spbau.mit.java.commons;

import com.google.protobuf.ByteString;
import lombok.extern.slf4j.Slf4j;
import org.jooq.lambda.tuple.Tuple2;
import ru.spbau.mit.java.commons.proto.IntArrayMsg;
import ru.spbau.mit.java.commons.proto.UDPMessage;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Unfortunately we physically can't send UDP datagrams with too big size and consequently
 * we can't send too big arrays for sorting in only one packet. So I'am dealing with it simply
 * be splitting one big array message into number of datagrams of size 65000. Every such array-part
 * packet has it's index and also it carries total number of part-packets, which contain the whole array.
 */
@Slf4j
public class UDPProtocol {
    static public final int MAX_UDP_DATAGRAM_SIZE = 65000;
    static private final int ARRAY_SEGMENT_MAX_PART = 64000;


    public static List<UDPMessage> splitArrayToUdpMsgs(IntArrayMsg arr) {
        byte[] bytes = arr.toByteArray();
        final int totalPartsNumber =
                bytes.length / ARRAY_SEGMENT_MAX_PART + ((bytes.length % ARRAY_SEGMENT_MAX_PART == 0) ? 0 : 1);

        return IntStream
                .range(0, totalPartsNumber)
                .mapToObj(idx -> {
                    int offset = idx * ARRAY_SEGMENT_MAX_PART;
                    return new Tuple2<>(
                            offset / ARRAY_SEGMENT_MAX_PART,
                            ByteString.copyFrom(bytes, offset,
                                    Math.min(bytes.length - offset, ARRAY_SEGMENT_MAX_PART))
                    );
                })
                .map(t -> UDPMessage.newBuilder()
                        .setMsgType(UDPMessage.Type.PART)
                        .setPartIndex(t.v1)
                        .setTotalPartNum(totalPartsNumber)
                        .setArrayBytes(t.v2).build())
                .collect(Collectors.toList());
    }

    /**
     * Generate ack message
     */
    public static UDPMessage ackUdpMsg(int packetIdx) {
        return UDPMessage.newBuilder().setMsgType(UDPMessage.Type.ACK).setPartIndex(packetIdx).build();
    }


    public static Comparator<UDPMessage> UDP_ARR_PART_MSG_COMPARATOR = Comparator.comparing(m -> -m.getPartIndex());

    /**
     * Data is transmitted to link not very fast so not to lost udp packets here
     * I increase buffer sizes for UDP socket buffer
     */
    public static void setupClientUDPSocket(DatagramSocket socket) throws SocketException {
        socket.setReceiveBufferSize(1000000); // haha, don't be so naive
        socket.setSendBufferSize(1000000);
        log.debug("UDP Receive buf (client) " + socket.getReceiveBufferSize());
        log.debug("UDP Send buf (client) " + socket.getSendBufferSize());
        socket.setSoTimeout(10000);
    }

    public static void setupServerUDPSocket(DatagramSocket socket) throws SocketException {
        socket.setReceiveBufferSize(1000000);
        socket.setSendBufferSize(1000000);
        log.debug("UDP Receive buf (server) " + socket.getReceiveBufferSize());
        log.debug("UDP Send buf (server) " + socket.getSendBufferSize());
        socket.setSoTimeout(10000);
    }
}
