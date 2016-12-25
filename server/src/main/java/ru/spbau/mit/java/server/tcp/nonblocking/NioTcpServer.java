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
    private final List<SocketChannel> clientChannels;

    public NioTcpServer(int port, int threadNum) throws IOException {
        this.server = Executors.newSingleThreadExecutor();
        this.requestProcessingService = Executors.newFixedThreadPool(threadNum);
        this.results = new ArrayList<>();
        this.clientChannels = new ArrayList<>();

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
        serverFuture = server.submit(new ServerTask());
    }

    @Override
    public ServerStats stop() throws IOException {
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
                } catch (IOException e) {
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
        }

        private void accept(SelectionKey key) throws IOException {
            SocketChannel clientSocket = ((ServerSocketChannel) key.channel()).accept();
            clientSocket.configureBlocking(false);
            clientSocket.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE,
                    new OneRequestState(requestProcessingService));
            clientChannels.add(clientSocket);
        }

        private void read(SelectionKey key) throws IOException {
            OneRequestState oneRequestState = (OneRequestState) key.attachment();
            oneRequestState.read(key);
        }

        private void write(SelectionKey key) throws IOException {
            OneRequestState oneRequestState = (OneRequestState) key.attachment();
            Optional<OneRequestStats> result = oneRequestState.write(key);
            if (result.isPresent()) {
                OneRequestStats stats = result.get();
                results.add(stats);
                key.attach(new OneRequestState(requestProcessingService));
            }
        }
    }
}
