package ru.spbau.mit.java.bench.client;

import ru.spbau.mit.java.client.TcpClient;
import ru.spbau.mit.java.client.runner.ClientRunner;
import ru.spbau.mit.java.client.runner.RunnerOpts;
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
        int benchingServerPort = 5554;
        int clientNumber = 1;
        int requestNumber = 5;
        int archType = ServArchitecture.TCP_THREAD_PER_CLIENT;

        BenchOptsMsg optsMsg = BenchOptsMsg.newBuilder()
                .setClientNumber(clientNumber)
                .setServerPort(benchingServerPort)
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

        boolean startedOk = in.readBoolean();
        if (!startedOk) {
            System.out.println("=(");
            socket.close();
        }

        ClientRunner clientRunner = new ClientRunner(new RunnerOpts(
                clientNumber,
                10,
                1000,
                requestNumber),
                new TcpClient.Creator(benchServerHost, benchingServerPort));

        clientRunner.run();

        int ansLen = in.readInt();
        byte[] statsBs = new byte[ansLen];
        in.read(statsBs);
        ServerStatsMsg serverStatsMsg = ServerStatsMsg.parseFrom(statsBs);

        System.out.println(serverStatsMsg.toString());
        socket.close();
    }
}
