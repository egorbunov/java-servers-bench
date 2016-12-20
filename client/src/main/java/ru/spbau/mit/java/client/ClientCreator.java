package ru.spbau.mit.java.client;


import java.io.IOException;

public interface ClientCreator {
    BenchClient create() throws IOException;
}
