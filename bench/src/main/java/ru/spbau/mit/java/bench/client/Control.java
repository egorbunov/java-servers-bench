package ru.spbau.mit.java.bench.client;

public enum Control {
    CLIENT_NUM("Clients number"),
    REQUSET_NUM("Request number"),
    DELAY("Response-request gap"),
    ARRAY_LEN("Array to sort length");

    private final String name;

    Control(String s) {
        name = s;
    }

    @Override
    public String toString() {
        return name;
    }
}
