package ru.spbau.mit.java.bench;

public enum Control {
    CLIENT_NUM("Clients number", 1, 5000),
    REQUSET_NUM("Request number", 1, 10000),
    DELAY("Response-request gap (ms)", 0, 1000),
    ARRAY_LEN("Array to sort length", 10, 1000000);

    private final String name;
    private final int min;
    private final int max;

    Control(String s, int min, int max) {
        name = s;
        this.min = min;
        this.max = max;
    }

    @Override
    public String toString() {
        return name;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }
}
