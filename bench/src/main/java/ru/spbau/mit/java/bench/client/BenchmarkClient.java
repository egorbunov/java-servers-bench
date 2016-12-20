package ru.spbau.mit.java.bench.client;


import lombok.extern.slf4j.Slf4j;
import ru.spbau.mit.java.bench.client.stat.FinalStat;
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
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

@Slf4j
public class BenchmarkClient implements Callable<List<FinalStat>> {
    private final ServArchitecture servArchitecture;
    private final String benchHost;
    private final int benchPort;
    private final RunnerOpts opts;
    private final Control whatToChange;
    private final int from;
    private final int to;
    private int step;
    private Consumer<Integer> onProgressChange;
    private Consumer<String> errorCallback;

    public BenchmarkClient(ServArchitecture servArchitecture,
                           String benchHost,
                           int benchPort,
                           RunnerOpts opts,
                           Control whatToChange,
                           int from,
                           int to,
                           int step,
                           Consumer<Integer> onProgressChange,
                           Consumer<String> errorCallback) {
        this.servArchitecture = servArchitecture;
        this.benchHost = benchHost;
        this.benchPort = benchPort;
        this.opts = opts;
        this.whatToChange = whatToChange;
        this.from = from;
        this.to = to;
        this.step = step;
        this.onProgressChange = onProgressChange;
        this.errorCallback = errorCallback;
    }

    private RunnerOpts constructRunnableOpts(int val) {
        switch (whatToChange) {
            case CLIENT_NUM:
                return new RunnerOpts(val, opts.getArrayLen(), opts.getDeltaMs(), opts.getRequestNumber());
            case REQUSET_NUM:
                return new RunnerOpts(opts.getClientNumber(), opts.getArrayLen(), opts.getDeltaMs(), val);
            case DELAY:
                return new RunnerOpts(opts.getClientNumber(), opts.getArrayLen(), val, opts.getRequestNumber());
            case ARRAY_LEN:
                return new RunnerOpts(opts.getClientNumber(), val, opts.getDeltaMs(), opts.getRequestNumber());
        }
        return null;
    }


    @Override
    public List<FinalStat> call() throws Exception {
        ArrayList<FinalStat> finalStats = new ArrayList<>();
        for (int i = from; i < to; i += step) {
            onProgressChange.accept(i);
            RunnerOpts runnerOpts = constructRunnableOpts(i);
            finalStats.add(runForOneOpts(runnerOpts));
            log.debug("Got stats: " + finalStats.get(finalStats.size() - 1));
        }
        return finalStats;
    }

    private FinalStat runForOneOpts(RunnerOpts runnerOpts) {
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
            in.read(statsBs);
            ServerStatsMsg serverStatsMsg = ServerStatsMsg.parseFrom(statsBs);

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
