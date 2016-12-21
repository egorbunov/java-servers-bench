package ru.spbau.mit.java.bench.client;


import lombok.extern.slf4j.Slf4j;
import ru.spbau.mit.java.bench.client.stat.FinalStat;
import ru.spbau.mit.java.bench.client.stat.BenchmarkResults;
import ru.spbau.mit.java.client.ClientCreator;
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
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

@Slf4j
public class BenchmarkClient {
    private final RunnerOpts runnerOpts;
    private final String benchHost;
    private final int benchPort;
    private final ServArchitecture servArchitecture;
    private final Consumer<String> errorCallback;

    public BenchmarkClient(RunnerOpts runnerOpts,
                           String benchHost, int benchPort,
                           ServArchitecture servArchitecture,
                           Consumer<String> errorCallback) {

        this.runnerOpts = runnerOpts;
        this.benchHost = benchHost;
        this.benchPort = benchPort;
        this.servArchitecture = servArchitecture;
        this.errorCallback = errorCallback;
    }


    public FinalStat run() {
        try (Socket socket = new Socket(benchHost, benchPort)) {
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            DataInputStream in = new DataInputStream(socket.getInputStream());

            BenchOptsMsg optsMsg = BenchOptsMsg.newBuilder()
                    .setClientNumber(runnerOpts.getClientNumber())
                    .setServerPort(benchPort + 1)
                    .setRequestsNumber(runnerOpts.getRequestNumber())
                    .setServerArchitecture(servArchitecture.getCode())
                    .build();

            byte[] optsBytes = optsMsg.toByteArray();
            out.writeInt(optsBytes.length);
            out.write(optsBytes);

            boolean startedOk = in.readBoolean();
            if (!startedOk) {
                System.out.println("=(");
                socket.close();
            }

            ClientCreator clientCreator = null;
            if (servArchitecture == ServArchitecture.TCP_NON_BLOCKING ||
                    servArchitecture == ServArchitecture.TCP_THREAD_POOL ||
                    servArchitecture == ServArchitecture.TCP_THREAD_PER_CLIENT) {

                clientCreator = new TcpClient.Creator(benchHost, benchPort + 1);
            } else if (servArchitecture == ServArchitecture.TCP_SINGLE_THREADED) {
            } else if (servArchitecture == ServArchitecture.UDP_THREAD_PER_REQUEST) {
            } else if (servArchitecture == ServArchitecture.UDP_THREAD_POOL) {
            }

            if (clientCreator == null) {
                throw new IllegalArgumentException("Unknown serv architecture =(");
            }

            ClientRunner clientRunner = new ClientRunner(new RunnerOpts(
                    runnerOpts.getClientNumber(),
                    runnerOpts.getArrayLen(),
                    runnerOpts.getDeltaMs(),
                    runnerOpts.getRequestNumber()),
                    clientCreator);

            double avClientLife = clientRunner.run();

            int ansLen = in.readInt();
            byte[] statsBs = new byte[ansLen];
            in.readFully(statsBs);
            ServerStatsMsg serverStatsMsg = ServerStatsMsg.parseFrom(statsBs);

            // end!
            out.writeInt(-1);

            return new FinalStat(
                    serverStatsMsg.getAvRequestNs(),
                    serverStatsMsg.getAvSortingNs(),
                    avClientLife
            );
        } catch (UnknownHostException e) {
            log.error("Unknown host! " + e.getMessage());
            errorCallback.accept("Unknown host! " + e.getMessage());
        } catch (IOException e) {
            log.error("IO Excpetion: " + e.getMessage());
            errorCallback.accept("IO Excpetion: " + e.getMessage());
        }
        return null;
    }
}
