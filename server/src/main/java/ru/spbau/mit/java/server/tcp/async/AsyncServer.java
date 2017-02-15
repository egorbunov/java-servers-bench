package ru.spbau.mit.java.server.tcp.async;


import lombok.extern.slf4j.Slf4j;
import lombok.val;
import ru.spbau.mit.java.commons.BenchmarkStatusCode;
import ru.spbau.mit.java.commons.proto.IntArrayMsg;
import ru.spbau.mit.java.server.BenchServer;
import ru.spbau.mit.java.server.BenchingError;
import ru.spbau.mit.java.server.RequestProcess;
import ru.spbau.mit.java.server.stat.OneRequestStats;
import ru.spbau.mit.java.server.stat.ServerStats;
import ru.spbau.mit.java.server.tcp.async.state.MyStates;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple asynchronous server
 */
@Slf4j
public class AsyncServer implements BenchServer {
    private int port;
    private AsynchronousServerSocketChannel serverChannel;
    private final Set<AsynchronousSocketChannel> clientChannels;
    private final Set<OneRequestStats> results;


    /**
     * Creates async server, socket and binds it to given port without specifying ip address
     */
    public AsyncServer(int port) throws IOException {
        this.port = port;
        serverChannel= AsynchronousServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress(port));
        results = ConcurrentHashMap.newKeySet();
        clientChannels = ConcurrentHashMap.newKeySet();
    }

    private void removeAsyncChannel(AsynchronousSocketChannel channel) {
        try {
            channel.close();
        } catch (IOException e) {
            log.error("Error closing async. channel! " + e);
        } finally {
            clientChannels.remove(channel);
        }
    }

    private void handleError(Throwable e) {
        log.error("Error occured: " + e);
    }

    private void initAsyncReading(AsynchronousSocketChannel channel) {
        AsyncUtils.asyncRead(
                channel,
                MyStates.intReadingState(),
                msgSizeOrDisconnect -> {
                    if (msgSizeOrDisconnect == BenchmarkStatusCode.DISCONNECT) {
                        log.info("Client disconnected...");
                        removeAsyncChannel(channel);
                        return;
                    }
                    final long startRequest = System.nanoTime();
                    AsyncUtils.asyncRead(
                            channel,
                            MyStates.arrMessageReadingState(msgSizeOrDisconnect),
                            intArrayMsg -> procAndWrite(channel, startRequest, intArrayMsg),
                            this::handleError
                    );
                },
                this::handleError
        );
    }

    private void procAndWrite(AsynchronousSocketChannel channel,
                              long startRequestTime,
                              IntArrayMsg request) {

        long startProc = System.nanoTime();
        IntArrayMsg answer = RequestProcess.processArray(request);
        long procTime = System.nanoTime() - startProc;

        AsyncUtils.asyncWrite(
                channel,
                MyStates.createWritingState(answer),
                () -> {
                    long endReqTime = System.nanoTime();
                    results.add(new OneRequestStats(endReqTime - startRequestTime, procTime));
                    initAsyncReading(channel);
                    return null;
                },
                this::handleError
        );
    }

    private class ConnectCompletor implements CompletionHandler<AsynchronousSocketChannel, Object> {
        @Override
        public void completed(AsynchronousSocketChannel result, Object attachment) {
            log.debug("Connection accepted (async)");
            clientChannels.add(result);
            initAsyncReading(result);
        }

        @Override
        public void failed(Throwable exc, Object attachment) {
            log.error("Error handling async accept: " + exc);
        }
    }

    @Override
    public void start() {
        serverChannel.accept(null, new ConnectCompletor());
    }

    @Override
    public ServerStats stop() throws InterruptedException, IOException, BenchingError {
        serverChannel.close();
        for (val ch : clientChannels) {
            ch.close();
        }

        ServerStats stats = ServerStats.calc(results.parallelStream());
        log.debug("Got " + results.size() + " stats: " + stats);
        return stats;
    }

    @Override
    public int getPort() {
        return port;
    }
}
