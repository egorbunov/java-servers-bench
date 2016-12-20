package ru.spbau.mit.java.client.runner;

import ru.spbau.mit.java.commons.proto.IntArray;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ArraySupplier implements Supplier<IntArray> {
    private final int size;

    public ArraySupplier(int size) {
        this.size = size;
    }

    @Override
    public IntArray get() {
        Random random = ThreadLocalRandom.current();
        IntArray.Builder builder = IntArray.newBuilder();
        IntStream.generate(random::nextInt).limit(size).forEach(builder::addNumbers);
        return builder.build();
    }
}
