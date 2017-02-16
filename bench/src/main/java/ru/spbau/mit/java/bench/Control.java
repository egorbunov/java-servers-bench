package ru.spbau.mit.java.bench;

/**
 * Available benchmark parameters
 */
public enum Control {
    CLIENT_NUM("Clients number", 1, 1000, 5),
    REQUEST_NUM("Request number", 1, 1000, 5),
    DELAY("Response-request gap (ms)", 0, 1000, 5),
    ARRAY_LEN("Array to sort length", 10, 50000, 100);

    private final String name;
    private final int min;
    private final int max;
    private final int defaultVal;

    Control(String s, int min, int max, int defaultVal) {
        name = s;
        this.min = min;
        this.max = max;
        this.defaultVal = defaultVal;
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

    public int getDefaultVal() {
        return defaultVal;
    }
}
