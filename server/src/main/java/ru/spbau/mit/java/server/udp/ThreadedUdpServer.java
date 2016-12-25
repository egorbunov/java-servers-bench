package ru.spbau.mit.java.server.udp;

import lombok.extern.slf4j.Slf4j;
import ru.spbau.mit.java.commons.proto.Protobuf;
import ru.spbau.mit.java.server.BenchServer;
import ru.spbau.mit.java.server.BenchingError;
import ru.spbau.mit.java.server.stat.OneRequestStats;
import ru.spbau.mit.java.server.stat.ServerStats;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Server, which creates thread per request
 */
@Slf4j
public class ThreadedUdpServer implements BenchServer {
    private final DatagramSocket socket;
    private final int maxDatagramSize;
    private final ExecutorService receiver;
    private Future<?> receiverFuture;
    private final List<Future<OneRequestStats>> requestsFs;
    private final List<ExecutorService> requestExecutors;

    /**
     * @param port in case port is zero, server socket is opened at any available port
     * @param maxDatagramSize maximum number of bytes in message
     */
    public ThreadedUdpServer(int port, int maxDatagramSize) throws SocketException {
        socket = new DatagramSocket(port);
        this.maxDatagramSize = maxDatagramSize;
        receiver = Executors.newSingleThreadExecutor();
        requestsFs = new ArrayList<>();
        requestExecutors = new ArrayList<>();
    }

    @Override
    public void start() {
        receiverFuture = receiver.submit(new UdpReceiverTask(
                socket,
                maxDatagramSize,
                datagramPacket -> {
                    UdpOneRequestTask task =
                            new UdpOneRequestTask(datagramPacket, socket, System.nanoTime());
                    ExecutorService es = Executors.newSingleThreadExecutor();
                    requestsFs.add(es.submit(task));
                    requestExecutors.add(es);
                }
        ));
    }

    @Override
    public ServerStats stop() throws InterruptedException, IOException, BenchingError {
        receiverFuture.cancel(true);
        socket.close();
        receiver.shutdownNow();
        for (ExecutorService es : requestExecutors) {
            es.shutdownNow();
        }
        List<OneRequestStats> stats = new ArrayList<>(requestsFs.size());
        for (Future<OneRequestStats> f : requestsFs) {
            try {
                stats.add(f.get());
            } catch (ExecutionException e) {
                log.error("Request execution failed: " + e.getCause());
            }
        }
        ServerStats result = ServerStats.calc(stats.parallelStream());
        log.debug("Got stats for " + stats.size() + " requests; result = " + result);
        return result;
    }

    @Override
    public int getPort() {
        return socket.getLocalPort();
    }
}

