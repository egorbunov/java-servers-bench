package ru.spbau.mit.java.bench.client;


import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import ru.spbau.mit.java.bench.stat.FinalStat;
import ru.spbau.mit.java.client.ClientCreator;
import ru.spbau.mit.java.client.TcpConnectionPerRequestClient;
import ru.spbau.mit.java.client.TcpConnectionPreservingClient;
import ru.spbau.mit.java.client.runner.ClientRunner;
import ru.spbau.mit.java.client.runner.RunnerOpts;
import ru.spbau.mit.java.commons.BenchReqCode;
import ru.spbau.mit.java.commons.ServArchitecture;
import ru.spbau.mit.java.commons.proto.BenchOptsMsg;
import ru.spbau.mit.java.commons.proto.ServerStatsMsg;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.function.Consumer;

/**
 * Benchmark client, initialized with options specifying
 * what server architecture to use and how many clients to spawn, etc...
 * It runs once:
 *    - sends benchmark info to server-runner (benchmark server, who starts server)
 *    - awaits while server-runner answers with 'ok' (server for bench started successfully)
 *    - starts spawning clients and waits for all of them
 *    - receives server statistics from server-runner
 */
@Slf4j
public class BenchmarkClient {
    private final RunnerOpts runnerOpts;
    private final String benchHost;
    private final int benchServerRunnerPort;
    private final ServArchitecture servArchitecture;
    private final Consumer<String> errorCallback;

    public BenchmarkClient(RunnerOpts runnerOpts,
                           String benchHost,
                           int benchServerRunnerPort,
                           ServArchitecture servArchitecture,
                           Consumer<String> errorCallback) {

        this.runnerOpts = runnerOpts;
        this.benchHost = benchHost;
        this.benchServerRunnerPort = benchServerRunnerPort;
        // because sometimes server socket not freed quickly
        this.servArchitecture = servArchitecture;
        this.errorCallback = errorCallback;
    }


    public FinalStat run() throws BenchError {
        try (Socket socket = new Socket(benchHost, benchServerRunnerPort)) {
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            DataInputStream in = new DataInputStream(socket.getInputStream());

            sendBenchmarkOptions(out);
            int benchPort = waitBenchServerReady(in);
            log.debug("Bench server started at port: " + benchPort);

            ClientCreator clientCreator = createClientFactory(benchPort);

            ClientRunner clientRunner = new ClientRunner(new RunnerOpts(
                    runnerOpts.getClientNumber(),
                    runnerOpts.getArrayLen(),
                    runnerOpts.getDeltaMs(),
                    runnerOpts.getRequestNumber()),
                    clientCreator);

            double avClientLife = clientRunner.run();
            int ansLen = in.readInt();
            if (ansLen == BenchReqCode.BENCH_FAILED) {
                log.error("benchmark failed (see bench runner logs)");
                throw new BenchError("Bench. server ERROR: benchmark failed");
            }

            // reading server stats
            byte[] statsBs = new byte[ansLen];
            in.readFully(statsBs);
            ServerStatsMsg serverStatsMsg = ServerStatsMsg.parseFrom(statsBs);

            // disconnecting this client from server
            out.writeInt(BenchReqCode.DISCONNECT);

            return new FinalStat(
                    serverStatsMsg.getAvRequestNs(),
                    serverStatsMsg.getAvSortingNs(),
                    avClientLife
            );
        } catch (UnknownHostException e) {
            log.error("Unknown host! " + e.getMessage());
            errorCallback.accept("Unknown host! " + e.getMessage());
        } catch (IOException e) {
            log.error("IO Exception: " + e.getMessage());
            errorCallback.accept("IO Exception: " + e.getMessage());
        }
        return null;
    }

    @NotNull
    private ClientCreator createClientFactory(int benchPort) throws BenchError {
        ClientCreator clientCreator = null;
        if (servArchitecture == ServArchitecture.TCP_NON_BLOCKING ||
                servArchitecture == ServArchitecture.TCP_THREAD_POOL ||
                servArchitecture == ServArchitecture.TCP_THREAD_PER_CLIENT) {

            clientCreator = new TcpConnectionPreservingClient.Creator(benchHost, benchPort);
        } else if (servArchitecture == ServArchitecture.TCP_SINGLE_THREADED) {
            clientCreator = new TcpConnectionPerRequestClient.Creator(benchHost, benchPort);
        } else if (servArchitecture == ServArchitecture.UDP_THREAD_PER_REQUEST) {
        } else if (servArchitecture == ServArchitecture.UDP_THREAD_POOL) {
        }
        if (clientCreator == null) {
            throw new BenchError("Unsupported server architecture (can't create client) =(");
        }
        return clientCreator;
    }

    /**
     * returns port, where bench server started
     */
    private int waitBenchServerReady(DataInputStream in) throws IOException, BenchError {
        int status = in.readInt();
        if (status == BenchReqCode.BAD_ARCH) {
            log.error("can't create server with given architecture");
            throw new BenchError("Bench. server ERROR: can't create server with given architecture");
        }
        if (status != BenchReqCode.BENCH_READY) {
            log.error("unknown error");
            throw new BenchError("Bench. server ERROR: unknown error");
        }
        return in.readInt();
    }

    private void sendBenchmarkOptions(DataOutputStream out) throws IOException {
        BenchOptsMsg optsMsg = BenchOptsMsg.newBuilder()
                .setClientNumber(runnerOpts.getClientNumber())
                .setRequestsNumber(runnerOpts.getRequestNumber())
                .setServerArchitecture(servArchitecture.getCode())
                .build();

        byte[] optsBytes = optsMsg.toByteArray();
        out.writeInt(optsBytes.length);
        out.write(optsBytes);
    }
}
