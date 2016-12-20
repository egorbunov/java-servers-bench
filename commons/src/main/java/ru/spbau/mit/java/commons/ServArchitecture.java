package ru.spbau.mit.java.commons;


import java.util.HashMap;
import java.util.Map;

public class ServArchitecture {
    public final static int TCP_THREAD_PER_CLIENT = 0;
    public final static int TCP_THREAD_POOL = 1;
    public final static int TCP_ASYNC = 2;
    public final static int TCP_SINGLE_THREADED = 3;
    public final static int TCP_NON_BLOCKING = 4;

    public final static int UDP_THREAD_PER_REQUEST = 5;
    public final static int UDP_THREAD_POOL = 6;

    public final static Map<Integer, String> idToName;
    public final static Map<String, Integer> nameToId;

    static {
        idToName = new HashMap<>();
        idToName.put(TCP_THREAD_PER_CLIENT, "TCP: Thread per client");
        idToName.put(TCP_THREAD_POOL, "TCP: Cached thread pool");
        idToName.put(TCP_ASYNC, "TCP: Asynchronous");
        idToName.put(TCP_SINGLE_THREADED, "TCP: Single thread, conn. per request");
        idToName.put(TCP_NON_BLOCKING, "TCP: nio");
        idToName.put(UDP_THREAD_PER_REQUEST, "UDP: Thread per request");
        idToName.put(UDP_THREAD_POOL, "UDP: Thread pool for requests");

        nameToId = new HashMap<>();
        for (Map.Entry<Integer, String> e : idToName.entrySet()) {
            nameToId.put(e.getValue(), e.getKey());
        }
    }
}
