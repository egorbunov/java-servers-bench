package ru.spbau.mit.java.server.tcp.nonblocking;

import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;
import ru.spbau.mit.java.commons.BenchmarkStatusCode;
import ru.spbau.mit.java.server.RequestProcess;
import ru.spbau.mit.java.server.stat.OneRequestStats;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Created by: Egor Gorbunov
 * Date: 12/25/16
 * Email: egor-mailbox@ya.com
 */

@Slf4j
class OneNioClientState {
    private ExecutorService requestExecutor;
    private SocketChannel channel;

    public SocketChannel getChannel() {
        return channel;
    }

    enum State {
        READING_NOT_STARTED,
        READING_HEADER,
        READING_REQUEST,
        EXECUTING,
        FINISHED_EXECUTION,
        WRITING_RESPONSE,
        FINISHED_ONE_REQUEST,
        CLIENT_DISCONNECT
    }

    private volatile State state;
    private final ByteBuffer requestHeader;
    private ByteBuffer request;
    private ByteBuffer[] response;
    private long receiveTime = -1;
    private long startProcessingTime = -1;
    private long endProcessingTime = -1;
    private Future requestProcFuture;


    OneNioClientState(ExecutorService requestExecutor, SocketChannel channel) {
        this.requestExecutor = requestExecutor;
        this.channel = channel;
        requestHeader = ByteBuffer.allocate(Integer.BYTES);
        reset();
    }

    void reset() {
        state = (state == State.CLIENT_DISCONNECT) ? State.CLIENT_DISCONNECT : State.READING_NOT_STARTED;
        requestHeader.clear();
        request = null;
        response = null;
        requestProcFuture = null;
    }

    boolean isDisconnected() {
        return state == State.CLIENT_DISCONNECT;
    }

    void read(SelectionKey key) throws IOException {
        if (state == State.READING_NOT_STARTED) {
            // start reading array
            receiveTime = System.nanoTime();
            state = State.READING_HEADER;
            return;
        }

        if (state == State.READING_HEADER) {
            if (!tryRead(key, requestHeader)) {
                return;
            }

            requestHeader.flip();
            int msgSizeOrDisconnect = requestHeader.getInt();
            if (msgSizeOrDisconnect == BenchmarkStatusCode.DISCONNECT) {
                log.debug("Got DISCONNECT signal from client");
                state = State.CLIENT_DISCONNECT;
                return;
            } else {
                log.debug("Header contains message len = " + msgSizeOrDisconnect);
            }

            request = ByteBuffer.allocate(msgSizeOrDisconnect);
            log.debug("Allocated buffer: pos = " + request.position() + " ; lim = " + request.limit());
            state = State.READING_REQUEST;
        } else if (state == State.READING_REQUEST) {
            if (!tryRead(key, request)) {
                return;
            }

            request.flip();

            log.debug("Request buf pos = " + request.position());
            log.debug("Request buf lim = " + request.limit());

            state = State.EXECUTING;
            requestProcFuture = requestExecutor.submit(new RequestExecutionTask());
        }
    }

    Optional<OneRequestStats> write(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();

        if (requestProcFuture != null && requestProcFuture.isDone()) {
            try {
                requestProcFuture.get();
                if (state == State.FINISHED_EXECUTION) {
                    state = State.WRITING_RESPONSE;
                } else {
                    log.debug("Hm. Strange execution state after job done: " + state);
                }
            } catch (InterruptedException e) {
                log.error("Interrupted: " + e);
                key.cancel();
                channel.close();
                return Optional.empty();
            } catch (ExecutionException e) {
                log.error("Error during request execution: " + e.getCause());
                key.cancel();
                channel.close();
                return Optional.empty();
            }
        }

        if (state != State.WRITING_RESPONSE) {
            return Optional.empty();
        }

        if (response[0].hasRemaining() || response[1].hasRemaining()) {
            channel.write(response);
        }

        if (!response[0].hasRemaining() && !response[1].hasRemaining()) {
            state = State.FINISHED_ONE_REQUEST;
            long sendTime = System.nanoTime();
            return Optional.of(new OneRequestStats(
                    sendTime - receiveTime,
                    endProcessingTime - startProcessingTime));
        }

        return Optional.empty();
    }

    private boolean tryRead(SelectionKey key, ByteBuffer buffer) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        int read = channel.read(buffer);
        if (read < 0) {
            channel.close();
            key.cancel();
            return false;
        }
        return !requestHeader.hasRemaining();
    }

    private class RequestExecutionTask implements Callable<Void> {
        @Override
        public Void call() throws InvalidProtocolBufferException {
            startProcessingTime = System.nanoTime();

            int msgLen = request.limit();
            byte[] msg = new byte[msgLen];
            request.get(msg);
            byte[] result = RequestProcess.process(msg);

            ByteBuffer responseHeader = ByteBuffer.allocate(Integer.BYTES);
            responseHeader.putInt(result.length);
            responseHeader.flip();
            ByteBuffer responseBody = ByteBuffer.wrap(result);
            response = new ByteBuffer[] {responseHeader, responseBody};
            endProcessingTime = System.nanoTime();
            state = State.FINISHED_EXECUTION;
            return null;
        }
    }
}
