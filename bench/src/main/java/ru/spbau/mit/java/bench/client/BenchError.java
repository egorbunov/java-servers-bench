package ru.spbau.mit.java.bench.client;

/**
 * Error on bench server side
 */
public class BenchError extends Exception {
    public BenchError() {
        super();
    }

    public BenchError(String message) {
        super(message);
    }

    public BenchError(String message, Throwable cause) {
        super(message, cause);
    }
}
