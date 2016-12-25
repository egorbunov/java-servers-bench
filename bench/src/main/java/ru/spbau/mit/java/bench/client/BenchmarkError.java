package ru.spbau.mit.java.bench.client;

/**
 * Error on bench server side
 */
public class BenchmarkError extends Exception {
    public BenchmarkError() {
        super();
    }

    public BenchmarkError(String message) {
        super(message);
    }

    public BenchmarkError(String message, Throwable cause) {
        super(message, cause);
    }
}
