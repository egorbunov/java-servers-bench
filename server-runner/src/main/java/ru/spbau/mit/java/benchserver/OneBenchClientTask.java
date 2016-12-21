package ru.spbau.mit.java.benchserver;

import lombok.extern.slf4j.Slf4j;
import ru.spbau.mit.java.commons.ServArchitecture;
import ru.spbau.mit.java.commons.proto.BenchOptsMsg;
import ru.spbau.mit.java.commons.proto.ServerStatsMsg;
import ru.spbau.mit.java.server.BenchOpts;
import ru.spbau.mit.java.server.BenchServer;
import ru.spbau.mit.java.server.ThreadedTcpServer;
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
                int msgLen = in.readInt();

                if (msgLen < 0) {
                    log.debug("Client disconnected");
                    break;
                }

                byte[] msg = new byte[msgLen];
                in.readFully(msg);
                BenchOptsMsg opts = BenchOptsMsg.parseFrom(msg);

                log.debug("Gor bench options: " + opts.toString());

                BenchServer serverToBench = null;
                ServArchitecture arch = ServArchitecture.fromCode(opts.getServerArchitecture());
                if (arch == null) {
                    log.error("Not supported server arch");
                    break;
                }
                switch (arch) {
                    case TCP_THREAD_PER_CLIENT: {
                        serverToBench = new ThreadedTcpServer(
                                opts.getServerPort(),
                                new BenchOpts(opts.getClientNumber(), opts.getRequestsNumber())
                        );
                        break;
                    }
                    default: {
                        log.info("Not supported " + opts.getServerArchitecture());
                        break;
                    }
                }

                if (serverToBench == null) {
                    out.writeInt(0);
                } else {
                    try (BenchServer bs = serverToBench) {
                        serverToBench.start();

                        out.writeBoolean(true); // client can start benching clients

                        ServerStats stats = serverToBench.bench();
                        ServerStatsMsg statsMsg = ServerStatsMsg.newBuilder()
                                .setAvRequestNs(stats.getAvgRequestProcNs())
                                .setAvSortingNs(stats.getAvgSortingNs())
                                .build();
                        byte[] bsStats = statsMsg.toByteArray();
                        out.writeInt(bsStats.length);
                        out.write(bsStats);
                    } catch (Exception e) {
                        log.error("Error during bench: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            log.error("Error: " + e.getMessage());
        }
    }
}
