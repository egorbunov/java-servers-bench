package ru.spbau.mit.java.benchserver;

import lombok.extern.slf4j.Slf4j;
import ru.spbau.mit.java.commons.BenchReqCode;
import ru.spbau.mit.java.commons.ServArchitecture;
import ru.spbau.mit.java.commons.proto.BenchOptsMsg;
import ru.spbau.mit.java.commons.proto.ServerStatsMsg;
import ru.spbau.mit.java.server.BenchOpts;
import ru.spbau.mit.java.server.BenchServer;
import ru.spbau.mit.java.server.tcp.ThreadPoolTcpServer;
import ru.spbau.mit.java.server.tcp.ThreadedTcpServer;
import ru.spbau.mit.java.server.stat.ServerStats;

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

                BenchOptsMsg opts = readBenchOptions(in);
                if (opts == null) {
                    log.debug("Bench client disconnected");
                    break;
                }
                log.debug("Got bench options: " + opts.toString());
                ServArchitecture arch = ServArchitecture.fromCode(opts.getServerArchitecture());
                BenchServer serverToBench = createBenchServer(arch, opts);
                if (serverToBench == null) {
                    out.writeInt(BenchReqCode.BAD_ARCH);
                    continue;
                }
                ServerStats stats = null;
                try (BenchServer bs = serverToBench) {
                    bs.start();
                    // client can start benching clients
                    out.writeInt(BenchReqCode.BENCH_READY);
                    stats = bs.bench();
                } catch (IOException e) {
                    log.error("Error during bench: " + e.getMessage());
                } catch (InterruptedException e) {
                    log.error("Interrupt during server stop");
                }

                if (stats == null) {
                    out.writeInt(BenchReqCode.BENCH_FAILED);
                    continue;
                }
                // ok
                ServerStatsMsg statsMsg = ServerStatsMsg.newBuilder()
                        .setAvRequestNs(stats.getAvgRequestProcNs())
                        .setAvSortingNs(stats.getAvgSortingNs())
                        .build();
                byte[] bsStats = statsMsg.toByteArray();
                out.writeInt(bsStats.length);
                out.write(bsStats);
            }
        } catch (IOException e) {
            log.error("IO Error: " + e.getMessage());
        }
    }

    private BenchServer createBenchServer(ServArchitecture arch, BenchOptsMsg opts) throws IOException {
        BenchServer serverToBench = null;
        if (arch == null) {
            log.error("Not supported server arch");
            return null;
        }
        switch (arch) {
            case TCP_THREAD_PER_CLIENT: {
                return new ThreadedTcpServer(
                        opts.getServerPort(),
                        new BenchOpts(opts.getClientNumber(), opts.getRequestsNumber())
                );
            }
            case TCP_THREAD_POOL: {
                return new ThreadPoolTcpServer(
                        opts.getServerPort(),
                        new BenchOpts(opts.getClientNumber(), opts.getRequestsNumber())
                );
            }
            default: {
                log.info("Not supported " + opts.getServerArchitecture());
                break;
            }
        }
        return null;
    }

    private BenchOptsMsg readBenchOptions(DataInputStream in) throws IOException {
        int msgLen = in.readInt();

        if (msgLen == BenchReqCode.DISCONNECT) {
            log.debug("Client disconnected");
            return null;
        }

        byte[] msg = new byte[msgLen];
        in.readFully(msg);
        BenchOptsMsg opts = BenchOptsMsg.parseFrom(msg);
        return opts;
    }
}
