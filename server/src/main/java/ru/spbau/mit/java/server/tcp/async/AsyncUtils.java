package ru.spbau.mit.java.server.tcp.async;


import lombok.extern.slf4j.Slf4j;
import ru.spbau.mit.java.server.tcp.async.state.MessageRead;
import ru.spbau.mit.java.server.tcp.async.state.ReadingState;
import ru.spbau.mit.java.server.tcp.async.state.WritingIsDone;
import ru.spbau.mit.java.server.tcp.async.state.WritingState;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Slf4j
public class AsyncUtils {
    /**
     * Asynchronous connect to remote server with callbacks
     */
    public static void asyncConnect(SocketAddress addr,
                                    Consumer<AsynchronousSocketChannel> onComplete,
                                    Consumer<Throwable> onFail) {
        AsynchronousSocketChannel conn;
        try {
            conn = AsynchronousSocketChannel.open();
        } catch (IOException e) {
            onFail.accept(e);
            return;
        }

        conn.connect(addr, null, new CompletionHandler<Void, Object>() {
            @Override
            public void completed(Void result, Object attachment) {
                onComplete.accept(conn);
            }

            @Override
            public void failed(Throwable exc, Object attachment) {
                onFail.accept(exc);
            }
        });
    }

    /**
     * Async. read from given channel once
     */
    public static <T> void asyncRead(AsynchronousSocketChannel channel,
                                     ReadingState<T> initState,
                                     Consumer<T> onComplete,
                                     Consumer<Throwable> onFail) {

        class MyReadCompletionHandler implements CompletionHandler<Integer, Object> {
            private ReadingState<T> state;
            private final Consumer<T> onComplete;
            private final Consumer<Throwable> onFail;

            private MyReadCompletionHandler(ReadingState<T> state,
                                            Consumer<T> onComplete,
                                            Consumer<Throwable> onFail) {

                this.state = state;
                this.onComplete = onComplete;
                this.onFail = onFail;
            }

            @Override
            public void completed(Integer result, Object attachment) {
                state = state.proceed();
                if (state instanceof MessageRead) {
                    onComplete.accept(state.getMessage());
                } else {
                    if (channel.isOpen()) {
                        channel.read(state.getBuffer(), null, this);
                    }
                }
            }

            @Override
            public void failed(Throwable exc, Object attachment) {
                onFail.accept(exc);
            }
        }

        channel.read(
                initState.getBuffer(),
                null,
                new MyReadCompletionHandler(initState, onComplete, onFail)
        );
    }

    /**
     * Asynchronous write
     */
    public static void asyncWrite(AsynchronousSocketChannel channel,
                                  WritingState initWritingState,
                                  Supplier<Void> onComplete,
                                  Consumer<Throwable> onFail) {

        class MyWriteCompletionHandler implements CompletionHandler<Integer, Object> {

            private WritingState state;
            private final Supplier<Void> onComplete;
            private final Consumer<Throwable> onFail;

            private MyWriteCompletionHandler(WritingState state,
                                             Supplier<Void> onComplete,
                                             Consumer<Throwable> onFail) {

                this.state = state;
                this.onComplete = onComplete;
                this.onFail = onFail;
            }

            @Override
            public void completed(Integer result, Object attachment) {
                state = state.proceed();
                if (state instanceof WritingIsDone) {
                    onComplete.get();
                } else {
                    channel.write(state.getBuffer(), null, this);
                }

            }

            @Override
            public void failed(Throwable exc, Object attachment) {
                onFail.accept(exc);
            }
        }

        channel.write(
                initWritingState.getBuffer(),
                null,
                new MyWriteCompletionHandler(initWritingState, onComplete, onFail)
        );

    }
}
