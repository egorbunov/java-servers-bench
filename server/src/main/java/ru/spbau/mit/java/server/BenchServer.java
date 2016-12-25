package ru.spbau.mit.java.server;

import ru.spbau.mit.java.server.stat.ServerStats;

import java.io.IOException;

/**
 * Base interface for benchmarkable server
 */
public interface BenchServer extends AutoCloseable {
    void start();

    /**
     * Terminates server and returns all statistics, which was calculated
     * till the moment of the call
     */
    ServerStats stop() throws InterruptedException, IOException, BenchingError;
    int getPort();

    @Override
    default void close() throws InterruptedException, IOException, BenchingError {
        stop();
    }
}
