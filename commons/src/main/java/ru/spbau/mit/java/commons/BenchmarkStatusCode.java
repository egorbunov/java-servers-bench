package ru.spbau.mit.java.commons;


/**
 * Contains status codes, which are codes sent be benchmark server to client and vice versa
 * for synchronization and error reporting
 */
public class BenchmarkStatusCode {
    public final static int BAD_ARCH = -666; // server runner can't create serv. with given architecture
    public final static int BENCH_FAILED = -661; // error during benchmarking (error in server for benchmark)
    public final static int BENCH_READY = -200; // server successfully created server for bench

    public final static int STOP_BENCH = -42; // stop bench. server code, sent by client

    // common request code
    public final static int DISCONNECT = -123;
}
