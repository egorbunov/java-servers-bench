package ru.spbau.mit.java.server;

/**
 * Error during benchmarking on server
 */
public class BenchingError extends Exception {
    public BenchingError() {
        super();
    }

    public BenchingError(String message) {
        super(message);
    }

    public BenchingError(String message, Throwable cause) {
        super(message, cause);
    }
}
