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
                byte[] msg = new byte[msgLen];
                in.readFully(msg);
                BenchOptsMsg opts = BenchOptsMsg.parseFrom(msg);

                log.debug("Gor bench options: " + opts.toString());

                BenchServer serverToBench = null;
                switch (opts.getServerArchitecture()) {
                    case ServArchitecture.TCP_THREAD_PER_CLIENT: {
                        serverToBench = new ThreadedTcpServer(
                                opts.getServerPort(),
                                new BenchOpts(opts.getClientNumber(), opts.getRequestsNumber())
                        );
                        break;
                    }
                    default: {
                        log.info("Not supported " + opts.getServerArchitecture());
                    }
                }

                if (serverToBench == null) {
                    out.writeInt(0);
                } else {
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
                }

                serverToBench.stop();
            }
        } catch (IOException e) {
            log.error("Error: " + e.getMessage());
        } catch (InterruptedException e) {
            log.error("Error waiting for bench server to stop: " + e.getMessage());
        }
    }
}
