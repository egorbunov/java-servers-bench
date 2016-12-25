package ru.spbau.mit.java.benchserver;

import lombok.extern.slf4j.Slf4j;
import ru.spbau.mit.java.commons.BenchmarkStatusCode;
import ru.spbau.mit.java.commons.ServArchitecture;
import ru.spbau.mit.java.commons.proto.BenchmarkOpts;
import ru.spbau.mit.java.commons.proto.ServerStatsMsg;
import ru.spbau.mit.java.server.BenchServer;
import ru.spbau.mit.java.server.BenchingError;
import ru.spbau.mit.java.server.tcp.simple.SingleThreadTcpServer;
import ru.spbau.mit.java.server.tcp.simple.ThreadPoolTcpServer;
import ru.spbau.mit.java.server.tcp.simple.ThreadedTcpServer;
import ru.spbau.mit.java.server.stat.ServerStats;
import ru.spbau.mit.java.server.udp.FixedPoolUdpServer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Task, which serves requests from one client
 */
@Slf4j
public class OneBenchClientTask implements Runnable {
    private final Socket sock;
    public OneBenchClientTask(Socket sock) {
        this.sock = sock;
    }

    @Override
    public void run() {
        try (Socket clientSocket = sock) {
            DataInputStream in = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());

            while (!Thread.currentThread().isInterrupted()) {
                BenchmarkOpts opts = readBenchOptions(in);
                if (opts == null) {
                    log.debug("Bench client disconnected");
                    break;
                }

                log.debug("Got bench options: " + opts.toString());
                BenchServer serverToBench = createBenchServer(opts);
                if (serverToBench == null) {
                    out.writeInt(BenchmarkStatusCode.BAD_ARCH);
                    continue;
                }

                log.debug("Starting server for benchmark...");
                serverToBench.start();
                // client can start benching clients
                out.writeInt(BenchmarkStatusCode.BENCH_READY);
                // send port, where the server is running
                out.writeInt(serverToBench.getPort());

                int code = in.readInt();
                if (code != BenchmarkStatusCode.STOP_BENCH) {
                    log.error("Got strange code from client, expected STOP_BENCH code");
                }

                log.debug("Stopping server for benchmark...");
                ServerStats stats = null;
                try {
                    stats = serverToBench.stop();
                } catch (InterruptedException e) {
                    log.error("Failed to stop server for benchmarking: " + e.getMessage());
                } catch (BenchingError e) {
                    log.error("Failed to stop server for benchmarking: " + e.getCause());
                }

                writeAnswerToClient(stats, out);
            }
        } catch (IOException e) {
            log.error("IO Error: " + e.getMessage());
        }
    }

    private void writeAnswerToClient(ServerStats stats, DataOutputStream out) throws IOException {
        if (stats == null) {
            out.writeInt(BenchmarkStatusCode.BENCH_FAILED);
            return;
        }

        // ok
        log.info(stats.toString());
        ServerStatsMsg statsMsg = ServerStatsMsg.newBuilder()
                .setAvReceiveSendGapNs(stats.getAvReceiveSendGapNs())
                .setAvRequestProcNs(stats.getAvRequestProcNs())
                .build();
        byte[] bsStats = statsMsg.toByteArray();
        out.writeInt(bsStats.length);
        out.write(bsStats);
    }

    private BenchServer createBenchServer(BenchmarkOpts opts) throws IOException {
        ServArchitecture arch = ServArchitecture.fromCode(opts.getServerArchitecture());
        if (arch == null) {
            log.error("Not supported server arch");
            return null;
        }
        switch (arch) {
            case TCP_THREAD_PER_CLIENT: {
                return new ThreadedTcpServer(0);
            }
            case TCP_THREAD_POOL: {
                return new ThreadPoolTcpServer(0);
            }
            case TCP_SINGLE_THREADED: {
                return new SingleThreadTcpServer(0);
            }
            case UDP_THREAD_POOL: {
                return new FixedPoolUdpServer(0, Runtime.getRuntime().availableProcessors() - 1,
                        opts.getMaxArraySize());
            }
            default: {
                log.info("Not supported " + opts.getServerArchitecture());
                break;
            }
        }
        return null;
    }

    private BenchmarkOpts readBenchOptions(DataInputStream in) throws IOException {
        int msgLen = in.readInt();

        if (msgLen == BenchmarkStatusCode.DISCONNECT) {
            log.debug("Client disconnected");
            return null;
        }

        byte[] msg = new byte[msgLen];
        in.readFully(msg);
        return BenchmarkOpts.parseFrom(msg);
    }
}
