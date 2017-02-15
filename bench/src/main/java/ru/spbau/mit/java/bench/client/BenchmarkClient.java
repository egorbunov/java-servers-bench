package ru.spbau.mit.java.bench.client;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jetbrains.annotations.NotNull;
import ru.spbau.mit.java.bench.stat.FinalStat;
import ru.spbau.mit.java.client.ClientCreator;
import ru.spbau.mit.java.client.TcpConnectionPerRequestClient;
import ru.spbau.mit.java.client.TcpConnectionPreservingClient;
import ru.spbau.mit.java.client.UdpClient;
import ru.spbau.mit.java.client.runner.ClientRunner;
import ru.spbau.mit.java.client.runner.RunnerOpts;
import ru.spbau.mit.java.commons.BenchmarkStatusCode;
import ru.spbau.mit.java.commons.ServerArch;
import ru.spbau.mit.java.commons.proto.BenchmarkOpts;
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
    private final ServerArch serverArch;
    private final Consumer<String> errorCallback;

    public BenchmarkClient(RunnerOpts runnerOpts,
                           String benchHost,
                           int benchServerRunnerPort,
                           ServerArch serverArch,
                           Consumer<String> errorCallback) {

        this.runnerOpts = runnerOpts;
        this.benchHost = benchHost;
        this.benchServerRunnerPort = benchServerRunnerPort;
        // because sometimes server socket not freed quickly
        this.serverArch = serverArch;
        this.errorCallback = errorCallback;
    }


    public FinalStat run() throws BenchmarkError {
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
            out.writeInt(BenchmarkStatusCode.STOP_BENCH);

            int ansLen = in.readInt();
            if (ansLen == BenchmarkStatusCode.BENCH_FAILED) {
                log.error("benchmark failed (see bench runner logs)");
                throw new BenchmarkError("Bench. server ERROR: benchmark failed");
            }

            // reading server stats
            byte[] statsBs = new byte[ansLen];
            in.readFully(statsBs);
            ServerStatsMsg serverStatsMsg = ServerStatsMsg.parseFrom(statsBs);

            return new FinalStat(
                    serverStatsMsg.getAvReceiveSendGapNs(),
                    serverStatsMsg.getAvRequestProcNs(),
                    avClientLife
            );
        } catch (UnknownHostException e) {
            log.error("Unknown host! " + e.getMessage());
            errorCallback.accept("Unknown host! " + ExceptionUtils.getStackTrace(e));
        } catch (IOException e) {
            log.error("IO Exception: " + ExceptionUtils.getStackTrace(e));
            errorCallback.accept("IO Exception: " + ExceptionUtils.getStackTrace(e));
        } catch (Exception e) {
            log.error("Specific error: " + ExceptionUtils.getStackTrace(e));
            errorCallback.accept("Error during client execution: " + ExceptionUtils.getStackTrace(e));
        }
        return null;
    }

    @NotNull
    private ClientCreator createClientFactory(int benchPort) throws BenchmarkError {
        ClientCreator clientCreator = null;
        if (serverArch == ServerArch.TCP_NON_BLOCKING ||
                serverArch == ServerArch.TCP_THREAD_POOL ||
                serverArch == ServerArch.TCP_THREAD_PER_CLIENT) {
            clientCreator = new TcpConnectionPreservingClient.Creator(benchHost, benchPort);
        } else if (serverArch == ServerArch.TCP_SINGLE_THREADED) {
            clientCreator = new TcpConnectionPerRequestClient.Creator(benchHost, benchPort);
        } else if (serverArch == ServerArch.UDP_THREAD_PER_REQUEST
                || serverArch == ServerArch.UDP_THREAD_POOL) {
            clientCreator = new UdpClient.Creator(benchHost, benchPort);
        }
        if (clientCreator == null) {
            throw new BenchmarkError("Unsupported server architecture (can't create client) =(");
        }
        return clientCreator;
    }

    /**
     * returns port, where bench server started
     */
    private int waitBenchServerReady(DataInputStream in) throws IOException, BenchmarkError {
        int status = in.readInt();
        if (status == BenchmarkStatusCode.BAD_ARCH) {
            log.error("can't create server with given architecture");
            throw new BenchmarkError("Bench. server ERROR: can't create server with given architecture");
        }
        if (status != BenchmarkStatusCode.BENCH_READY) {
            log.error("unknown error");
            throw new BenchmarkError("Bench. server ERROR: unknown error");
        }
        return in.readInt();
    }

    private void sendBenchmarkOptions(DataOutputStream out) throws IOException {
        BenchmarkOpts optsMsg = BenchmarkOpts.newBuilder()
                .setServerArchitecture(serverArch.getCode())
                .setMaxArraySize(runnerOpts.getArrayLen())
                .build();

        byte[] optsBytes = optsMsg.toByteArray();
        out.writeInt(optsBytes.length);
        out.write(optsBytes);
    }
}
