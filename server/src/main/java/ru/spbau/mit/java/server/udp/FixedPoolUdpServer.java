package ru.spbau.mit.java.server.udp;

import lombok.extern.slf4j.Slf4j;
import ru.spbau.mit.java.commons.UDPProtocol;
import ru.spbau.mit.java.commons.proto.BenchmarkOpts;
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
 * UDP server, which uses fixed thread pool for serving requests
 */
@Slf4j
public class FixedPoolUdpServer implements BenchServer {
    private final DatagramSocket socket;
    private final ExecutorService requestProcessingService;
    private final ExecutorService receiver;
    private Future<?> receiverFuture;
    private final List<Future<OneRequestStats>> requestsFs;

    /**
     * @param port in case port is zero, server socket is opened at any available port
     * @param threadNum number of threads, which will process requests
     */
    public FixedPoolUdpServer(int port, int threadNum) throws SocketException {
        socket = new DatagramSocket(port);
        UDPProtocol.setupServerUDPSocket(socket);
        requestProcessingService = Executors.newFixedThreadPool(threadNum);
        receiver = Executors.newSingleThreadExecutor();
        requestsFs = new ArrayList<>();
    }

    @Override
    public void start() {
        receiverFuture = receiver.submit(new UdpReceiverTask(
                socket,
                t -> {
                    UdpOneRequestTask task = new UdpOneRequestTask(t.v1, t.v2, socket, System.nanoTime());
                    requestsFs.add(requestProcessingService.submit(task));
                }
        ));
    }

    @Override
    public ServerStats stop() throws InterruptedException, IOException, BenchingError {
        receiverFuture.cancel(true);
        socket.close();
        receiver.shutdownNow();
        requestProcessingService.shutdownNow();
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
