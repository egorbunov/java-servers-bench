package ru.spbau.mit.java.server.tcp.nonblocking;

import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;
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
class OneRequestState {
    private ExecutorService requestExecutor;

    enum State {
        WAITING_FOR_SERVING,
        READING_HEADER,
        READING_REQUEST,
        EXECUTING,
        FINISHED_EXECUTION,
        WRITING_RESPONSE,
        FINISHED
    }

    private volatile State state;
    private final ByteBuffer requestHeader;
    private ByteBuffer request;
    private ByteBuffer[] response;
    private long receiveTime = -1;
    private long startProcessingTime = -1;
    private long endProcessingTime = -1;
    private Future requestProcFuture;


    OneRequestState(ExecutorService requestExecutor) {
        this.requestExecutor = requestExecutor;
        state = State.WAITING_FOR_SERVING;
        requestHeader = ByteBuffer.allocate(Integer.BYTES);
        request = null;
        response = null;
        requestProcFuture = null;
    }

    void read(SelectionKey key) throws IOException {
        if (state == State.WAITING_FOR_SERVING) {
            receiveTime = System.nanoTime();
            state = State.READING_HEADER;
        }

        if (state == State.READING_HEADER) {
            if (!tryRead(key, requestHeader)) {
                return;
            }

            requestHeader.flip();
            int msgSize = requestHeader.getInt();
            request = ByteBuffer.allocate(msgSize);
            state = State.READING_REQUEST;
        } else if (state == State.READING_REQUEST) {
            if (!tryRead(key, request)) {
                return;
            }

            request.flip();
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
            state = State.FINISHED;
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
