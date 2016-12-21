package ru.spbau.mit.java.server;

import ru.spbau.mit.java.server.stat.ServerStats;

import java.io.IOException;

/**
 * Base interface for benchmarkable server
 */
public interface BenchServer extends AutoCloseable {
    /**
     * performs benchmark (blocking call) (call start before it)
     */
    ServerStats bench();

    void start();
    void stop() throws InterruptedException, IOException;

    @Override
    default void close() throws InterruptedException, IOException {
        stop();
    }
}
