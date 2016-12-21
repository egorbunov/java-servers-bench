package ru.spbau.mit.java.client.runner;

import ru.spbau.mit.java.commons.proto.IntArrayMsg;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class ArraySupplier implements Supplier<IntArrayMsg> {
    private final int size;

    public ArraySupplier(int size) {
        this.size = size;
    }

    @Override
    public IntArrayMsg get() {
        Random random = ThreadLocalRandom.current();
        IntArrayMsg.Builder builder = IntArrayMsg.newBuilder();
        IntStream.generate(random::nextInt).limit(size).forEach(builder::addNumbers);
        return builder.build();
    }
}
