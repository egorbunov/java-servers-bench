package ru.spbau.mit.java.commons;

public abstract class StoppableRunnable implements Runnable {
    private volatile boolean stopped = false;

    public void stop() {
        stopped = true;
    }

    public boolean isStopped() {
        return stopped;
    }
}
