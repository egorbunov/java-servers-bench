package ru.spbau.mit.java.commons;

public enum ServerArch {
    TCP_THREAD_PER_CLIENT(0, "TCP: Thread per client"),
    TCP_THREAD_POOL(1, "TCP: Thread pool for clients"),
    TCP_ASYNC(2, "TCP: Asynchronous"),
    TCP_SINGLE_THREADED(3, "TCP: Single thread"),
    TCP_NON_BLOCKING(4, "TCP: Nio"),
    UDP_THREAD_PER_REQUEST(5, "UDP: Thread per request"),
    UDP_THREAD_POOL(6, "UDP: Thread pool for requests");

    private final int code;
    private final String descr;

    ServerArch(int code, String s) {
        this.code = code;
        this.descr = s;
    }

    public int getCode() {
        return code;
    }

    public static ServerArch fromCode(int code) {
        for (ServerArch sa : ServerArch.values()) {
            if (sa.code == code) {
                return sa;
            }
        }
        return null;
    }

    public String getDescr() {
        return descr;
    }

    @Override
    public String toString() {
        return descr;
    }
}
