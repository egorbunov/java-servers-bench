package ru.spbau.mit.java.commons;


public class BenchReqCode {
    public final static int BAD_ARCH = -666; // server runner can't create serv. with given architecture
    public final static int BENCH_FAILED = -661; // error during benchmarking (error in server for benchmark)
    public final static int BENCH_READY = -200; // server successfully created server for bench

    // common request code
    public final static int DISCONNECT = -123;
}
