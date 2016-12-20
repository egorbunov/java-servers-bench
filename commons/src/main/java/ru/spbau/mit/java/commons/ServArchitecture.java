package ru.spbau.mit.java.commons;


public class ServArchitecture {
    public final static int TCP_THREAD_PER_CLIENT = 0;
    public final static int TCP_THREAD_POOL = 1;
    public final static int TCP_ASYNC = 2;
    public final static int TCP_SINGLE_THREADED = 3;
    public final static int TCP_NON_BLOCKING = 4;

    public final static int UDP_THREAD_PER_REQUEST = 5;
    public final static int UDP_THREAD_POOL = 6;
}
