package ru.spbau.mit.java.client.runner;

import lombok.Data;

@Data
public class RunnerOpts {
    /**
     * number of clients to run in parallel
     */
    private final int clientNumber;
    /**
     * number of int elements in one client message to be sorted on server
     */
    private final int arrayLen;
    /**
     * delay between response receive from server and next client request
     */
    private final int deltaMs;
    /**
     * total number of request to be made by one client before shutting down
     */
    private final int requestNumber;
}
