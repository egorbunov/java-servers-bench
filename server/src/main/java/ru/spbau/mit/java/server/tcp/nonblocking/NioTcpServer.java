package ru.spbau.mit.java.server.tcp.nonblocking;

import lombok.extern.slf4j.Slf4j;
import ru.spbau.mit.java.server.BenchServer;
import ru.spbau.mit.java.server.stat.OneRequestStats;
import ru.spbau.mit.java.server.stat.ServerStats;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Server, which uses selectors and stuff
 */
@Slf4j
public class NioTcpServer implements BenchServer {
    private final ExecutorService server;
    private Future serverFuture;
    private final ExecutorService requestProcessingService;
    private final Selector selector;
    private final ServerSocketChannel socket;
    private final List<OneRequestStats> results;
    private final Set<SocketChannel> clientChannels;

    public NioTcpServer(int port, int threadNum) throws IOException {
        this.server = Executors.newSingleThreadExecutor();
        this.requestProcessingService = Executors.newFixedThreadPool(threadNum);
        this.results = new ArrayList<>();
        this.clientChannels = new HashSet<>();

        selector = Selector.open();
        try {
            socket = ServerSocketChannel.open();
            socket.configureBlocking(false);
            InetSocketAddress addr = new InetSocketAddress(port);
            socket.bind(addr);
            socket.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            log.error("Failed to initialize nio server: " + e);
            selector.close();
            throw new IOException(e);
        }
    }


    @Override
    public void start() {
        log.debug("Starting NIO server in separate thread...");
        serverFuture = server.submit(new ServerTask());
    }

    @Override
    public ServerStats stop() throws IOException {
        log.debug("Stopping NIO server...");
        server.shutdownNow();
        serverFuture.cancel(true);
        selector.close();
        socket.close();
        for (SocketChannel sc : clientChannels) {
            sc.close();
        }
        ServerStats stats = ServerStats.calc(results.parallelStream());
        log.debug("Got " + results.size() + " stats: " + stats);
        return stats;
    }

    @Override
    public int getPort() {
        return socket.socket().getLocalPort();
    }

    private class ServerTask implements Runnable {
        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    serve();
                } catch (Exception e) {
                    if (Thread.currentThread().isInterrupted()) {
                        log.info("NIO server thread interrupted.");
                        return;
                    }
                    log.error("Error during serving: " + e);
                }
            }
        }

        private void serve() throws IOException {
            selector.select();
            Iterator<SelectionKey> it = selector.selectedKeys().iterator();
            while (it.hasNext()) {
                SelectionKey key = it.next();
                it.remove();
                if (key.isValid() && key.isAcceptable()) {
                    accept(key);
                }
                if (key.isValid() && key.isReadable()) {
                    read(key);
                }
                if (key.isValid() && key.isWritable()) {
                    write(key);
                }
            }
//            log.debug("Exiting from NIO server...");
        }

        private void accept(SelectionKey key) throws IOException {
            SocketChannel clientSocket = ((ServerSocketChannel) key.channel()).accept();
            log.debug("Client accepted!");
            clientSocket.configureBlocking(false);
            clientSocket.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE,
                    new OneNioClientState(requestProcessingService, clientSocket));
            clientChannels.add(clientSocket);
        }

        private void read(SelectionKey key) throws IOException {
            OneNioClientState clientState = (OneNioClientState) key.attachment();
            clientState.read(key);
            if (clientState.isDisconnected()) {
                log.debug("Cancelling key and closing channel...");
                key.cancel();
                SocketChannel sc = clientState.getChannel();
                clientChannels.remove(sc);
                sc.close();
            }
        }

        private void write(SelectionKey key) throws IOException {
            OneNioClientState clientState = (OneNioClientState) key.attachment();
            Optional<OneRequestStats> result = clientState.write(key);
            if (result.isPresent()) {
                OneRequestStats stats = result.get();
                results.add(stats);
                log.debug("Resetting client state...");
                clientState.reset();
            }
        }
    }
}
