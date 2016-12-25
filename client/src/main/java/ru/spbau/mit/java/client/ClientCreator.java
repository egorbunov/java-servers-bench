package ru.spbau.mit.java.client;


import java.io.IOException;

public interface ClientCreator {
    Client create() throws IOException;
}
