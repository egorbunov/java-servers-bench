package ru.spbau.mit.java.server;

import lombok.Data;

@Data
public class BenchOpts {
    private final int clientNum;
    private final int requestNum;
}
