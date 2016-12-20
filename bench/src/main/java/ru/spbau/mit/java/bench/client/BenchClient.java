package ru.spbau.mit.java.bench.client;

import ru.spbau.mit.java.commons.ServArchitecture;
import ru.spbau.mit.java.commons.proto.BenchOptsMsg;
import ru.spbau.mit.java.commons.proto.ServerStatsMsg;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Client, which connects to server runner and sends him options
 * and info which server arch. to benchmark, after it waits for
 * benchmark results
 */
public class BenchClient {
    public static void main(String[] args) throws IOException {
        int port = 5554;
        int clientNumber = 10;
        int requestNumber = 10;
        int archType = ServArchitecture.TCP_THREAD_PER_CLIENT;

        BenchOptsMsg optsMsg = BenchOptsMsg.newBuilder()
                .setClientNumber(clientNumber)
                .setServerPort(port)
                .setRequestsNumber(requestNumber)
                .setServerArchitecture(archType)
                .build();



        int benchServerPort = 6666;
        String benchServerHost = "localhost";

        Socket socket = new Socket(benchServerHost, benchServerPort);

        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        DataInputStream in = new DataInputStream(socket.getInputStream());

        byte[] optsBytes = optsMsg.toByteArray();
        out.writeInt(optsBytes.length);
        out.write(optsBytes);

        int ansLen = in.readInt();
        byte[] statsBs = new byte[ansLen];
        ServerStatsMsg serverStatsMsg = ServerStatsMsg.parseFrom(statsBs);

        System.out.println(serverStatsMsg.toString());
    }
}
